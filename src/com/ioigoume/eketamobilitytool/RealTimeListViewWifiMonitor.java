package com.ioigoume.eketamobilitytool;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.customAdapterWifi.wifiListCustomAdapter;


public class RealTimeListViewWifiMonitor extends Activity implements ApplicationGlobalVars{
	public static final String TAG = "RealTimeListViewMonitor";
	private wifiClass wifidata = null;
	private MobilityToolApplication myapp = null;
	private ListView mylistView = null;
	private wifiListCustomAdapter lvAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Context ctx = RealTimeListViewWifiMonitor.this;
		// Set the ListView Title
		setTitle("Wifi Access Points List");
		// Initialize the list view object
		mylistView = new ListView(ctx);
		mylistView.setAdapter(null);
		// Here i define the listview divider
		mylistView.setDivider(new ColorDrawable(this.getResources().getColor(R.color.lightBlue)));
		mylistView.setDividerHeight(1);
		// Background color
		mylistView.setBackgroundColor(this.getResources().getColor(R.color.black));
		
		// Get my wifi class through the shared
		myapp = (MobilityToolApplication) RealTimeListViewWifiMonitor.this.getApplication();
		
		// The object that will be returned might be null if the wificlass has not
		// setted the reference of the object
		wifidata = myapp.getPreferencesObjNonXml().getWifiObj();
		if(wifidata == null || wifidata.getAccessPointList() == null){
			Toast.makeText(this, "Start the service and then retry.", Toast.LENGTH_LONG).show();
			// Put it in here so the screen that is returned is black
			setContentView(mylistView);
			return;
		}
		
		lvAdapter = new wifiListCustomAdapter(ctx, wifidata.getAccessPointList());
		myapp.getPreferencesObjNonXml().setWifiAdaptOb(lvAdapter);
		mylistView.setAdapter(lvAdapter);
		setContentView(mylistView);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	
	}
}
