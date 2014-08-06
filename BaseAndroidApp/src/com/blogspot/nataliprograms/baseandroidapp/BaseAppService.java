package com.blogspot.nataliprograms.baseandroidapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.greenrobot.event.EventBus;

public class BaseAppService extends Service {
	
	private static final String TAG = BaseAppService.class.getName();

	private LocalBinder localBinder;

	private EventBus bus;

	@Override
	public void onCreate() {
		localBinder = new LocalBinder();
		bus = EventBus.getDefault();
		bus.register(this);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return localBinder;
	}

	public class LocalBinder extends Binder {
		public BaseAppService getService() {
			return BaseAppService.this;
		}
	}
	
	public void onEventMainThread(Object event) {
		Log.v(TAG, "event = " + event);
	}
}
