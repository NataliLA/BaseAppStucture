package com.blogspot.nataliprograms.baseandroidapp.content.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}. Each
 * appended clause is combined using {@code AND}. This class is <em>not</em>
 * thread safe.
 */
public class SelectionBuilder {
	private String mTable = null;
	private Map<String, String> mProjectionMap = new HashMap<String, String>();
	private StringBuilder mSelection = new StringBuilder();
	private ArrayList<String> mSelectionArgs = new ArrayList<String>();

	private StringBuilder groupBy = new StringBuilder();

	/**
	 * Reset any internal state, allowing this builder to be recycled.
	 */
	public SelectionBuilder reset() {
		mTable = null;
		mSelection.setLength(0);
		mSelectionArgs.clear();
		groupBy = new StringBuilder();
		return this;
	}

	/**
	 * Append the given selection clause to the internal state. Each clause is
	 * surrounded with parenthesis and combined using {@code AND}.
	 */
	public SelectionBuilder where(String selection, String... selectionArgs) {
		if (TextUtils.isEmpty(selection)) {
			if (selectionArgs != null && selectionArgs.length > 0) {
				throw new IllegalArgumentException(
						"Valid selection required when including arguments=");
			}

			// Shortcut when clause is empty
			return this;
		}

		if (mSelection.length() > 0) {
			mSelection.append(" AND ");
		}

		mSelection.append("(").append(selection).append(")");
		if (selectionArgs != null) {
			Collections.addAll(mSelectionArgs, selectionArgs);
		}

		return this;
	}

	public SelectionBuilder groupBy(String fieldName, String tableName) {
		if (!TextUtils.isEmpty(this.groupBy)) {
			this.groupBy.append(", ").append(tableName).append(".")
					.append(fieldName);
		} else {
			this.groupBy.append(tableName).append(".").append(fieldName);
		}
		return this;
	}

	public SelectionBuilder groupBy(String fieldName) {
		if (!TextUtils.isEmpty(this.groupBy)) {
			this.groupBy.append(", ").append(fieldName);
		} else {
			this.groupBy.append(fieldName);
		}
		return this;
	}

	public SelectionBuilder table(String table) {
		mTable = table;
		return this;
	}

	private void assertTable() {
		if (mTable == null) {
			throw new IllegalStateException("Table not specified");
		}
	}

	public SelectionBuilder mapToTable(String column, String table) {
		mProjectionMap.put(column, table + "." + column);
		return this;
	}

	public SelectionBuilder map(String fromColumn, String toClause) {
		if (toClause == null) {
			toClause = "NULL";
		}
		if (mProjectionMap.containsKey(fromColumn)) {
			toClause = mProjectionMap.get(fromColumn);
		}
		mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
		return this;
	}

	public SelectionBuilder map(String fromColumn, String toClause,
			String... args) {
		for (int arg = 0; arg < args.length; arg++) {
			String argStr = new StringBuilder().append("'").append(args[arg])
					.append("'").toString();
			toClause = toClause.replaceFirst("\\?", argStr);
		}
		return map(fromColumn, toClause);
	}

	/**
	 * Return selection string for current internal state.
	 * 
	 * @see #getSelectionArgs()
	 */
	public String getSelection() {
		return mSelection.toString();
	}

	/**
	 * Return selection arguments for current internal state.
	 * 
	 * @see #getSelection()
	 */
	public String[] getSelectionArgs() {
		return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
	}

	private void mapColumns(String[] columns) {
		for (int i = 0; i < columns.length; i++) {
			final String target = mProjectionMap.get(columns[i]);
			if (target != null) {
				columns[i] = target;
			}
		}
	}

	@Override
	public String toString() {
		return "SelectionBuilder[table=" + mTable + ", selection="
				+ getSelection() + ", selectionArgs="
				+ Arrays.toString(getSelectionArgs()) + "]";
	}

