package com.ioigoume.eketamobilitytool;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.ioigoume.eketamobilitytool.database.DBAdapter;

public class MapViewActivity extends MapActivity{

	private MapView mymap;
	private int map_view_select = -1;
	private MapController mycontroller;
	private Drawable mydrawable;
	private MyItemizedOverlay myOverlay;
	private DBAdapter db = null;
	private ProgressDialog progress = null;
	private OverlayItem overlayitem = null;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// On destroy close the database
		db.close();
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		
		/************ DATA FROM INTENT *************/
		Intent dataFromMenu = getIntent();
		// map_view_select : 0 -> phone
		// map_view_select : 1 -> wifi
		map_view_select = Integer.parseInt(dataFromMenu.getAction().toString());
		
		/************ INITIALIZATIONS **************/
		// Create an object of the database
		db = new DBAdapter(MapViewActivity.this);
		try{
			db.open();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			Toast.makeText(this, "Database could not be opened.Retry.", Toast.LENGTH_LONG).show();
			return;
		}

		/************ Draw the map *****************/

		// Puts the map on the screen
		mymap = (MapView) findViewById(R.id.mapview);
		// Puts the zoom control tool on the screen
		mymap.setBuiltInZoomControls(true);
		

		/********** Draw on the map *****************/

		// Get the tool to control my map
		mycontroller = mymap.getController();
		
		// Se the satellite
		mymap.setSatellite(true);
		// Clear any previous overlays
		mymap.getOverlays().clear();
				
