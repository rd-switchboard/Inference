package org.rdswitchboard.importers.search;

import java.util.concurrent.Semaphore;

public class MatcherThread extends Thread {

	private Matcher matcher = null;
	private Object lock = new Object();
	private boolean canExit = false;
	private final Semaphore semaphore;
	
	public MatcherThread(Semaphore semaphore) {
		this.semaphore = semaphore;
	}
	
	public boolean isFree() {
		synchronized(lock) {
			return null == this.matcher;
		}
	}
	
	public void addMatcher(Matcher matcher) throws MatcherThreadException {
		synchronized(lock) {
			if (null != this.matcher)
				throw new MatcherThreadException("The matcher tread is busy");
		
			this.matcher = matcher;
			this.lock.notify();
		}
	}
	
	public synchronized void finishCurrentAndExit() {
		synchronized(lock) {
			
			this.canExit = true;
			this.lock.notify();
		}
	}
	
	@Override
	public void run() {
		try {
			 for (;;) {
				 synchronized(lock) {
					 if (canExit)
						 break;
					 
					 if (null == matcher) 
						 lock.wait();
				 }
				 
				 if (null != matcher) {
					 matcher.run();
					 matcher = null;
					 semaphore.release();
				 }
			 }
		} catch (InterruptedException e) {
	    }
    }
}
