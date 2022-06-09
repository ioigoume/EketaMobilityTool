package com.ioigoume.eketamobilitytool;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.util.Log;
import de.quist.app.errorreporter.ReportingIntentService;

public class AsyncOmlTextProtocolPushsService extends ReportingIntentService{

	public static final String TAG = AsyncOmlTextProtocolPushsService.class.getSimpleName();
	
	
	static final String EXTRA_STACK_TRACE = AsyncOmlTextProtocolPushsService.class.getPackage().getName().concat(".extraStackTrace");
	static final String ACTION_SEND_REPORT = AsyncOmlTextProtocolPushsService.class.getPackage().getName().concat(".actionOMLPush");
	/**
	 * Used internally to count retries.
	 */
	private static final String EXTRA_CURRENT_RETRY_COUNT = AsyncOmlTextProtocolPushsService.class.getPackage().getName().concat(".extraCurrentRetryCount");

	/**
	 * The default maximum backoff exponent.
	 */
	static final int DEFAULT_MAXIMUM_BACKOFF_EXPONENT = 12;

	/**
	 * The default maximum number of tries to send a report. This value results in a retry
	 * time of about 8 hours with an unchanged retry count.
	 */
	static final int DEFAULT_MAXIMUM_RETRY_COUNT = DEFAULT_MAXIMUM_BACKOFF_EXPONENT + 40;
	
	/**
	 * Maximum number of tries to send a report. Default is {@value #DEFAULT_MAXIMUM_RETRY_COUNT}.
	 */
	private static final String META_DATA_MAXIMUM_RETRY_COUNT = AsyncOmlTextProtocolPushsService.class.getPackage().getName().concat(".maximumRetryCount");
	private static final String META_DATA_MAXIMUM_BACKOFF_EXPONENT = AsyncOmlTextProtocolPushsService.class.getPackage().getName().concat(".maximumBackoffExponent");
	
	
	
	private ArrayList<String[]> wifi_tuples = null;
	private ArrayList<String[]> phone_tuples = null;
	
	public AsyncOmlTextProtocolPushsService() {
		super(AsyncOmlTextProtocolPushsService.class.getSimpleName());

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			wifi_tuples = (ArrayList<String[]>) intent.getSerializableExtra("wifi_tuples");
			phone_tuples = (ArrayList<String[]>) intent.getSerializableExtra("phone_tuples");
			
			if(wifi_tuples == null && phone_tuples == null)
				return;
			
			sendReport(intent);
		} catch (ClassCastException e){
			Log.e(TAG, "The extra you received is not casted to the right type");
		} catch (Exception e) {
			// Catch all other exceptions as otherwise they would create an endless loop
			Log.e(TAG, "Error while sending data to server", e);
		}		
	}
	
	
	/**
	 * @param intent
	 * @throws UnsupportedEncodingException
	 * @throws NameNotFoundException
	 */
	private void sendReport(Intent intent) throws UnsupportedEncodingException, NameNotFoundException{
		Log.v(TAG,"Got request to push to OML Server:" + intent.toString());
		
		String stacktrace = intent.getStringExtra(EXTRA_STACK_TRACE);
		MobilityToolApplication myapp = (MobilityToolApplication)AsyncOmlTextProtocolPushsService.this.getApplication();
		
		int maximumRetryCount = getMaximumRetryCount();
		int maximumExponent = getMaximumBackoffExponent();
		
		
		// Close everything
		myapp.terminateOmlObject();
		// Try to reconnect
		if(myapp.getpreferencesOmlObject().getOmlObj().isSockOpen()){
			Log.v(TAG, "Head Injected");
			if(phone_tuples != null){
				// Inject the phone tuples
				myapp.getpreferencesOmlObject().getOmlObj().inject_mass(TelephoneInterface.DATABASE_TABLE, phone_tuples);
				Log.v(TAG,"Phone Tuples Injected");
			}
			if(wifi_tuples != null){
				// Inject the wifi tuples
				myapp.getpreferencesOmlObject().getOmlObj().inject_mass(WifiInterface.DATABASE_TABLE_WIFI, wifi_tuples);
				Log.v(TAG,"Wifi Tuples Injected");
			}
			
			myapp.terminateOmlObject();
			
		}else{
			// Retry at a later point in time
			AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
			int exponent = intent.getIntExtra(EXTRA_CURRENT_RETRY_COUNT, 0);
			intent.putExtra(EXTRA_CURRENT_RETRY_COUNT, exponent + 1);
			
			PendingIntent operation = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			if (exponent >= maximumRetryCount) {
				// Discard error
				Log.w(TAG, "Error report reached the maximum retry count and will be discarded.\nStacktrace:\n"+stacktrace);
				return;
			}
			if (exponent > maximumExponent) {
				exponent = maximumExponent;
			}
			long backoff = (1 << exponent) * 1000; // backoff in ms
			alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + backoff, operation);
		}
	}
	
	
	public int getMaximumRetryCount() throws NameNotFoundException {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			return ai.metaData.getInt(META_DATA_MAXIMUM_RETRY_COUNT, DEFAULT_MAXIMUM_RETRY_COUNT);
		} catch (NameNotFoundException e) {
			// Should never happen
			throw e;
		} 
	}

	public int getMaximumBackoffExponent() throws NameNotFoundException {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			return ai.metaData.getInt(META_DATA_MAXIMUM_BACKOFF_EXPONENT, DEFAULT_MAXIMUM_BACKOFF_EXPONENT);
		} catch (NameNotFoundException e) {
			// Should never happen
			throw e;
		} 
	}

}
