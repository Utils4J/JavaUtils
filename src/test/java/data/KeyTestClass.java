package data;

import de.mineking.javautils.database.Column;

public class KeyTestClass {
	@Column(autoincrement = true, key = true)
	public int id;

	@Column
	public String text;

	public KeyTestClass( String text) {
		id = 0;
		this.text = text;
	}

	public KeyTestClass() {
		this(null);
	}

	@Override
	public String toString() {
		return id + ": " + text;
	}
}
