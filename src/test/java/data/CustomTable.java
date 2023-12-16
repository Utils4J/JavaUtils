package data;

import de.mineking.javautils.database.Order;
import de.mineking.javautils.database.Table;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CustomTable extends Table<TestClass> {
	@NotNull
	@Override
	CustomTable createTable();

	default List<TestClass> select2() {
		return selectAll(Order.empty().limit(2));
	}
}
