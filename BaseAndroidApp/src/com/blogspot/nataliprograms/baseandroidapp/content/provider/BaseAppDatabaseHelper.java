package com.blogspot.nataliprograms.baseandroidapp.content.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.blogspot.nataliprograms.baseandroidapp.R;
import com.blogspot.nataliprograms.baseandroidapp.util.DBUtil;

public class BaseAppDatabaseHelper extends SQLiteOpenHelper {
	
	public static final String FROM = " FROM ";
	public static final String SELECT = " SELECT ";
	public static final String EQ = " = ";
	public static final String AND = " AND ";
	public static final String OR = " OR ";
	public static final String WHERE = " WHERE ";
	public static final String NOT_EQ = " <> ";
	public static final String ON = " ON ";
	public static final String LEFT_OUTER_JOIN = " LEFT OUTER JOIN ";
	public static final String JOIN = " JOIN ";
	public static final String INNER_JOIN = " INNER JOIN ";
	public static final String IN = " IN ";
	public static final String AS = " AS ";
	public static final String NOT_IN = " NOT IN ";
	public static final String IS = " IS ";
	public static final String NULL = " NULL ";
	public static final String NOT_NULL = " NOT NULL ";
	
	/**
	 * Database name.
	 */
	public static final String DATABASE_NAME = "base_app_database.db";
	/**
	 * Database version.
	 */
	public static final int DATABASE_VERSION = 1;
	
	private Context context;

	public BaseAppDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		DBUtil.executeScriptFromResources(db, context, R.raw.db_create);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		if (oldVersion == 1 && newVersion == 2) {
//			DBUtil.executeScriptFromResources(db, context,
//					R.raw.db_upgrade_v1_v2);
//		}
	}

}
