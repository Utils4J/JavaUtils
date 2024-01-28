package de.mineking.javautils.function;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {

	void run() throws E;

}
