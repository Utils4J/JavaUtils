package de.mineking.javautils.function;

@FunctionalInterface
public interface ThrowingSupplier<E extends Throwable, T> {

	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	T get() throws E;

}
