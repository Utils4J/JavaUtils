package de.mineking.javautils.database;

import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface TypeMapper<T> {
	boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@NotNull
	String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@Nullable
	default Object value(@NotNull DatabaseManager manager, @NotNull Field f, @Nullable T value) {
		return value;
	}

	@Nullable
	T parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException;

	TypeMapper<Integer> SERIAL = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return f.getAnnotation(Column.class).autoincrement() && (int.class.isAssignableFrom(type) || long.class.isAssignableFrom(type));
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return int.class.isAssignableFrom(type) ? "int" : "bigint";
		}

		@Override
		public Integer parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return (Integer) set.getObject(name);
		}
	};

	TypeMapper<Integer> INTEGER = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return int.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "int";
		}

		@Override
		public Integer parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return (Integer) set.getObject(name);
		}
	};

	TypeMapper<Long> LONG = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return long.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "bigint";
		}

		@Override
		public Long parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return (Long) set.getObject(name);
		}
	};

	TypeMapper<Double> DOUBLE = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return double.class.isAssignableFrom(type) || Double.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "decimal";
		}

		@Override
		public Double parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return (Double) set.getObject(name);
		}
	};

	TypeMapper<Boolean> BOOLEAN = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "boolean";
		}

		@Override
		public Boolean parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return (Boolean) set.getObject(name);
		}
	};

	TypeMapper<String> STRING = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return String.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "text";
		}

		@Override
		public String parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return (String) set.getObject(name);
		}
	};

	TypeMapper<Instant> TIMESTAMP = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return Instant.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "timestamp";
		}

		@Override
		public Instant parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			var timestamp = set.getTimestamp(name);
			return timestamp == null ? null : timestamp.toInstant();
		}
	};

	TypeMapper<Optional<?>> OPTIONAL = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
			return type.equals(Optional.class);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			var p = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
			return manager.getType((Class<?>) p, f);
		}

		@NotNull
		@Override
		public Optional<?> parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			var p = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			return Optional.ofNullable(manager.parse((Class<?>) p, field, name, set, ctx));
		}
	};

	TypeMapper<Enum<?>> ENUM = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return type.isEnum();
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "test";
		}

		@Nullable
		@Override
		public Object value(@NotNull DatabaseManager manager, @NotNull Field f, @Nullable Enum<?> value) {
			return value == null ? null : value.name();
		}

		@Nullable
		@Override
		public Enum<?> parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			return getEnumConstant(type, set.getString(name)).orElse(null);
		}

		private Optional<Enum<?>> getEnumConstant(Class<?> type, String name) {
			return Arrays.stream((Enum<?>[]) type.getEnumConstants())
					.filter(c -> c.name().equals(name))
					.findFirst();
		}
	};

	TypeMapper<?> ARRAY = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return type.isArray() || type.isAssignableFrom(List.class);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			var component = getComponentType(type, f.getGenericType());
			var t = manager.getType(component, f);
			manager.db.registerArrayType(component, t);
			return t + "[]";
		}

		@Nullable
		@Override
		public Object parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext ctx) throws SQLException {
			var array = getArray(set.getArray(name));
			return type.isArray() ? array : Arrays.asList(array);
		}

		private Class<?> getComponentType(Class<?> type, Type generic) {
			if(type.isArray()) return type.getComponentType();
			else {
				var p = ((ParameterizedType) generic).getActualTypeArguments()[0];
				return (Class<?>) p;
			}
		}

		@SuppressWarnings("unchecked")
		private <C> C[] getArray(Array array) throws SQLException {
			return (C[]) array.getArray();
		}
	};
}
