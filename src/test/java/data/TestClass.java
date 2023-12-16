package data;

import de.mineking.javautils.ID;
import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class TestClass implements DataClass<TestClass> {
	private final Table<TestClass> table;

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

	public TestClass(Table<TestClass> table) {
		this.table = table;
	}

	public TestClass(Table<TestClass> table, TE e, EnumSet<TE> enums) {
		this.table = table;
		this.enumTest = e;
		this.enums = enums;
	}

	@Override
	public String toString() {
		return id.asString() + ": " + enumTest + ", " + enums + " (" + (id.getTimeCreated().toEpochMilli() - System.currentTimeMillis()) + ")";
	}
}
