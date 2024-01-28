package database;

import de.mineking.javautils.ID;
import de.mineking.javautils.database.*;
import de.mineking.javautils.database.exception.ConflictException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InsertTest {
	private final DatabaseManager manager;
	private final Table<TestClass> table;

	@NoArgsConstructor
	@AllArgsConstructor
	private class TestClass implements DataClass<TestClass> {
		@Column(key = true)
		public ID id;

		@Column
		public String test;

		@NotNull
		@Override
		public Table<TestClass> getTable() {
			return table;
		}
	}

	public InsertTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/postgres", "postgres", "postgres");
		table = manager.getTable(TestClass.class, TestClass::new, "insert").createTable();
	}

	@Test
	public void insert() {
		var test = new TestClass();

		assertTrue(table.selectOne(Where.equals("id", test.id)).isEmpty());

		table.insert(test);
		assertThrows(ConflictException.class, () -> table.insert(test));

		assertTrue(table.selectOne(Where.equals("id", test.id)).isPresent());

		test.id = null;
		test.insert();
		assertThrows(ConflictException.class, test::insert);
	}

	@Test
	public void update() {
		var test = new TestClass();
		table.insert(test);

		assertNull(table.selectOne(Where.equals("id", test.id)).get().test);

		test.test = "abc";
		assertTrue(test.update());
		assertEquals(table.selectOne(Where.equals("id", test.id)).get().test, "abc");

		test.id = ID.generate();
		assertFalse(test.update());
	}
}
