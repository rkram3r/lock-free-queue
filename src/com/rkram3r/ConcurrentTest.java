package com.rkram3r;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class ConcurrentTest {
	private static final int TIMEOUT = 1000;

	private ConcurrentLinkedQueue<Throwable> uncaughtExceptions = new ConcurrentLinkedQueue<>();

	@BeforeEach
	public void beforeEach() {
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> uncaughtExceptions.add(exception));
	}

	@AfterEach
	public void afterEach() {
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			if (thread != Thread.currentThread() && !thread.isDaemon() && !isSystemThread(thread)) {
				try {
					thread.join(TIMEOUT);
					if (thread.isAlive()) {
						throw new RuntimeException("Thread join timed out: " + thread);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		for (Throwable throwable : uncaughtExceptions) {
			throwable.printStackTrace();
		}
		if (uncaughtExceptions.size() > 0) {
			throw new RuntimeException("Errors in other threads");
		}
	}

	private boolean isSystemThread(Thread thread) {
		return thread.getClass().getPackage().getName().startsWith("org.eclipse");
	}
}
