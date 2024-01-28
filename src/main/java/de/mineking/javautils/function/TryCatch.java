package de.mineking.javautils.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface TryCatch<T extends Exception> {

	@FunctionalInterface
	interface ExeptRunnable<T extends Exception> {

		void run() throws T;

	}

	void tryExecute() throws T;

	void catchException(@NotNull T e);

	@SuppressWarnings("unchecked")
	default void execute() {
		try {
			tryExecute();
		} catch (Exception e) {
			catchException((T) e);
		}
	}

	static <T extends Exception> void tryAndThrow(@NotNull ExeptRunnable<T> tryMethod) {
		tryCatch(tryMethod, e -> {
			if (e instanceof RuntimeException re) throw re;
			throw new RuntimeException(e);
		});
	}

	static <T extends Exception> void tryAndIgnore(@NotNull ExeptRunnable<T> tryMethod) {
		tryCatch(tryMethod, e -> {});
	}

	static <T extends Exception> void tryAndPrint(@NotNull ExeptRunnable<T> tryMethod) {
		tryCatch(tryMethod, Throwable::printStackTrace);
	}

	static <T extends Exception> void tryCatch(@NotNull ExeptRunnable<T> tryMethod, @NotNull Consumer<T> catchMethod) {
		new TryCatch<T>() {
			@Override
			public void tryExecute() throws T {
				tryMethod.run();
			}

			@Override
			public void catchException(@NotNull T e) {
				catchMethod.accept(e);
			}
		}.execute();
	}
}
