package com.blogspot.nataliprograms.baseandroidapp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class DBUtil {
	
	private static final String TAG = "DBUtils";

	protected static final boolean LOGD = Log.isLoggable(TAG, Log.DEBUG);
	protected static final boolean LOGE = Log.isLoggable(TAG, Log.ERROR);
	protected static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	protected static final boolean LOGI = Log.isLoggable(TAG, Log.INFO);
	
	public static boolean executeScriptFromResources(SQLiteDatabase db,
			Context context, Object... scriptResources) {
		Collection<String> scripts = new ArrayList<String>();
		for (Object scriptRes : scriptResources) {
			scripts.addAll(readDatabaseScript(context, (Integer) scriptRes));
		}
		executeMultipleSQL(db, scripts);
		return true;
	}
	
	/**
	 * The method to execute package of SQL scripts.
	 * 
	 * @param db
	 *            - SQLite database
	 * @param scripts
	 *            - array with SQL scripts lines
	 */
	public static void executeMultipleSQL(SQLiteDatabase db,
			Collection<String> scripts) {
		for (String singleScript : scripts) {
			if (!TextUtils.isEmpty(singleScript)) {
				if (LOGD) {
					Log.d(TAG, "prepare to execute script");
				}
				db.execSQL(singleScript);
				if (LOGD) {
					Log.d(TAG, "executed: " + singleScript);
				}
			}
		}
	}
	
	/**
	 * The method to read file with SQL scripts
	 * 
	 * @param fileID
	 *            - raw file resource identifier
	 * @return - array with file content
	 * @throws IOException
	 */
	public static Collection<String> readDatabaseScript(Context context,
			int fileID) {
		Collection<String> sqlScripts = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			InputStream is = context.getResources().openRawResource(fileID);
			reader = new BufferedReader(new InputStreamReader(is));
			sqlScripts.addAll(readLines(reader));
		} catch (IOException e) {
			if (LOGE) {
				Log.e(TAG, "Error by reading db scripts", e);
			}
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				if (LOGE) {
					Log.e(TAG, "Error closing reader", ex);
				}
			}
		}
		return sqlScripts;
	}

	public static List<String> readLines(BufferedReader reader)
			throws IOException {
		StringBuffer sb = null;
		List<String> lines = new Vector<String>();
		String line = reader.readLine();
		while (line != null) {
			line = trimComment(line, 0);
			line = line.trim();
			boolean finished = checkFinished(line);

			if (finished) {
				if (sb != null) {
					sb.append(" ");
					sb.append(line);
					line = sb.toString();
					sb = null;
				}

				int pos = line.lastIndexOf(END_LINE);
				if (pos >= 0) {
					line = line.substring(0, pos);
				}

				if ((!line.startsWith(COMMENT_START)) && (line.length() > 0)) {
					if (LOGD) {
						Log.d(TAG, "Script line: " + line);
					}
					lines.add(line);
				}
			} else {
				if (sb == null) {
					sb = new StringBuffer(256);
				} else {
					sb.append(" ");
				}
				sb.append(line);
			}

			line = reader.readLine();
		}
		return lines;
	}
	
	private static final char END_LINE = ';';
	static final String COMMENT_START = "--";

	static boolean checkFinished(String s) {
		if (TextUtils.isEmpty(s)) {
			return true;
		}
		return s.endsWith(";");
	}

	static String trimComment(String line, int fromIndex) {
		int commentIndex = line.indexOf(COMMENT_START, fromIndex);
		if (commentIndex != -1) {
			String commentSuspect = line.substring(0, commentIndex);
			if (checkIsComment(commentSuspect)) {
				return line = commentSuspect;
			} else {
				return trimComment(line, commentIndex + COMMENT_START.length());
			}

		} else {
			return line;
		}
	}
	
	/**
	 * @param line
	 * @param indexOfMinusMinus
	 * @return
	 */
	static boolean checkIsComment(String line) {
		return (countOccurrencesOf(line, "'") % 2) == 0;
	}

	private static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0
				|| sub.length() == 0) {
			return 0;
		}
		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}
	
	public static void safeCloseCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}
}
