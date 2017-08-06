package leadIq.transaction.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import leadIq.transaction.domain.Transaction;
import leadIq.transaction.resources.TransactionController;

public class TransactionService {
	
	private Map<Long,Transaction> idToTransactionMap; 
	private Map<String,List<Transaction>> typeToTransactionsMap;
	private ReentrantLock primaryLock;
	private ReentrantLock secondaryLock;
	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
	
	public TransactionService(ConcurrentHashMap<Long,Transaction> idToTransactionMap, ConcurrentHashMap<String,List<Transaction>> typeToTransactionsMap,ReentrantLock primaryLock,ReentrantLock secondaryLock) {
		this.idToTransactionMap = idToTransactionMap;
		this.typeToTransactionsMap = typeToTransactionsMap;
		this.primaryLock = primaryLock;
		this.secondaryLock = secondaryLock;
	}
	
	public void addTransactions(Transaction transaction) throws TimeoutException, InterruptedException {
		Long currentTransactionId = transaction.getTransactionid();
		try {
			if (primaryLock.tryLock(100, TimeUnit.MILLISECONDS)) {
				// lock timeout and exceptions needs to be handled yet
				logger.debug("Primary lock successfully acquired for transaction-id "+currentTransactionId);
				Transaction existingTransaction = idToTransactionMap.get(currentTransactionId);
				if (existingTransaction != null) {
					transaction.setChildrenTransactionIdList(existingTransaction.getChildrenTransactionIdList());
					logger.info("TRANSACTION ALREADY EXISTS - GETTING REPLACED for transaction-id "+currentTransactionId);
				}
				idToTransactionMap.put(currentTransactionId, transaction);
				Long parentId = transaction.getParentId();
				if (parentId != null && idToTransactionMap.get(parentId) != null) {
					Transaction parentTransaction = idToTransactionMap.get(parentId);
					List<Long> childTransactionsList = parentTransaction.getChildrenTransactionIdList();
					if (childTransactionsList == null) {
						childTransactionsList = new ArrayList<Long>();
						parentTransaction.setChildrenTransactionIdList(childTransactionsList);
					}
					childTransactionsList.add(transaction.getTransactionid());
					logger.debug("Parent transaction successfully updated with children list for transaction-id "+currentTransactionId);
				}
			} else {
				logger.error("Timeout in acquiring primary lock transaction-id "+currentTransactionId);
				throw new TimeoutException("Timedout when acquiring primary lock");
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException transaction-id "+currentTransactionId);
			throw new RuntimeException("The thread was interrupted");
		} finally {
			primaryLock.unlock();
		}

		// This task can also be done asynchronously by delegating to a thread pool
		try {
			if (secondaryLock.tryLock(100, TimeUnit.MILLISECONDS)) {
				logger.debug("Secondary lock successfully acquired for transaction-id "+currentTransactionId);
				String transactionType = transaction.getType();
				List<Transaction> transactionList = typeToTransactionsMap.get(transactionType);
				if (transactionList == null) {
					transactionList = new ArrayList<>();
					typeToTransactionsMap.put(transactionType, transactionList);
				}
				transactionList.add(transaction);
			} else {
				logger.error("Timeout in acquiring secondary lock transaction-id "+currentTransactionId);
				throw new TimeoutException("Timedout when acquiring secondary lock");
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException transaction-id "+currentTransactionId);
			throw new RuntimeException("The thread was interrupted");
		} finally {
			secondaryLock.unlock();
		}
	}
	
	public Transaction getTransactionById(Long transactionId) {
		Transaction resultantTransaction = null;
		resultantTransaction = idToTransactionMap.get(transactionId);
		logger.debug("getTransactionById - transactionId "+transactionId+" ResultantTransaction "+resultantTransaction);
		return resultantTransaction;
	}
	
	public List<Transaction> getTransactionByType(String transactionType) {
		List<Transaction> resultantTransaction = null;
		resultantTransaction = typeToTransactionsMap.get(transactionType);
		logger.debug("getTransactionByType - transactionType "+transactionType+" ResultantTransaction "+resultantTransaction);
		return resultantTransaction;
	}
	
	public double getSumByTransactionId(Long transactionId) {
		double sum = 0;
		if (transactionId != null && idToTransactionMap.get(transactionId) != null) {
			// Do breadth first search
			Queue childTransationsQueue = new LinkedList<List<Long>>();
			Transaction transaction = idToTransactionMap.get(transactionId);
			sum += transaction.getAmount();
			List<Long> childrenTransactionIds = transaction.getChildrenTransactionIdList();
			while (childrenTransactionIds != null) {
				for (Long currentTransactionId : childrenTransactionIds) {
					Transaction currentTransaction = idToTransactionMap.get(currentTransactionId);
					if (currentTransaction != null) {
						sum += currentTransaction.getAmount();
						if (currentTransaction.getChildrenTransactionIdList() != null) {
							childTransationsQueue.add(currentTransaction.getChildrenTransactionIdList());
						}
					}
				}
				childrenTransactionIds = (List<Long>) childTransationsQueue.poll();
			}
		}
		logger.debug("getSumByTransactionId - transactionId "+transactionId+" ResultantSum "+sum);
		return sum;
	}

}
