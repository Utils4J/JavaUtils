import data.KeyTestClass;
import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import org.junit.jupiter.api.Test;

public class KeyTest {
	public final DatabaseManager manager;
	public final Table<KeyTestClass> table;

	public KeyTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/test", "postgres", "test123");
		table = manager.getTable(KeyTestClass.class, KeyTestClass::new, "key_test").createTable();
	}

	@Test
	public void create() {
	}

	@Test
	public void insert() {
		System.out.println(table.insert(new KeyTestClass("Test")));
	}

	@Test
	public void update() {
		table.selectOne(Where.equals("id", 1)).ifPresent(o -> {
			o.text = "abc";
			table.insert(o);
		});
	}

	@Test
	public void copy() {
		table.selectOne(Where.equals("id", 1)).ifPresent(o -> {
			o.id = 0;
			table.insert(o);
		});
	}
}
