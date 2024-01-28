import data.TE;
import data.TestClass;
import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class InsertTest {
	public final DatabaseManager manager;
	public final Table<TestClass> table;

	public InsertTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/test", "postgres", "test123");
		table = manager.getTable(TestClass.class, this::createInstance, "test").createTable();
	}

	private TestClass createInstance() {
		return new TestClass(table);
	}

	@Test
	public void insert() {
		var test = new TestClass(table, TE.B, EnumSet.noneOf(TE.class));

		table.insert(test);
		assertThrows(ConflictException.class, () -> table.insert(test));

		test.id = null;
		test.insert();
		assertThrows(ConflictException.class, test::insert);
	}
}
