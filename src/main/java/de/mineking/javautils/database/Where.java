package de.mineking.javautils.database;

import de.mineking.javautils.ID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface Where {
	@NotNull
	static <T> Where of(@NotNull Table<T> table, @NotNull T object) {
		if(table.getKeys().isEmpty()) throw new IllegalArgumentException("Cannot identify object without keys");
		return allOf(table.getKeys().entrySet().stream()
				.map(e -> {
					try {
						var field = e.getValue();
						var value = table.getManager().getMapper(field.getType(), field).string(table.getManager(), field.getType(), field, field.get(object));

						if(value == null) return Where.empty();

						return equals(e.getKey(), value);
					} catch(IllegalAccessException ex) {
						throw new RuntimeException(ex);
					}
				})
				.toList()
		);
	}

	@NotNull
	static Where empty() {
		return new Where() {
			@NotNull
			@Override
			public Map<String, Object> values() {
				return Collections.emptyMap();
			}

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

			@NotNull
			@Override
			public Where and(@NotNull Where other) {
				return other;
			}

			@NotNull
			@Override
			public Where or(@NotNull Where other) {
				return other;
			}

			@NotNull
			@Override
			public Where not() {
				return this;
			}
		};
	}

	@NotNull
	static Where allOf(@NotNull Where where, @NotNull Where... others) {
		for(var w : others) where = where.and(w);
		return where;
	}

	@NotNull
	static Where allOf(@NotNull Collection<Where> wheres) {
		return allOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where anyOf(@NotNull Where where, @NotNull Where... others) {
		for(var w : others) where = where.or(w);
		return where;
	}

	@NotNull
	static Where anyOf(@NotNull Collection<Where> wheres) {
		return anyOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where noneOf(@NotNull Where where, @NotNull Where... others) {
		for(var w : others) where = where.and(w.not());
		return where;
	}

	@NotNull
	static Where noneOf(@NotNull Collection<Where> wheres) {
		return noneOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where equals(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, "=");
	}

	@NotNull
	static Where notEqual(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, "!=");
	}

	@NotNull
	static Where like(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, "like");
	}

	@NotNull
	static Where likeIgnoreCase(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, "ilike");
	}

	@NotNull
	static Where greater(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, ">");
	}

	@NotNull
	static Where lower(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, "<");
	}

	@NotNull
	static Where greaterOrEqual(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, ">=");
	}

	@NotNull
	static Where lowerOrEqual(@NotNull String name, @NotNull Object value) {
		return WhereImpl.create(name, value, "<=");
	}

	@NotNull
	static Where not(@NotNull Where where) {
		return where.not();
	}

	@NotNull
	default Where and(@NotNull Where other) {
		return WhereImpl.combined(this, other, "and");
	}

	@NotNull
	default Where or(@NotNull Where other) {
		return WhereImpl.combined(this, other, "or");
	}

	@NotNull
	default Where not() {
		return new WhereImpl("not " + get(), values());
	}

	@NotNull
	Map<String, Object> values();

	@NotNull
	String get();

	@NotNull
	default String format() {
		return "where " + get();
	}

	class WhereImpl implements Where {
		private final String str;
		private final Map<String, Object> values;

		public WhereImpl(String str, Map<String, Object> values) {
			this.str = str;
			this.values = values;
		}

		public static Where create(String name, Object value, String operator) {
			var id = ID.generate().asString();

			var data = new HashMap<String, Object>();
			data.put(id, value);

			return new WhereImpl("\"" + name + "\" " + operator + " :" + id, data);
		}

		public static Where combined(Where a, Where b, String operator) {
			if(a.get().isEmpty()) return b;
			if(b.get().isEmpty()) return a;

			var combined = new HashMap<>(a.values());
			combined.putAll(b.values());

			return new WhereImpl("(" + a.get() + ") " + operator + " (" + b.get() + ")", combined);
		}

		@NotNull
		@Override
		public Map<String, Object> values() {
			return values;
		}

		@NotNull
		@Override
		public String get() {
			return str;
		}
	}
}
