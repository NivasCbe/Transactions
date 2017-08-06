package leadIq.transaction;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import leadIq.transaction.core.TransactionService;
import leadIq.transaction.domain.Transaction;
import leadIq.transaction.resources.TransactionController;

public class TransactionApplication extends Application<TransactionConfiguration> {

	private static final Logger logger = LoggerFactory.getLogger(TransactionApplication.class);
	
    public static void main(final String[] args) throws Exception {
        new TransactionApplication().run(args);
    }

    @Override
    public String getName() {
        return "assignment";
    }

    @Override
    public void initialize(final Bootstrap<TransactionConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final TransactionConfiguration configuration,
                    final Environment environment) {
    	environment.jersey().register(new TransactionController(new TransactionService(new ConcurrentHashMap<Long,Transaction>(),new ConcurrentHashMap<String,List<Transaction>>(),new ReentrantLock(),new ReentrantLock())));
    	logger.info("********SERVER STARTED********");
    }

}