	/**
	 * Execute query using the current internal state as {@code WHERE} clause.
	 */
	public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
		return query(db, columns, null, null, orderBy, null);
	}

	/**
	 * Execute query using the current internal state as {@code WHERE} clause.
	 */
	public Cursor query(SQLiteDatabase db, String[] columns, String groupBy,
			String having, String orderBy, String limit) {
		assertTable();
		String[] mappedColumns = null;
		if (columns != null) {
			mappedColumns = Arrays.copyOf(columns, columns.length);
			mapColumns(mappedColumns);
		}
		return db.query(mTable, mappedColumns, getSelection(),
				getSelectionArgs(), this.groupBy.toString(), having, orderBy,
				limit);
	}

	/**
	 * Execute update using the current internal state as {@code WHERE} clause.
	 */
	public int update(SQLiteDatabase db, ContentValues values) {
		assertTable();
		return db.update(mTable, values, getSelection(), getSelectionArgs());
	}

	/**
	 * Execute delete using the current internal state as {@code WHERE} clause.
	 */
	public int delete(SQLiteDatabase db) {
		assertTable();
		return db.delete(mTable, getSelection(), getSelectionArgs());
	}

	/**
	 * Build raw query represented as {@link String}
	 * 
	 * @return
	 */
	public String buildRawQuery(String[] columns, String orderBy) {
		StringBuilder query = new StringBuilder("SELECT ");

		String[] columnsCopy = Arrays.copyOf(columns, columns.length);

		mapColumns(columnsCopy);

		String[] mappedColumns = null;
		if (columns != null) {
			mappedColumns = Arrays.copyOf(columnsCopy, columns.length);
			mapColumns(mappedColumns);
		}

		for (int i = 0, length = mappedColumns.length; i < length; i++) {
			query.append(mappedColumns[i]);
			if (i < (length - 1)) {
				query.append(", ");
			}
		}

		query.append(" FROM ").append(mTable);
		String selection = mSelection.toString();

		if (!TextUtils.isEmpty(selection)) {
			query.append(" WHERE ");

			Iterator<String> argsIterator = mSelectionArgs.iterator();
			while (argsIterator.hasNext()) {
				String argStr = new StringBuilder().append("'")
						.append(argsIterator.next()).append("'").toString();
				selection = selection.replaceFirst("\\?", argStr);
			}
		}

		query.append(selection);
		if (groupBy.length() > 0) {
			query.append(" GROUP BY ").append(groupBy.toString());
		}

		if (!TextUtils.isEmpty(orderBy)) {
			query.append(" ORDER BY ").append(orderBy);
		}

		return query.toString();
	}

	public String buildRawQueryWithAsColumns(String orderBy) {
		StringBuilder query = new StringBuilder("SELECT ");

		List<String> columnName = new ArrayList<String>();
		List<String> columnAsValue = new ArrayList<String>();

		Set<String> keySet = mProjectionMap.keySet();

		for (String key : keySet) {
			columnName.add(key);
			columnAsValue.add(mProjectionMap.get(key));
		}

		String[] mappedColumns = new String[] {};
		mappedColumns = columnAsValue.toArray(mappedColumns);

		for (int i = 0, length = mappedColumns.length; i < length; i++) {
			query.append(mappedColumns[i]);
			if (i < (length - 1)) {
				query.append(", ");
			}
		}

		query.append(" FROM ").append(mTable);
		String selection = mSelection.toString();

		if (!TextUtils.isEmpty(selection)) {
			query.append(" WHERE ");

			Iterator<String> argsIterator = mSelectionArgs.iterator();
			while (argsIterator.hasNext()) {
				String argStr = new StringBuilder().append("'")
						.append(argsIterator.next()).append("'").toString();
				selection = selection.replaceFirst("\\?", argStr);
			}
		}

		query.append(selection);
		if (groupBy.length() > 0) {
			query.append(" GROUP BY ").append(groupBy.toString());
		}

		if (!TextUtils.isEmpty(orderBy)) {
			query.append(" ORDER BY ").append(orderBy);
		}

		return query.toString();
	}
}
