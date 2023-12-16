import de.mineking.javautils.ID;
import de.mineking.javautils.database.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

public class DatabaseTest {
	private final DatabaseManager manager;
	private final Table<TestClass> table;

	public enum TE {
		A, B, C
	}

	public class TestClass implements DataClass<TestClass> {
		@NotNull
		@Override
		public Table<TestClass> getTable() {
			return table;
		}

		@Column(key = true)
		public ID id;

		@Column
		public TE enumTest;

		@Column
		public EnumSet<TE> enums;

		private TestClass() {}

		public TestClass(TE e, EnumSet<TE> enums) {
			this.enumTest = e;
			this.enums = enums;
		}

		@Override
		public String toString() {
			return id.asString() + ": " + enumTest + ", " + enums + " (" + (id.getTimeCreated().toEpochMilli() - System.currentTimeMillis()) + ")";
		}
	}

	public DatabaseTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/test", "postgres", "test123");
		table = manager.getTable(TestClass.class, TestClass::new, "test").createTable();
	}

	@Test
	public void create() {}

	@Test
	public void insert() {
		System.out.println(new TestClass(TE.B, EnumSet.allOf(TE.class)).update());
	}

	@Test
	public void delete() {
		new TestClass(TE.A, EnumSet.noneOf(TE.class)).update().delete();
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
