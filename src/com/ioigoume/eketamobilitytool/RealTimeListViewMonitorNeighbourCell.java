package com.ioigoume.eketamobilitytool;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.customAdapterPhone.phoneListCustomAdapter;


public class RealTimeListViewMonitorNeighbourCell extends Activity{
	public static final String TAG = "RealTimeListViewMonitor";
	private TelephoneClass phoneData = null;
	private MobilityToolApplication myapp = null;
	private ListView mylistView = null;
	private phoneListCustomAdapter lvAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the ListView Title
		setTitle("Neighbour Cells List");
		// Initialize the list view object
		mylistView = new ListView(this);
		mylistView.setAdapter(null);
		// Here i define the listview divider
		mylistView.setDivider(new ColorDrawable(this.getResources().getColor(R.color.lightBlue)));
		mylistView.setDividerHeight(1);
		// Background color
		mylistView.setBackgroundColor(this.getResources().getColor(R.color.black));
		
		// Get my wifi class through the shared
		myapp = (MobilityToolApplication) RealTimeListViewMonitorNeighbourCell.this.getApplication();
		// The object that will be returned might be null if the wificlass has not
		// setted the reference of the object
		phoneData = myapp.getPreferencesObjNonXml().getPhoneObj();
		if(phoneData == null){
			Toast.makeText(this, "Start the service and then retry.", Toast.LENGTH_LONG).show();
			// Put it in here so the screen that is returned is black
			setContentView(mylistView);
			return;
		}
		
		lvAdapter = new phoneListCustomAdapter();
		lvAdapter.setNeightbourCellList(phoneData.getnCellList());
		lvAdapter.setContext(RealTimeListViewMonitorNeighbourCell.this);
		mylistView.setAdapter(lvAdapter);
		lvAdapter.notifyDataSetChanged();
		setContentView(mylistView);
		
	}	
}

