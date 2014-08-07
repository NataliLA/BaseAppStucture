package com.blogspot.nataliprograms.baseandroidapp.content.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.blogspot.nataliprograms.baseandroidapp.content.provider.BaseAppContract.Tables;

public class BaseAppContentProvider extends ContentProvider {

	private static final String TAG = BaseAppContentProvider.class
			.getSimpleName();

	private Context mContext;
	private BaseAppDatabaseHelper mDbHelper;
	private UriMatcher mUriMatcher;

	private final static int TESTTABLE = 100;

	{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(BaseAppContract.CONTENT_AUTHORITY, Tables.TEST_TABLE, TESTTABLE);
//		mUriMatcher.addURI(BaseAppContract.CONTENT_AUTHORITY,
//				Tables.TEST_TABLE + "/"
//						+ BaseAppContract.TEST_PATH,
//				TESTPATHCODE);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String tableName = null;

		switch (mUriMatcher.match(uri)) {
		case TESTTABLE:
			tableName = Tables.TEST_TABLE;
			break;
        default:
			throw new UnsupportedOperationException(
					"Unknown URI while deleting : " + uri);
		}

		int count = db.delete(tableName, selection, selectionArgs);
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case TESTTABLE:
			return BaseAppContract.TestTable.CONTENT_TYPE;
		default:
			throw new UnsupportedOperationException(
					"Unknown URI while checking MIME type : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		Log.d(TAG,
				"insert(uri=" + uri + ", values: " + contentValues.toString()
						+ ")");

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String tableName = null;

		switch (mUriMatcher.match(uri)) {
		case TESTTABLE:
			tableName = Tables.TEST_TABLE;
			break;
		default:
			throw new UnsupportedOperationException(
					"Unknown URI while inserting : " + uri);
		}

		long rowId = db.insert(tableName, null, contentValues);
		if (rowId > 0) {
			Uri resultUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return resultUri;
		} else {
			return null;
		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		Log.d(TAG, "bulkInsert(uri=" + uri + ", values: " + values.toString()
				+ ")");

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String tableName = null;
		int numInserted = 0;

		switch (mUriMatcher.match(uri)) {
		case TESTTABLE:
			tableName = Tables.TEST_TABLE;
			break;
		}

		db.beginTransaction();
		try {
			for (ContentValues cv : values) {
				long newID = db.insertOrThrow(tableName, null, cv);
				if (newID <= 0) {
					throw new UnsupportedOperationException(
							"Failed to insert row into " + uri);
				}
			}
			db.setTransactionSuccessful();
			getContext().getContentResolver().notifyChange(uri, null);
			numInserted = values.length;
		} finally {
			db.endTransaction();
		}
		return numInserted;
	}

	@Override
	public boolean onCreate() {
		this.mContext = getContext();

		this.mDbHelper = new BaseAppDatabaseHelper(getContext(),
				BaseAppDatabaseHelper.DATABASE_NAME, null,
				BaseAppDatabaseHelper.DATABASE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		final SelectionBuilder builder = buildSelection(uri).where(selection,
				selectionArgs);
		Cursor cursor = builder.query(db, projection, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
		Log.d(TAG, "query = " + builder.toString());
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if ((values == null)) {
			values = new ContentValues();
		}
		Log.d(TAG, "update(uri=" + uri + ", values: " + values.toString() + ")");

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String tableName = null;

		switch (mUriMatcher.match(uri)) {
		case TESTTABLE:
			tableName = Tables.TEST_TABLE;
			break;
		default:
			throw new UnsupportedOperationException("Unknown URI : " + uri);
		}

		int countUpdatedRows = db.update(tableName, values, selection,
				selectionArgs);
		if (countUpdatedRows > 0) {
			mContext.getContentResolver().notifyChange(uri, null);
		}

		return countUpdatedRows;
	}

	private SelectionBuilder buildSelection(Uri uri) {
		String id = null;
		final SelectionBuilder builder = new SelectionBuilder();
		Log.v(TAG, "select uri = " + uri);
		Log.v(TAG, "mUriMatcher.match(uri) = " + mUriMatcher.match(uri));
		switch (mUriMatcher.match(uri)) {
		case TESTTABLE:
			return builder.table(Tables.TEST_TABLE);
		default:
			throw new UnsupportedOperationException(
					"Unknown URI while executing query : " + uri);
		}
	}
}
