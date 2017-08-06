package leadIq.transaction.core;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import junit.framework.Assert;
import leadIq.transaction.domain.Transaction;

public class TransactionServiceTest {
	
	@Test
	public void testGetSumByTransactionId() {
		TransactionService service = getTransactionService();
		Assert.assertTrue(service.getSumByTransactionId(121l) == 1230);
		Assert.assertTrue(service.getSumByTransactionId(131l) == 390);
		Assert.assertTrue(service.getSumByTransactionId(201l) == 150);
	}
	
	@Test
	public void testAddTransactionsOperations() {
		TransactionService service = getTransactionService();
		List<Transaction> transactions = service.getTransactionByType("type1");
		Assert.assertTrue(transactions.size() == 3);
		transactions = service.getTransactionByType("type3");
		Assert.assertTrue(transactions.size() == 2);
		Transaction transaction = service.getTransactionById(171l);
		Assert.assertTrue(transaction.getAmount() == 140d);
		transaction = service.getTransactionById(201l);
		Assert.assertTrue(transaction.getAmount() == 150d);
		Assert.assertTrue(transaction.getType().equals("type3"));
	}
	
	@Test
	public void testAddTransactionLockAcquireFailures() {
		ReentrantLock primaryLockMck = Mockito.mock(ReentrantLock.class);
		try {
			Mockito.when(primaryLockMck.tryLock(Matchers.anyLong(),Matchers.any(TimeUnit.class))).thenReturn(false);
		} catch (InterruptedException e) {
		}
		TransactionService service = new TransactionService(new ConcurrentHashMap<Long,Transaction>(),new ConcurrentHashMap<String,List<Transaction>>(),primaryLockMck,new ReentrantLock());
		try {
			service.addTransactions(getTransaction(120d,"type0",200l,121l));
		}catch(Exception e) {
			Assert.assertTrue(e instanceof TimeoutException);
		}
		
		try {
			Mockito.when(primaryLockMck.tryLock(Matchers.anyLong(),Matchers.any(TimeUnit.class))).thenThrow(new InterruptedException());
			service.addTransactions(getTransaction(120d,"type0",200l,121l));
		} catch (Exception e) {
			Assert.assertTrue(e instanceof RuntimeException);
		}
		
		// Test Failures with secondary Lock
		service = new TransactionService(new ConcurrentHashMap<Long,Transaction>(),new ConcurrentHashMap<String,List<Transaction>>(),new ReentrantLock(),primaryLockMck);
		try {
			service.addTransactions(getTransaction(120d,"type0",200l,121l));
		} catch (Exception e) {
			Assert.assertTrue(e instanceof RuntimeException);
		}
	}
	
	public TransactionService getTransactionService() {
		 TransactionService service = new TransactionService(new ConcurrentHashMap<Long,Transaction>(),new ConcurrentHashMap<String,List<Transaction>>(),new ReentrantLock(),new ReentrantLock());
		 try {
			 service.addTransactions(getTransaction(120d,"type0",200l,121l));
			 service.addTransactions(getTransaction(130d,"type1",121l,131l));
			 service.addTransactions(getTransaction(130d,"type1",131l,141l));
			 service.addTransactions(getTransaction(130d,"type1",141l,151l));
			 service.addTransactions(getTransaction(140d,"type2",121l,161l));
			 service.addTransactions(getTransaction(140d,"type2",161l,171l));
			 service.addTransactions(getTransaction(140d,"type2",171l,181l));
			 service.addTransactions(getTransaction(150d,"type3",121l,191l));
			 service.addTransactions(getTransaction(150d,"type3",121l,201l));
		}catch(Exception e) {
			 
		 }
		 
		return service;
	}
	
	public Transaction getTransaction(double amount,String type,Long parentId,Long transactionId) {
		Transaction transaction =new Transaction();
		transaction.setAmount(amount);
		transaction.setType(type);
		transaction.setParentId(parentId);
		transaction.setTransactionid(transactionId);
		return transaction;
	}
	

}
