import data.CustomTable;
import data.TestClass;
import de.mineking.javautils.database.DatabaseManager;
import org.junit.jupiter.api.Test;

public class CustomTableTest {
	public final DatabaseManager manager;
	public final CustomTable table;

	public CustomTableTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/test", "postgres", "test123");
		table = manager.getTable(CustomTable.class, TestClass.class, this::createInstance, "test").createTable();
	}

	private TestClass createInstance() {
		return new TestClass(table);
	}

	@Test
	public void create() {}

	@Test
	public void select2() {
		System.out.println(table.select2());
	}
}
