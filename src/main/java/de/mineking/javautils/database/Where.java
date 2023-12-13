package de.mineking.javautils.database;

import org.jetbrains.annotations.NotNull;

public interface Where {
	@NotNull
	static Where empty() {
		return new Where() {
			@NotNull
			@Override
			public String get() {
				return "";
			}

			@NotNull
			@Override
			public String format() {
				return "";
			}
		};
	}

	@NotNull
	static Where of(@NotNull String condition) {
		return () -> condition;
	}

	@NotNull
	static Where equals(@NotNull String name, @NotNull Object value) {
		return () -> name + " = '" + value + "'";
	}

	@NotNull
	static Where notEqual(@NotNull String name, @NotNull Object value) {
		return () -> name + " != '" + value + "'";
	}

	@NotNull
	static Where like(@NotNull String name, @NotNull Object value) {
		return () -> name + " like '" + value + "'";
	}

	@NotNull
	static Where likeIgnoreCase(@NotNull String name, @NotNull Object value) {
		return () -> name + " ilike '" + value + "'";
	}

	@NotNull
	static Where greater(@NotNull String name, @NotNull Object value) {
		return () -> name + " > '" + value + "'";
	}

	@NotNull
	static Where lower(@NotNull String name, @NotNull Object value) {
		return () -> name + " < '" + value + "'";
	}

	@NotNull
	static Where greaterOrEqual(@NotNull String name, @NotNull Object value) {
		return () -> name + " >= '" + value + "'";
	}

	@NotNull
	static Where lowerOrEqual(@NotNull String name, @NotNull Object value) {
		return () -> name + " <= '" + value + "'";
	}

	@NotNull
	default Where and(@NotNull Where other) {
		return () -> "(" + format() + ") and (" + other.format() + ")";
	}

	@NotNull
	default Where or(@NotNull Where other) {
		return () -> "(" + format() + ") or (" + other.format() + ")";
	}

	@NotNull
	String get();

	@NotNull
	default String format() {
		return "where " + get();
	}
}
