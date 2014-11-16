package cz.rhok.prague.osf.governmentcontacts.helper;

import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import cz.rhok.prague.osf.governmentcontacts.scraper.CanBeRepeatedException;
import cz.rhok.prague.osf.governmentcontacts.scraper.UnableToConnectToServer;

public abstract class RepeatOnTimeoutTask<V> implements Callable<V> {

	private static final int WAIT_TIME_BETWEEN_REPETITION = 1000 /* ms */;
	private static final int MAX_NUMBER_OF_REPETITION = 5;


	@Override
	public V call() {

		AtomicInteger repetitionCount = new AtomicInteger();

		while(repetitionCount.get() <= MAX_NUMBER_OF_REPETITION) {
			try {
				
				if (repetitionCount.get() > 0) {
					/* just wait, timeout can be caused by detection of automatization (too fast) or server is overloaded  */
                    try {
                        Thread.sleep(WAIT_TIME_BETWEEN_REPETITION);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
				
				return doTask();
				
			} catch (RuntimeException e) {
				if (canBeRepeated(e) || canBeRepeated(e.getCause())) {
					repeatOrRethrow(repetitionCount, e);
                } else {
					throw e;
				}
			}
		}
		
		// this line should not be reached
		throw new RuntimeException("Operation failed");
	}

	private boolean canBeRepeated(Throwable e) {
		return 
			e instanceof SocketTimeoutException || 
			e instanceof NoRouteToHostException || 
			e instanceof CanBeRepeatedException;
	}
	
	private void repeatOrRethrow(AtomicInteger repetitionCount, RuntimeException e) {
		if (repetitionCount.get() < MAX_NUMBER_OF_REPETITION) {
			repetitionCount.incrementAndGet();
		} else {
			throw e;
		}
	}

	public abstract V doTask();

}
