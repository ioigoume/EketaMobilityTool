package com.ioigoume.eketamobilitytool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	
	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Log.d("BootReceiver", context.getString(R.string.prefs_key_startAtBoot));
		if(prefs.getBoolean(context.getString(R.string.prefs_key_startAtBoot), false))
		{
			Intent bootStart = new Intent(context, BackgroundService.class);
			bootStart.putExtra(context.getString(R.string.intent_extras_name_bootReceiver), true);
			context.startService(bootStart);
			
		}
	}

}
