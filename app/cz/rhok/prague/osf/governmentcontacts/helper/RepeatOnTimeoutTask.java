package cz.rhok.prague.osf.governmentcontacts.helper;

import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import cz.rhok.prague.osf.governmentcontacts.scraper.CanBeRepeatedException;
import cz.rhok.prague.osf.governmentcontacts.scraper.UnableToConnectToServer;

public abstract class RepeatOnTimeoutTask<V> implements Callable<V> {

	private static final int WAIT_TIME_BETWEEN_REPETITION = 1000 /* ms */;
	private static final int MAX_NUMBER_OF_REPETITION = 5;


	@Override
	public V call() {

		int repetitionCount = 0;

		while(repetitionCount <= MAX_NUMBER_OF_REPETITION) {
			try {
				
				if (repetitionCount > 0) {
					/* just wait, timeout can be caused by detection of automatization (too fast) or server is overloaded  */
                    try {
                        Thread.sleep(WAIT_TIME_BETWEEN_REPETITION);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
				
				return doTask();
			} catch (UnableToConnectToServer utce) {
				repetitionCount++;
			} catch (RuntimeException re) {
				if (re.getCause() instanceof SocketTimeoutException) {
					repetitionCount++;
				}
                else if(re.getCause() instanceof CanBeRepeatedException
                          || re instanceof CanBeRepeatedException) {
                    repetitionCount++;
                }
				else {
					throw re;
				}
			}
		}
		
		throw new RuntimeException("Operation failed");

	}


	public abstract V doTask();

}
