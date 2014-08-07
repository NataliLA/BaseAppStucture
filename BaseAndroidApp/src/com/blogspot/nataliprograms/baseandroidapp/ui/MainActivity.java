package com.blogspot.nataliprograms.baseandroidapp.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.nataliprograms.baseandroidapp.R;
import com.blogspot.nataliprograms.baseandroidapp.content.provider.BaseAppContract.TestTable;
import com.blogspot.nataliprograms.baseandroidapp.content.provider.BaseAppContract.TestTableColumns;

public class MainActivity extends BaseActivity {
	
	private static final String TAG = MainActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_main);
		
		doStartService(new Intent());
 
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		ContentValues values = new ContentValues();
		values.put(TestTableColumns.TEST_ID, 1);
		values.put(TestTableColumns.TEST_TEST, "Test text");
		getContentResolver().insert(TestTable.CONTENT_URI, values);
		
		Cursor result = getContentResolver().query(TestTable.CONTENT_URI, new String[]{"*"}, null, null, null);
		Log.d(TAG, "Result cursor = "+DatabaseUtils.dumpCursorToString(result));
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
