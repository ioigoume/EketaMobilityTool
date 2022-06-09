package com.ioigoume.eketamobilitytool;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	private EditTextPreference samplingText;
	static private String TAG = "prefsActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Adds the xml preference
		Log.d(TAG, "preference activity");
		addPreferencesFromResource(R.xml.prefs);
		
		// Retrieve the preference item
		samplingText = (EditTextPreference)findPreference(getString(R.string.key_samplingItem));
		
		// Set the keyboard to appear when i want to input text
		samplingText.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		// Disable list pref when service is running
		if(!isServiceRunning(("." + BackgroundService.class.getSimpleName()).trim())){
			ListPreference listpref = (ListPreference)findPreference(getString(R.string.key_serverUploadProcess));
			listpref.setSelectable(true);
		}else{
			ListPreference listpref = (ListPreference)findPreference(getString(R.string.key_serverUploadProcess));
			listpref.setSelectable(false);
		}
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		/**
		 * 	CHECK IF THE TIME SAMPLING INTERVAL IS SETTED SUCCESSFULLY
		 */
		if(key == getString(R.string.key_samplingItem))
		{
			int myValue = Integer.parseInt(sharedPreferences.getString(key, "0"));
			
			Log.d(TAG, "Sampling interval changed.");
			
			
			if(myValue < 2000){
				Toast.makeText(this, "Duration must be over 2000(ms)\nYour time is set to 5s by default.", Toast.LENGTH_LONG).show();
				Preference pref = findPreference(key);
				SharedPreferences.Editor editor = pref.getSharedPreferences().edit();
				editor.putString(key, "5000");
				editor.commit();
			}
			else{
				Toast.makeText(this, "Time is set successfully", Toast.LENGTH_LONG).show();
			}
		}
		
		/**
		 * 	CHECK IF THE PUSHING TO SERVER PROCESS IS SETTED SUCCESFULLY
		 */
		if(key == getString(R.string.key_serverUploadProcess)){
			int myValue = Integer.parseInt(sharedPreferences.getString(key, "1"));
			if(myValue == 1){
				// Send Broadcast of the the change of the server
				Toast.makeText(this, "Upload right after interface sampling.", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Upload in the end of the monitoring.", Toast.LENGTH_LONG).show();
			}
			
		}
		
		/**
		 * CHECK IF THE SERVER IS SETTED SUCCESFULLY
		 */
		
		if(key == getString(R.string.prefs_key_ChooseServer)){
			int myValue = Integer.parseInt(sharedPreferences.getString(key, "1"));
			if(myValue == 1){
				// Send Broadcast of the the change of the server
				Intent intentMsg = new Intent();
				intentMsg.setAction("com.ioigoume.eketamobilitytool.NOSERVER");
				sendBroadcast(intentMsg);
				
				Toast.makeText(this, "No Server was chosen.", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Server was chosen successfully", Toast.LENGTH_LONG).show();
				// Send Broadcast of the the change of the server
				Intent intentMsg = new Intent();
				intentMsg.setAction("com.ioigoume.eketamobilitytool.SERVERON");
				sendBroadcast(intentMsg);
			}
		}
		
		/**
		 * 	CHECK IF THE UPLOADING INTERFACES IS CHOOSEN SUCCESSFULLY
		 */
		
		if(key == getString(R.string.key_upload)){
			int myValue = Integer.parseInt(sharedPreferences.getString(key, "1"));
			if(myValue == 1){
				// Send Broadcast of the the change of the server
				Toast.makeText(this, "Upload only through wifi.", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Upload through any available interface.", Toast.LENGTH_LONG).show();
			}
		}
		
		/**
		 * CHECK IF THE PERSONAL INFO IS CHANGED
		 */
		if(key == getString(R.string.prefs_key_PersonalInfoName)){
			String myValue = sharedPreferences.getString(key, "none");
			if(myValue != "none"){
				Toast.makeText(this, "Name changed to: " + myValue, Toast.LENGTH_LONG).show();
			}
		}
		if(key == getString(R.string.prefs_key_PersonalInfoSurname)){
			String myValue = sharedPreferences.getString(key, "none");
			if(myValue != "none"){
				Toast.makeText(this, "Surname changed to: " + myValue, Toast.LENGTH_LONG).show();
			}
		}
		
		/**
		 * CHECK IF THE STORE RADIO BUTTON IS CHANGED
		 */
		if(key == getString(R.string.prefs_key_BooleanStoreIfGpsExists)){
			boolean myValue = sharedPreferences.getBoolean(key, false);
			if(myValue == false){
				Toast.makeText(this, "Measurements are not going to be saved in absence of GPS.", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Measurements will be saved even with no GPS data.", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	
	// --- SERVICE CHECK CONTROL USING THE SYSTMEM
	// Check if the service is running
	public boolean isServiceRunning(String serviceName) {
		boolean serviceRunning = false;
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
		Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
		while (i.hasNext()) {
			ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i.next();
			if (runningServiceInfo.service.getShortClassName().equals(serviceName)) {
				serviceRunning = true;
			}
		}
		return serviceRunning;
	}
}
