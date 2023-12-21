import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.Table;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

public class ArrayTest {
	public final DatabaseManager manager;
	public final Table<ATest> table;

	public class ATest {
		@Column
		private final Set<String>[] array;

		public ATest(Set<String>[] array) {
			this.array = array;
		}

		public ATest() {
			this(null);
		}

		@Override
		public String toString() {
			return Arrays.deepToString(array);
		}
	}

	public ArrayTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/test", "postgres", "test123");
		manager.getDriver().installPlugin(new PostgresPlugin());

		table = manager.getTable(ATest.class, ATest::new, "array_test").createTable();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void insert() {
		table.insert(new ATest(new Set[] {Set.of("a", "b", "c"), Set.of("d", "e"), Set.of()}));
		//table.insert(new ATest(new Integer[][] {{1, 2, 3}, {4, 5, 6}}));
	}

	@Test
	public void select() {
		System.out.println(table.selectAll());
	}
}
