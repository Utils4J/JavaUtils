package de.mineking.javautils.database.type;

import org.jetbrains.annotations.NotNull;

public enum PostgresType implements DataType {

	/**
	 * signed eight-byte integer
	 */
	BIG_INT("bigint"),
	/**
	 * 	autoincrementing eight-byte integer
	 */
	BIG_SERIAL("bigserial"),
	/**
	 * 	fixed-length bit string
	 */
	BIT("bit"),
	/**
	 * variable-length bit string
	 */
	VAR_BIT("varbit"),
	/**
	 * logical Boolean (true/false)
	 */
	BOOLEAN("boolean"),
	/**
	 * rectangular box on a plane
	 */
	BOX("box"),
	/**
	 * 	binary data (“byte array”)
	 */
	BYTE_ARRAY("bytea"),
	/**
	 * 	fixed-length character string
	 */
	CHARACTER("character"),
	/**
	 * variable-length character string
	 */
	VAR_CHAR("varchar"),
	/**
	 * IPv4 or IPv6 network address
	 */
	CIDR("cidr"),
	/**
	 * circle on a plane
	 */
	CIRCLE("circle"),
	/**
	 * calendar date (year, month, day)
	 */
	DATE("date"),
	/**
	 * double precision floating-point number (8 bytes)
	 */
	DOUBLE_PRECISION("float8"),
	/**
	 * IPv4 or IPv6 host address
	 */
	INET("inet"),
	/**
	 * 	signed four-byte integer
	 */
	INTEGER("integer"),
	/**
	 * time span
	 */
	INTERVAL("interval"),
	/**
	 * 	textual JSON data
	 */
	JSON("json"),
	/**
	 * binary JSON data, decomposed
	 */
	JSONB("jsonb"),
	/**
	 * infinite line on a plane
	 */
	LINE("line"),
	/**
	 * line segment on a plane
	 */
	LSEG("lseg"),
	/**
	 * 	MAC (Media Access Control) address
	 */
	MAC_ADDR("macaddr"),
	/**
	 * MAC (Media Access Control) address (EUI-64 format)
	 */
	MAC_ADDR_8("macaddr8"),
	/**
	 * currency amount
	 */
	MONEY("money"),
	/**
	 * exact numeric of selectable precision
	 */
	NUMERIC("numeric"),
	/**
	 * 	geometric path on a plane
	 */
	PATH("path"),
	/**
	 * PostgreSQL Log Sequence Number
	 */
	PG_LSN("pg_lsn"),
	/**
	 * user-level transaction ID snapshot
	 */
	PG_SNAPSHOT("pg_snapshot"),
	/**
	 * geometric point on a plane
	 */
	POINT("point"),
	/**
	 * closed geometric path on a plane
	 */
	POLYGON("polygon"),
	/**
	 * single precision floating-point number (4 bytes)
	 */
	REAL("real"),
	/**
	 * 	signed two-byte integer
	 */
	SMALL_INT("smallint"),
	/**
	 * autoincrementing two-byte integer
	 */
	SMALL_SERIAL("smallserial"),
	/**
	 * autoincrementing four-byte integer
	 */
	SERIAL("serial"),
	/**
	 * variable-length character string
	 */
	TEXT("text"),
	/**
	 * time of day (no time zone)
	 */
	TIME("time"),
	/**
	 * 	time of day, including time zone
	 */
	TIMETZ("timetz"),
	/**
	 * 	date and time (no time zone)
	 */
	TIMESTAMP("timestamp"),
	/**
	 * date and time, including time zone
	 */
	TIMESTAMPTZ("timestamptz"),
	/**
	 * text search query
	 */
	TSQUERY("tsquery"),
	/**
	 * text search document
	 */
	TSVECTOR("tsvector"),
	/**
	 * user-level transaction ID snapshot (deprecated; see pg_snapshot)
	 */
	TXID_SNAPSHOT("txid_snapshot"),
	/**
	 * 	universally unique identifier
	 */
	UUID("uuid"),
	/**
	 * XML data
	 */
	XML("xml");




	private final String name;

	PostgresType(String name) {
		this.name = name;
	}

	@NotNull
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
