import data.TE;
import data.TestClass;
import de.mineking.javautils.database.*;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

public class DatabaseTest {
	public final DatabaseManager manager;
	public final Table<TestClass> table;

	public DatabaseTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/test", "postgres", "test123");
		table = manager.getTable(TestClass.class, this::createInstance, "test").createTable();
	}

	private TestClass createInstance() {
		return new TestClass(table);
	}

	@Test
	public void create() {}

	@Test
	public void insert() {
		System.out.println(new TestClass(table, TE.B, EnumSet.allOf(TE.class)).update());
	}

	@Test
	public void delete() {
		new TestClass(table, TE.A, EnumSet.noneOf(TE.class)).update().delete();
	}

	@Test
	public void update() {
		table.selectOne(Where.equals("id", "001zlwbQgDo")).ifPresent(e -> {
			e.enumTest = TE.C;
			e.update();
		});
	}

	@Test
	public void selectMany() {
		var selected = table.selectMany(
				Where.anyOf(
						Where.equals("id", "001zlwbGkKW"),
						Where.equals("id", "001zlwbQgDo"),
						Where.equals("id", "001zlwbL9BA")
				),
				Order.descendingBy("id")
						.offset(1)
						.limit(2)
		);

		System.out.println(selected);
	}

	@Test
	public void selectAll() {
		System.out.println(table.selectAll());
		System.out.println(table.selectAll(Order.ascendingBy("id")));
		System.out.println(table.selectAll(Order.descendingBy("id")));
	}

	@Test
	public void deleteAll() {
		table.deleteAll();
	}
}
