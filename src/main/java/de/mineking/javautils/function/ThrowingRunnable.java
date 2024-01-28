package de.mineking.javautils.function;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {

	/**
	 * When an object implementing interface {@code Runnable} is used
	 * to create a thread, starting the thread causes the object's
	 * {@code run} method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method {@code run} is that it may
	 * take any action whatsoever.
	 *
	 * @see     java.lang.Thread#run()
	 */
	void run() throws E;

}
