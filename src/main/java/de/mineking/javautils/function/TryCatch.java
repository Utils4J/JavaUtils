package de.mineking.javautils.function;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public interface TryCatch<E extends Throwable, R> {

	R tryExecute() throws E;

	void catchException(@NotNull E e);

	@SuppressWarnings("unchecked")
	default Optional<R> execute() {
		try {
			return Optional.ofNullable(tryExecute());
		} catch (Throwable e) {
			catchException((E) e);
		}
		return Optional.empty();
	}

	//Runnable
	static <E extends Throwable> void tryAndThrow(@NotNull ThrowingRunnable<E> tryMethod) {
		tryAndHandle(tryMethod, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	static <E extends Throwable> void tryAndIgnore(@NotNull ThrowingRunnable<E> tryMethod) {
		tryAndHandle(tryMethod, e -> {});
	}

	static <E extends Throwable> void tryAndPrint(@NotNull ThrowingRunnable<E> tryMethod) {
		tryAndHandle(tryMethod, Throwable::printStackTrace);
	}

	static <E extends Throwable> void tryAndHandle(@NotNull ThrowingRunnable<E> tryMethod, @NotNull Consumer<E> catchMethod) {
		new TryCatch<E, Object>() {
			@Override
			public Object tryExecute() throws E {
				tryMethod.run();
				return null;
			}

			@Override
			public void catchException(@NotNull E e) {
				catchMethod.accept(e);
			}
		}.execute();
	}

	//Consumer

	static <E extends Throwable, T> void tryAndThrow(@NotNull ThrowingConsumer<E, T> tryMethod, T t) {
		tryAndHandle(tryMethod, t, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	static <E extends Throwable, T> void tryAndIgnore(@NotNull ThrowingConsumer<E, T> tryMethod, T t) {
		tryAndHandle(tryMethod, t, e -> {});
	}

	static <E extends Throwable, T> void tryAndPrint(@NotNull ThrowingConsumer<E, T> tryMethod, T t) {
		tryAndHandle(tryMethod, t, Throwable::printStackTrace);
	}

	static <E extends Throwable, T> void tryAndHandle(@NotNull ThrowingConsumer<E, T> tryMethod, T t, @NotNull Consumer<E> catchMethod) {
		new TryCatch<E, Object>() {
			@Override
			public Object tryExecute() throws E {
				tryMethod.accept(t);
				return null;
			}

			@Override
			public void catchException(@NotNull E e) {
				catchMethod.accept(e);
			}
		}.execute();
	}


	//Function
	static <E extends Throwable, T, R> Optional<R> tryAndThrow(@NotNull ThrowingFunction<E, T, R> tryMethod, T t) {
		return tryAndHandle(tryMethod, t, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	static <E extends Throwable, T, R> Optional<R> tryAndIgnore(@NotNull ThrowingFunction<E, T, R> tryMethod, T t) {
		return tryAndHandle(tryMethod, t, e -> {});
	}

	static <E extends Throwable, T, R> Optional<R> tryAndPrint(@NotNull ThrowingFunction<E, T, R> tryMethod, T t) {
		return tryAndHandle(tryMethod, t, Throwable::printStackTrace);
	}

	static <E extends Throwable, T, R> Optional<R> tryAndHandle(@NotNull ThrowingFunction<E, T, R> tryMethod, T t, @NotNull Consumer<E> catchMethod) {
		return new TryCatch<E, R>() {
			@Override
			public R tryExecute() throws E {
				return tryMethod.apply(t);
			}

			@Override
			public void catchException(@NotNull E e) {
				catchMethod.accept(e);
			}
		}.execute();
	}


	//Supplier

	static <E extends Throwable, T> Optional<T> tryAndThrow(@NotNull ThrowingSupplier<E, T> tryMethod) {
		return tryAndHandle(tryMethod, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	static <E extends Throwable, T> Optional<T> tryAndIgnore(@NotNull ThrowingSupplier<E, T> tryMethod) {
		return tryAndHandle(tryMethod, e -> {});
	}

	static <E extends Throwable, T> Optional<T> tryAndPrint(@NotNull ThrowingSupplier<E, T> tryMethod) {
		return tryAndHandle(tryMethod, Throwable::printStackTrace);
	}

	static <E extends Throwable, T> Optional<T> tryAndHandle(@NotNull ThrowingSupplier<E, T> tryMethod, @NotNull Consumer<E> catchMethod) {
		return new TryCatch<E, T>() {
			@Override
			public T tryExecute() throws E {
				return tryMethod.get();
			}

			@Override
			public void catchException(@NotNull E e) {
				catchMethod.accept(e);
			}
		}.execute();
	}


}
