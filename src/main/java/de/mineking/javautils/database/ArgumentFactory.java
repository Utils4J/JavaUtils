package de.mineking.javautils.database;

import org.jdbi.v3.core.argument.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ArgumentFactory {
	@NotNull
	String getName();

	@Nullable
	Object getValue();

	@NotNull
	Argument create(@NotNull Table<?> table);

	@NotNull
	static ArgumentFactory create(@NotNull String name, @Nullable Object value, @NotNull Function<Table<?>, Argument> factory) {
		return new ArgumentFactory() {
			@NotNull
			@Override
			public String getName() {
				return name;
			}

			@Nullable
			@Override
			public Object getValue() {
				return value;
			}

			@NotNull
			@Override
			public Argument create(@NotNull Table<?> table) {
				return factory.apply(table);
			}
		};
	}

	@NotNull
	static ArgumentFactory createDefault(@NotNull String name, @Nullable Object value) {
		return create(name, value, table -> {
			var f = table.getColumns().get(name);
			if(f == null) throw new IllegalStateException("Table has no column with name '" + name + "'");

			var type = f.getGenericType();
			var mapper = table.getManager().getMapper(type, f);

			Object v;

			try {
				v = mapper.format(table.getManager(), type, f, value);
			} catch(IllegalArgumentException | ClassCastException ex) {
				v = value;
			}

			return mapper.createArgument(table.getManager(), type, f, v);
		});
	}
}
