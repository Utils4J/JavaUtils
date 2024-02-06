package database;

import de.mineking.javautils.ID;
import de.mineking.javautils.database.*;
import de.mineking.javautils.database.exception.ConflictException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsertTest {
	private final DatabaseManager manager;
	private final Table<TestClass> table;

	@ToString
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
		//Reset table for clean test
		table.deleteAll();

		var test = new TestClass();

		assertTrue(table.selectOne(Where.equals("id", test.id)).isEmpty());

		table.insert(test);
		assertThrows(ConflictException.class, () -> table.insert(test));

		assertTrue(table.selectOne(Where.equals("id", test.id)).isPresent());

		test.id = null;
		test.insert();
		assertThrows(ConflictException.class, test::insert);
	}
}
