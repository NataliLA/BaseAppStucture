package com.blogspot.nataliprograms.baseandroidapp.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.blogspot.nataliprograms.baseandroidapp.BaseAppService;
import com.blogspot.nataliprograms.baseandroidapp.events.ServiceConnectionEvent;

import de.greenrobot.event.EventBus;

public class BaseActivity extends FragmentActivity {

	protected static String TAG = BaseActivity.class.getSimpleName();
	private static final String ServiceIntent = "com.blogspot.nataliprograms.baseandroidapp.BaseAppService";

	protected BaseAppService service;
	private ServiceConnection serviceConnection;
	protected EventBus bus;
	
	private boolean isBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TAG = this.getClass().getSimpleName();
		bus = EventBus.getDefault();
		bus.register(this);
		Log.v(TAG, "service = " + service);
		if (service == null) { 
			serviceConnection = new ServiceConnection() {

				@Override
				public void onServiceDisconnected(ComponentName name) {
					Log.v(TAG, "onServiceDisconnected");
					// TODO
				}

				@Override
				public void onServiceConnected(ComponentName name,
						IBinder binder) {
					Log.v(TAG, "onServiceConnected");
					service = ((BaseAppService.LocalBinder) binder)
							.getService();
					serviceConnected(service);
					bus.post(new ServiceConnectionEvent(service));
				}
			};
		} else {
			bus.post(new ServiceConnectionEvent(service));
		}
		doBindService();
	}

	protected void serviceConnected(BaseAppService service) {
		Log.v(TAG, "serviceConnected");
	}

	@Override
	protected void onDestroy() {
		bus.unregister(this);
		doUnbindService();
		super.onDestroy();
	}

	protected void doStartService(Intent intent) {
		Log.v(TAG, "starting service");
		Intent servIntent = new Intent(ServiceIntent);
		servIntent.putExtras(intent);
		startService(servIntent);
	}

	protected void doStopService() {
		Log.v(TAG, "shutting down service");
		Intent servIntent = new Intent(ServiceIntent);
		doUnbindService();
		stopService(servIntent);
	}

	protected void doBindService() {
		Log.v(TAG, "doBindService isBound = " + isBound);
		if (!isBound) {
			Log.v(TAG, "binding service");
			Intent servIntent = new Intent(ServiceIntent);
			isBound = bindService(servIntent, serviceConnection, 0);
			if (!isBound) {
				Log.e(TAG, "service did not bind.");
			}
		}
	}

	protected void doUnbindService() {
		Log.v(TAG, "doUnbindService isBound = " + isBound);
		if (isBound) {
			Log.v(TAG, "unbinding service ");
			isBound = false;
			unbindService(serviceConnection);
		}
	}

	public void onEventMainThread(ServiceConnectionEvent event) {
		Log.v(TAG, "event = " + event);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
}
