package de.mineking.javautils.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberStrategy;
import de.mineking.javautils.ID;
import de.mineking.javautils.database.type.DataType;
import de.mineking.javautils.database.type.PostgresType;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public interface TypeMapper<T, R> {
	boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@NotNull
	DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@NotNull
	default Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable T value) {
		return new Argument() {
			@Override
			public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
				statement.setObject(position, value);
			}

			@Override
			public String toString() {
				return Objects.toString(value);
			}
		};
	}

	@Nullable
	@SuppressWarnings("unchecked")
	default T string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable R value) {
		return (T) value;
	}

	@Nullable
	T extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException;

	@SuppressWarnings("unchecked")
	@Nullable
	default R parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable T value) {
		return (R) value;
	}

	TypeMapper<Integer, Integer> SERIAL = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return f.getAnnotation(Column.class).autoincrement() && (int.class.isAssignableFrom(type) || long.class.isAssignableFrom(type));
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return int.class.isAssignableFrom(type) ? PostgresType.SERIAL : PostgresType.BIG_SERIAL;
		}

		@Nullable
		@Override
		public Integer extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Integer) set.getObject(name);
		}
	};

	TypeMapper<Integer, Integer> INTEGER = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return int.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.INTEGER;
		}

		@Nullable
		@Override
		public Integer extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Integer) set.getObject(name);
		}
	};

	TypeMapper<Long, Long> LONG = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return long.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.BIG_INT;
		}

		@Nullable
		@Override
		public Long extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Long) set.getObject(name);
		}
	};

	TypeMapper<Double, Double> DOUBLE = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return double.class.isAssignableFrom(type) || Double.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.NUMERIC;
		}

		@Nullable
		@Override
		public Double extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Double) set.getObject(name);
		}
	};

	TypeMapper<Boolean, Boolean> BOOLEAN = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.BOOLEAN;
		}

		@Nullable
		@Override
		public Boolean extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Boolean) set.getObject(name);
		}
	};

	TypeMapper<String, String> STRING = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return String.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.TEXT;
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}
	};

	TypeMapper<Instant, Instant> TIMESTAMP = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return Instant.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Instant value) {
			return new Argument() {
				@Override
				public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
					statement.setTimestamp(position, value == null ? null : Timestamp.from(value));
				}

				@Override
				public String toString() {
					return Objects.toString(value);
				}
			};
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.TIMESTAMP;
		}

		@Nullable
		@Override
		public Instant extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			var timestamp = set.getTimestamp(name);
			return timestamp == null ? null : timestamp.toInstant();
		}
	};

	TypeMapper<String, ID> MKID = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return ID.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.TEXT;
		}

		@NotNull
		@Override
		public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable ID value) {
			return value == null ? ID.generate().asString() : value.asString();
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}

		@Nullable
		@Override
		public de.mineking.javautils.ID parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable String value) {
			return value == null ? null : ID.decode(value);
		}
	};

	TypeMapper<Object, Optional<?>> OPTIONAL = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
			return type.equals(Optional.class);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			var p = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
			return manager.getType((Class<?>) p, f);
		}

		@Nullable
		@Override
		public Object string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Optional<?> value) {
			if(value == null) return null;

			var p = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
			return manager.getMapper((Class<?>) p, f).string(manager, (Class<?>) p, f, value.orElse(null));
		}

		@Nullable
		@Override
		public Object extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getObject(name);
		}

		@NotNull
		@Override
		public Optional<?> parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Object value) {
			var p = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			return Optional.ofNullable(manager.parse((Class<?>) p, field, value));
		}
	};

	TypeMapper<String, Enum<?>> ENUM = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return type.isEnum();
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.TEXT;
		}

		@Nullable
		@Override
		public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Enum<?> value) {
			return value == null ? null : value.name();
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}

		@Nullable
		@Override
		public Enum<?> parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable String value) {
			if(value == null) return null;
			return getEnumConstant(type, value).orElse(null);
		}

		private Optional<Enum<?>> getEnumConstant(Class<?> type, String name) {
			return Arrays.stream((Enum<?>[]) type.getEnumConstants())
					.filter(c -> c.name().equals(name))
					.findFirst();
		}
	};

	TypeMapper<Object[], ?> ARRAY = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return type.isArray() || Collection.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			var component = getComponentType(type, f.getGenericType());
			return DataType.ofName(manager.getType(component, f).getName() + "[]");
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Object[] value) {
			return new Argument() {
				@Override
				public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
					if(value == null) {
						statement.setArray(position, null);
						return;
					}

					var c = getComponentType(type, f.getGenericType());
					while(c.isArray() || Collection.class.isAssignableFrom(c)) c = getComponentType(c, f.getGenericType());
					DataType t = manager.getType(c, f);

					statement.setArray(position, statement.getConnection().createArrayOf(t.getName(), toSql(manager, value, f)));
				}

				@Override
				public String toString() {
					return type.isArray() ? Arrays.deepToString((Object[]) value) : Objects.toString(value);
				}
			};
		}

		private Object[] toSql(DatabaseManager manager, Object o, Field field) {
			var component = getComponentType(o.getClass(), field.getGenericType());

			if(component.isArray() || Collection.class.isAssignableFrom(component)) {
				var maxLength = stream(o).filter(Objects::nonNull).mapToInt(e -> e.getClass().isArray() ? ((Object[]) e).length : ((Collection<?>) e).size()).max().orElse(0);
				if(maxLength == 0) return null;

				return stream(o)
						.map(e -> {
							if(e == null) return new Object[0];
							return e.getClass().isArray() ? (Object[]) e : ((Collection<?>) e).toArray(i -> (Object[]) Array.newInstance(getComponentType(component, field.getGenericType()), i));
						})
						.map(e -> Arrays.copyOf(e, maxLength))
						.map(e -> toSql(manager, e, field))
						.toArray();
			}

			return stream(o).map(e -> manager.getMapper(component, field).string(manager, component, field, e)).toArray();
		}

		private Stream<?> stream(Object o) {
			return o.getClass().isArray() ? Arrays.stream((Object[]) o) : ((Collection<?>) o).stream();
		}

		@Nullable
		@Override
		public Object[] extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			var temp = set.getArray(name);
			if(temp == null) return null;
			else return (Object[]) temp.getArray();
		}

		@Nullable
		@Override
		public Object parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Object[] value) {
			var component = getComponentType(type, field.getGenericType());
			if(value == null) return type.isArray() ? new Object[0] : createCollection(type, component, Collections.emptyList());

			var array = Arrays.stream(value)
					.filter(o -> component.isArray() || Collection.class.isAssignableFrom(component) || o != null)
					.map(e -> manager.parse(component, field, e))
					.toList();

			return type.isArray() ? array.toArray(i -> (Object[]) Array.newInstance(component, i)) : createCollection(type, component, array);
		}

		@Nullable
		@Override
		public Object[] string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Object value) {
			if(value == null) return null;
			return type.isArray() ? (Object[]) value : ((Collection<?>) value).toArray(i -> (Object[]) Array.newInstance(getComponentType(type, f.getGenericType()), i));
		}

		private Class<?> getComponentType(Class<?> type, Type generic) {
			if(type.isArray()) return type.getComponentType();
			else return getClass(generic);
		}

		private Class<?> getClass(Type type) {
			if(type instanceof Class<?> c) return c;
			else if(type instanceof GenericArrayType g) return getClass(g.getGenericComponentType());
			else if(type instanceof ParameterizedType p) return getClass(p.getActualTypeArguments()[0]);
			throw new IllegalArgumentException();
		}

		@SuppressWarnings("unchecked")
		private <C> Collection<C> createCollection(Class<?> type, Class<?> component, List<C> array) {
			if(type.isAssignableFrom(List.class)) return new ArrayList<>(array);
			else if(type.isAssignableFrom(Set.class)) return new HashSet<>(array);
			else if(type.isAssignableFrom(EnumSet.class)) return (Collection<C>) createEnumSet(array, component);

			throw new IllegalStateException("Cannot create collection for " + type.getTypeName() + " with component " + component.getTypeName());
		}

		@SuppressWarnings("unchecked")
		private <E extends Enum<E>> EnumSet<E> createEnumSet(Collection<?> collection, Class<?> component) {
			return collection.isEmpty() ? EnumSet.noneOf((Class<E>) component) : EnumSet.copyOf((Collection<E>) collection);
		}
	};

	TypeMapper<String, ?> JSON = new TypeMapper<>() {
		public static final ToNumberStrategy numberStrategy = in -> {
			var str = in.nextString();
			return str.contains(".") ? Double.parseDouble(str) : Integer.parseInt(str);
		};

		private final static Gson gson = new GsonBuilder()
				.setNumberToNumberStrategy(numberStrategy)
				.setObjectToNumberStrategy(numberStrategy)
				.create();

		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return f.isAnnotationPresent(Json.class);
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable String value) {
			return new Argument() {
				@Override
				public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
					statement.setString(position, value);
				}

				@Override
				public String toString() {
					return Objects.toString(value);
				}
			};
		}

		@NotNull
		@Override
		public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Object value) {
			return gson.toJson(value);
		}

		@NotNull
		@Override
		public DataType getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return PostgresType.TEXT;
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}

		@Nullable
		@Override
		public Object parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable String value) {
			return value == null ? null : gson.fromJson(value, type);
		}
	};
}