		// The pin tha will be drawed on the map
		//mydrawable = this.getResources().getDrawable(R.drawable.pin_red);
		// Create the overlay object
		//myOverlay = new MyItemizedOverlay(mydrawable);
		try{
			progress = ProgressDialog.show(MapViewActivity.this, "Wait!", "MapView is loading...", true, false);
			MapThread mythr = new MapThread("MapView Thread");
			mythr.start();
		} catch(Exception e){
			if(progress != null && progress.isShowing())
				progress.dismiss();
			// Close the database;
			db.close();
			Toast.makeText(MapViewActivity.this, "MapView could not be created.",Toast.LENGTH_LONG).show();
		}		
	}
	
	
	
	

	// Start tracking my position on the map for phone interface class
	private void updateMapView_phone(final String table_name, final String[] headers) {
		
		Log.d("Num OF Entries",String.valueOf(db.numofentries(table_name)));
		
				
		if(db.numofentries(table_name) > 0)
		{
			final Cursor c = db.getAllRecords(headers, table_name);
			int i = 1;
			int lat = 0;
			int log = 0;
			int rssi;
			boolean have_pin = false;
			String new_str[] = new String[3];
			new_str[0] = "";
			new_str[1] = "";
			new_str[2] = "false";
					
			if(c.moveToFirst())
			{
				do{
					try{
						lat = Integer.parseInt(c.getString(c.getColumnIndex(TelephoneInterface.KEY_LATITUNDE)));
						log = Integer.parseInt(c.getString(c.getColumnIndex(TelephoneInterface.KEY_LONGTITUDE)));
						rssi = Integer.parseInt(c.getString(c.getColumnIndex(TelephoneInterface.KEY_GSMSIGNALSTRENGTH)));							
					} catch(Exception e){
						lat = 0;
						log = 0;
						rssi = -113;
					}
					// if there exist points that has no coordinates continue to the next insert
					if(lat == 0 || log ==0)
						continue;
							
					have_pin = pin_Choose_and_Aplly_phone(rssi);
					if(have_pin == false){
						continue;
					}
							
					final GeoPoint point = new GeoPoint(lat, log);
			
					// Set the center to the middle point
					if(db.numofentries(table_name)/2 == i){
						mycontroller.setCenter(point);
					}
					if(db.numofentries(table_name) == 1)
						mycontroller.setCenter(point);
					
				
					// Help thread variable
					final int i_th = i;
					
					runOnUiThread(new Runnable(){

						public void run() {
							overlayitem = null;
							overlayitem = new OverlayItem(point,"Point#", String.valueOf(i_th));
							myOverlay.addOverlay(overlayitem);
							mymap.getOverlays().add(myOverlay);
						}
						
					});		
					i++;
				}while(c.moveToNext());
			} // if
			// mymap.getOverlays().add(myOverlay);
			// Set the zoom of the map
			// World view is zoom 1
			mycontroller.setZoom(mymap.getMaxZoomLevel() - 10);
					
			if(c != null)
				c.close();
					
			runOnUiThread(new Runnable(){
				public void run() {
					mymap.postInvalidate();
					// Dismiss the progress dialog
					progress.dismiss();
							
					Toast.makeText(MapViewActivity.this, Html.fromHtml("Num of entries: <font color=#4fa5d5>" + String.valueOf(db.numofentries(table_name)) + "</font>"), Toast.LENGTH_LONG).show();
					// Close the database;
					db.close();
				}
				
			});	
			
			
			
			} // if
			else
			{
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(MapViewActivity.this, "No entries in the database.", Toast.LENGTH_LONG).show();
					}
				});
			} // else	
	}

	
	// Start tracking my position on the map for phone interface class
		private void updateMapView_wifi(final String table_name, final String[] headers) {
			
			Log.d("Num OF Entries",String.valueOf(db.numofentries(table_name)));
			
			
			if(db.numofentries(table_name) > 0)
			{
				final Cursor c = db.getAllRecords(headers, table_name);
				int i = 1;
				int lat = 0;
				int log = 0;
				int rssi;
				boolean have_pin = false;
				String new_str[] = new String[3];
				new_str[0] = "";
				new_str[1] = "";
				new_str[2] = "false";
						
				if(c.moveToFirst())
				{
					do{
						try{
							lat = Integer.parseInt(c.getString(c.getColumnIndex(WifiInterface.KEY_LATITUNDE)));
							log = Integer.parseInt(c.getString(c.getColumnIndex(WifiInterface.KEY_LONGTITUDE)));
							rssi = Integer.parseInt(c.getString(c.getColumnIndex(WifiInterface.KEY_WIFISIGNALSTRENGTH)));							
						} catch(Exception e){
							lat = 0;
							log = 0;
							rssi = -95;
						}
						// if there exist points that has no coordinates continue to the next insert
						if(lat == 0 || log ==0)
							continue;
						
						have_pin = pin_Choose_and_Aplly_wifi(rssi);
						if(have_pin == false){
							continue;
						}
						final GeoPoint point = new GeoPoint(lat, log);
						
						// Set the center to the middle point
						if(db.numofentries(table_name)/2 == i){
							mycontroller.setCenter(point);
						}
						if(db.numofentries(table_name) == 1)
							mycontroller.setCenter(point);
						
						// Help variable to pass in the thread
						final int i_th = i;
						
						runOnUiThread(new Runnable(){

							public void run() {
								overlayitem = null;
								overlayitem = new OverlayItem(point,"Point#", String.valueOf(i_th));
								myOverlay.addOverlay(overlayitem);
								mymap.getOverlays().add(myOverlay);
								
							}
							
						});
						
						
						i++;
					}while(c.moveToNext());
							
				} // if
				// mymap.getOverlays().add(myOverlay);
				// Set the zoom of the map
				// World view is zoom 1
				mycontroller.setZoom(mymap.getMaxZoomLevel() - 10);
				if(c != null)
					c.close();	
						
				runOnUiThread(new Runnable(){

					public void run() {
						mymap.postInvalidate();
						// Clear the progress dialog
						progress.dismiss();
						Toast.makeText(MapViewActivity.this, Html.fromHtml("Num of entries: <font color=#4fa5d5>" + String.valueOf(db.numofentries(table_name)) + "</font>"), Toast.LENGTH_LONG).show();
						// Close the database;
						db.close();
					}
				});
				
				
			} // if
			else
			{
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(MapViewActivity.this, "No entries in the database.", Toast.LENGTH_LONG).show();
					}
				});
			}
		}

	public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

		// In this list i will put each overlay item
		private ArrayList<OverlayItem> mOverlays;

		public MyItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
			mOverlays = new ArrayList<OverlayItem>();
			populate();
		}

		@Override
		protected OverlayItem createItem(int ith) {
			return mOverlays.get(ith);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
		
		// Remove item
		public void removeItem(int i){
			mOverlays.remove(i);
			populate();
		}
		
		public void addOverlayItem(OverlayItem overlayItem) {
	        mOverlays.add(overlayItem);
	        populate();
	    }


	    public void addOverlayItem(int lat, int lon, String title) {
	        try {
	            GeoPoint point = new GeoPoint(lat, lon);
	            OverlayItem overlayItem = new OverlayItem(point, title, null);
	            addOverlayItem(overlayItem);    
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			// Populate is inherited from superclass
			populate(); // Will call createItem(int) above
		}

		// Function tap is generated when i click on the pin
		@Override
		protected boolean onTap(int index) {
			OverlayItem printing_overlay = mOverlays.get(index);
			String[] coordinates = new String[3];
			// Take only the coordinates
			// 0 - latitunde
			// 1 - longtitude
			coordinates[0] = String.valueOf(printing_overlay.getPoint().getLatitudeE6());
			coordinates[1] = String.valueOf(printing_overlay.getPoint().getLongitudeE6());
			coordinates[2] = String.valueOf(map_view_select);
			Intent listGo = new Intent(MapViewActivity.this, ListClass.class);
			listGo.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
			listGo.putExtra("coordinates", coordinates);
			startActivity(listGo);

			return true;
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	// Creates the snippet string
	public String snippetStr(Cursor c)
	{
		String data = "Network Type: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_NETWORKTYPE)) + "\n" +
                //"GSM BER: " + c.getString(c.getColumnIndex(PhoneInterfaceClass.KEY_GSMBER)) + "\n" +
                "GSM CELL ID: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_GSMCELLID)) + "\n" +
                "Operator Name: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_OPERATORNAME)) + "\n" +
                //"Longtitude:  " + c.getString(c.getColumnIndex(DBAdapter.KEY_LONGTITUDE)) + "\n" +
                //"Latitude: " + c.getString(c.getColumnIndex(DBAdapter.KEY_LATITUNDE)) + "\n" +
                "Date-Time: " + c.getString(c.getColumnIndex(DBAdapter.KEY_DATETIME));

		return data;
	}
	
	// Dispay all the records in a toast
	public void DisplayRecord(Cursor c)
    {
        Toast.makeText(MapViewActivity.this, 
        		"Network Type: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_NETWORKTYPE)) + "\n" +
                //"GSM BER: " + c.getString(c.getColumnIndex(PhoneInterfaceClass.KEY_GSMBER)) + "\n" +
                "GSM CELL ID: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_GSMCELLID)) + "\n" +
                "Operator Name: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_OPERATORNAME)) + "\n" +
                //"Longtitude:  " + c.getString(c.getColumnIndex(DBAdapter.KEY_LONGTITUDE)) + "\n" +
                //"Latitude: " + c.getString(c.getColumnIndex(DBAdapter.KEY_LATITUNDE)) + "\n" +
                "Date-Time: " + c.getString(c.getColumnIndex(DBAdapter.KEY_DATETIME)),
                Toast.LENGTH_SHORT).show();        
    } 

	// Add comments if the point is the same
	@SuppressWarnings("unused")
	private String[] SameCoordinationPoinHandle(int lat, int log, GeoPoint point){
		String messageTitle[] = new String[3];
		
		if(myOverlay.size() > 0)
		{
			messageTitle[2] = "false";
			for(int j=0;j<myOverlay.size();j++)
			{
				int temp_lat = myOverlay.getItem(j).getPoint().getLatitudeE6();
				int temp_log = myOverlay.getItem(j).getPoint().getLongitudeE6();
				
				if(temp_lat == lat && temp_log == log)
				{
					messageTitle[0] = myOverlay.getItem(j).getTitle();
					messageTitle[1] = myOverlay.getItem(j).getSnippet();
					myOverlay.removeItem(j);
					messageTitle[2] = "true";
				}
			}
		}
		return messageTitle;
	}
	
	/**
	 * 
	 * @param rssi : int, choose the appropriate pin with your eye on the rssi value
	 */
	private boolean pin_Choose_and_Aplly_phone(int rssi){
		boolean success = false; 
		try{
			if(rssi <= -105){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_red_16);
			}
			else if(rssi > -105 && rssi <= -90 ){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_orange_16);
			}
			else if(rssi > -90 && rssi <= -70){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_blue_16);
			}
			else if(rssi > -70){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_green_16);
			}
			// Create the overlay object
			myOverlay = new MyItemizedOverlay(mydrawable);
			success = true;
		}catch(Exception e){
			success = false;
		}
		return success;
	}
	
	/**
	 * 
	 * @param rssi : int, choose the appropriate pin with your eye on the rssi value
	 */
	private boolean pin_Choose_and_Aplly_wifi(int rssi){
		boolean success = false; 
		try{
			if(rssi <= -85){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_red_16);
			}
			else if(rssi > -85 && rssi <= -65 ){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_orange_16);
			}
			else if(rssi > -65 && rssi <= -45){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_blue_16);
			}
			else if(rssi > -45){
				mydrawable = this.getResources().getDrawable(R.drawable.circle_green_16);
			}
			// Create the overlay object
			myOverlay = new MyItemizedOverlay(mydrawable);
			success = true;
		}catch(Exception e){
			success = false;
		}
		return success;
	}	
	
	
	
	/** 
	 * 
	 */
	private class MapThread extends Thread {
		private boolean isRunning;

		MapThread(String ThreadName) {
			super(ThreadName);
			isRunning = false;
		}

		@SuppressWarnings("unused")
		public boolean isRunning() {
			return isRunning;
		}

		@SuppressWarnings("unused")
		private void setIsRunning(boolean value) {
			isRunning = value;
		}

		@Override
		public void run() {
			super.run();
			
			// creates the mapview for the measures table
			switch(map_view_select){
				case 0:
					updateMapView_phone(TelephoneInterface.DATABASE_TABLE, TelephoneInterface.NET_ELEMENTS);
					break;
				case 1:
					updateMapView_wifi(WifiInterface.DATABASE_TABLE_WIFI, WifiInterface.WIFI_ELEMENTS);
					break;
				default:
					runOnUiThread(new Runnable(){
						public void run() {
							Toast.makeText(MapViewActivity.this, "No default Action.", Toast.LENGTH_SHORT).show();		
						}
					});
					
					break;
			}
			
			runOnUiThread(new Runnable(){
				public void run() {
					if(progress != null && progress.isShowing())
						progress.dismiss();
				}
			});
		}

		@Override
		public synchronized void start() {
			super.start();
			isRunning = true;
		}

		@Override
		public void interrupt() {
			isRunning = false;
		}

	}
}
