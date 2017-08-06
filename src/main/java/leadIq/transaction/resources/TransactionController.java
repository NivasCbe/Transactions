package leadIq.transaction.resources;

import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.validation.Validator;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import leadIq.transaction.core.TransactionService;
import leadIq.transaction.domain.Transaction;
import leadIq.validation.TransactionValidator;

@Path("/transactionservice")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {

	private final TransactionService service;
	private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

	public TransactionController( TransactionService service) {
		this.service = service;
	}

	@GET
	@Path("/transaction/{transaction_id}")
	public Response getTransactionById(@PathParam("transaction_id") Long transactionId) {
		Transaction resultantTransaction = service.getTransactionById(transactionId);
		return Response.ok(resultantTransaction).build();
	}

	@PUT
	@Path("/transaction/{transaction_id}")
	public Response putTransactionById(@PathParam("transaction_id") Long transactionId, Transaction transaction) {
		logger.debug("ADDING TRANSACTION with transaction-id "+transactionId);
		transaction.setTransactionid(transactionId);
		if (TransactionValidator.isValid(transaction)) {
			try {
				service.addTransactions(transaction);
			} catch (TimeoutException e) {
				return Response.status(Status.GATEWAY_TIMEOUT).build();
			} catch (InterruptedException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		return Response.ok("SUCCESS").build();
	}

	@GET
	@Path("/types/{transaction_type}")
	public Response getTransactionByType(@PathParam("transaction_type") String transactionType) {
		List<Transaction> resultantTransaction = service.getTransactionByType(transactionType);
		return Response.ok(resultantTransaction).build();
	}

	@GET
	@Path("/sum/{transaction_id}")
	public Response getTransactionSumById(@PathParam("transaction_id") Long transactionId) {
		double resultantSum = service.getSumByTransactionId(transactionId);
		return Response.ok(resultantSum).build();
	}

}
