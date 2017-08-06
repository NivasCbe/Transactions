package leadIq.validation;

import org.apache.commons.lang3.StringUtils;

import leadIq.transaction.domain.Transaction;

public class TransactionValidator{
	
	public static boolean isValid(Transaction transaction) {
		
	    if(transaction != null && transaction.getTransactionid() != null && transaction.getAmount() > 0d && StringUtils.isNotEmpty(transaction.getType())) {
	    		return true;
	    }
	    
		return false;
	}

}
