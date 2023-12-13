package de.mineking.javautils.database;

import org.jetbrains.annotations.NotNull;

public interface Order {
	@NotNull
	static Order empty() {
		return () -> "";
	}

	@NotNull
	static Order ascendingBy(@NotNull String column) {
		return () -> "order by '" + column + "' asc";
	}

	@NotNull
	static Order descendingBy(@NotNull String column) {
		return () -> "order by '" + column + "' desc";
	}

	@NotNull
	default Order limit(int limit) {
		return () -> format() + " limit " + limit;
	}

	@NotNull
	default Order offset(int offset) {
		return () -> format() + " offset " + offset;
	}

	@NotNull
	String format();
}
