package de.mineking.javautils.function;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;

public final class TryCatch {
	private TryCatch() {
	}


	/*
	Runnable
	 */

	public static <E extends Throwable> void tryAndThrow(@NotNull ThrowingRunnable<E> tryMethod) {
		tryAndHandle(tryMethod, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	public static <E extends Throwable> void tryAndIgnore(@NotNull ThrowingRunnable<E> tryMethod) {
		tryAndHandle(tryMethod, e -> {});
	}

	public static <E extends Throwable> void tryAndPrint(@NotNull ThrowingRunnable<E> tryMethod) {
		tryAndHandle(tryMethod, Throwable::printStackTrace);
	}

	public static <E extends Throwable> void tryAndLog(@NotNull ThrowingRunnable<E> tryMethod, @NotNull Logger logger) {
		tryAndHandle(tryMethod, e -> logger.error("Failed to execute task", e));
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable> void tryAndHandle(@NotNull ThrowingRunnable<E> tryMethod, @NotNull Consumer<E> catchMethod) {
		try {
			tryMethod.run();
		} catch(Throwable e) {
			catchMethod.accept((E) e);
		}
	}

	/*
	Consumer
	 */

	public static <E extends Throwable, T> void tryAndThrow(@NotNull ThrowingConsumer<E, T> tryMethod, T arg) {
		tryAndHandle(tryMethod, arg, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	public static <E extends Throwable, T> void tryAndIgnore(@NotNull ThrowingConsumer<E, T> tryMethod, T arg) {
		tryAndHandle(tryMethod, arg, e -> {});
	}

	public static <E extends Throwable, T> void tryAndPrint(@NotNull ThrowingConsumer<E, T> tryMethod, T arg) {
		tryAndHandle(tryMethod, arg, Throwable::printStackTrace);
	}

	public static <E extends Throwable, T> void tryAndLog(@NotNull ThrowingConsumer<E, T> tryMethod, T arg, @NotNull Logger logger) {
		tryAndHandle(tryMethod, arg, e -> logger.error("Failed to execute task", e));
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable, T> void tryAndHandle(@NotNull ThrowingConsumer<E, T> tryMethod, T arg, @NotNull Consumer<E> catchMethod) {
		try {
			tryMethod.accept(arg);
		} catch(Throwable e) {
			catchMethod.accept((E) e);
		}
	}


	/*
	Function
	 */

	public static <E extends Throwable, T, R> Optional<R> tryAndThrow(@NotNull ThrowingFunction<E, T, R> tryMethod, T arg) {
		return tryAndHandle(tryMethod, arg, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	public static <E extends Throwable, T, R> Optional<R> tryAndIgnore(@NotNull ThrowingFunction<E, T, R> tryMethod, T arg) {
		return tryAndHandle(tryMethod, arg, e -> {});
	}

	public static <E extends Throwable, T, R> Optional<R> tryAndPrint(@NotNull ThrowingFunction<E, T, R> tryMethod, T arg) {
		return tryAndHandle(tryMethod, arg, Throwable::printStackTrace);
	}

	public static <E extends Throwable, T, R> Optional<R> tryAndLog(@NotNull ThrowingFunction<E, T, R> tryMethod, T arg, @NotNull Logger logger) {
		return tryAndHandle(tryMethod, arg, e -> logger.error("Failed to execute task", e));
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable, T, R> Optional<R> tryAndHandle(@NotNull ThrowingFunction<E, T, R> tryMethod, T arg, @NotNull Consumer<E> catchMethod) {
		try {
			return Optional.ofNullable(tryMethod.apply(arg));
		} catch(Throwable e) {
			catchMethod.accept((E) e);
			return Optional.empty();
		}
	}


	/*
	Supplier
	 */

	public static <E extends Throwable, T> Optional<T> tryAndThrow(@NotNull ThrowingSupplier<E, T> tryMethod) {
		return tryAndHandle(tryMethod, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	public static <E extends Throwable, T> Optional<T> tryAndIgnore(@NotNull ThrowingSupplier<E, T> tryMethod) {
		return tryAndHandle(tryMethod, e -> {});
	}

	public static <E extends Throwable, T> Optional<T> tryAndPrint(@NotNull ThrowingSupplier<E, T> tryMethod) {
		return tryAndHandle(tryMethod, Throwable::printStackTrace);
	}

	public static <E extends Throwable, T> Optional<T> tryAndLog(@NotNull ThrowingSupplier<E, T> tryMethod, @NotNull Logger logger) {
		return tryAndHandle(tryMethod, e -> logger.error("Failed to execute task", e));
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable, T> Optional<T> tryAndHandle(@NotNull ThrowingSupplier<E, T> tryMethod, @NotNull Consumer<E> catchMethod) {
		try {
			return Optional.ofNullable(tryMethod.get());
		} catch(Throwable e) {
			catchMethod.accept((E) e);
			return Optional.empty();
		}
	}
}
