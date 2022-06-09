package com.ioigoume.eketamobilitytool;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.database.DBAdapter;

public class ListClass extends ListActivity{

	public static final String TAG = "ListClass";
	private String[] coordinates;
	@SuppressWarnings("unused")
	private String table_name = "none";
	private String table_numOfData = "0";
	private String selection = null;
	private int db_select = -1;
	private MobilityToolApplication myapp = null; 
	// Initialize the database
	private DBAdapter list_db = null;
	private ProgressDialog progress = null;
	private ListAdapter mAdapter = null;
	private Cursor c = null;
	
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the data send from the invoking 
		try{
			//////////////////////////
			/////	GOOGLE MAP
			/////////////////////////
			
			// This instance is invoked only when i press a pin from map view
			Intent dataFromMap = getIntent();
			coordinates = dataFromMap.getExtras().getStringArray("coordinates");
			selection = String.format("%s=%s and %s=%s", TelephoneInterface.KEY_LATITUNDE,
																	coordinates[0],
																	TelephoneInterface.KEY_LONGTITUDE,
																	coordinates[1]);													
			// Set the title of the Activity - ListView screen
			ListClass.this.setTitle(getString(R.string.title_activity_listclass));
			
			switch(Integer.parseInt(coordinates[2])){
				case 0:
					db_select = -2;
					break;
				case 1:
					db_select = -3;
					break;
				default:
					db_select = -1;
					break;
			}
		} catch (NullPointerException e){
			///////////////////
			//		MENU
			///////////////////
			
			// The main menu sends no coordinates, therefore an exception is thrown
			// and i can work in here for these triggers
			Intent dataFromMenu = getIntent();
			db_select = Integer.parseInt(dataFromMenu.getAction().toString());
			
			// Set the title of the Activity - ListView screen
			ListClass.this.setTitle(getString(R.string.title_activity_grid_db));
		} catch (Exception e){	}
		
		
		// Create an object of the database
		list_db = new DBAdapter(ListClass.this);
		try{
			list_db.open();
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(this, "Database could not be opened.Retry.", Toast.LENGTH_LONG).show();
			list_db = null;
			return;
		}
		
		// Async
		try{
			progress = ProgressDialog.show(ListClass.this, "Wait!", "List is loading...", true, false);
			ListThread tmpTh = new ListThread("ListThread");
			tmpTh.start();
		} catch(Exception e){
			// Close the database before exciting
			if(c!= null && !c.isClosed())
				c.close();
			if(progress != null && progress.isShowing() == true)
				progress.dismiss();
			list_db.close();
			
			Toast.makeText(ListClass.this, "List could not be created.",Toast.LENGTH_LONG).show();
		}
		
		
		
		
		//list_db.close();
	}
		
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(selection!=null){
			Intent netIntent = new Intent(ListClass.this, UmtsInfo.class);
			netIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
							Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(netIntent);
		}
	}
	
	
	/**
	 * Help function
	 * @param selection
	 */
	private String data_input(int selection, Cursor c){
		StringBuilder myData = new StringBuilder();
		switch(selection){
			case -2: 
			case 0:
				// Here i can add as many data i want
				myData.append("Network Type: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_NETWORKTYPE)));
				myData.append("\nGSM SignalStrength: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_GSMSIGNALSTRENGTH)));
				//myData.append("\nGSM BER: " + c.getString(c.getColumnIndex(PhoneInterfaceClass.KEY_GSMBER)));
		        myData.append("\nGSM CELL id: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_GSMCELLID)));
		        myData.append("\nOperator Name: " + c.getString(c.getColumnIndex(TelephoneInterface.KEY_OPERATORNAME)));
				break;
			case 1:
			case -3:
				// Here i can add as many data i want
				myData.append("SSID: " + c.getString(c.getColumnIndex(WifiInterface.KEY_SSID)));
				myData.append("\nBSSID: " + c.getString(c.getColumnIndex(WifiInterface.KEY_BSSID)));
				myData.append("\nChannel: " + c.getString(c.getColumnIndex(WifiInterface.KEY_CHANNEL_NUM)));
		        myData.append("\nRSSI: " + c.getString(c.getColumnIndex(WifiInterface.KEY_WIFISIGNALSTRENGTH)));
		        myData.append("\nLink Speed: " + c.getString(c.getColumnIndex(WifiInterface.KEY_LINK_SPEED)));
		        myData.append("\nCapabilities: " + c.getString(c.getColumnIndex(WifiInterface.KEY_CAPABILITIES)));
		        myData.append("\nAccess Points: #" + c.getString(c.getColumnIndex(WifiInterface.KEY_AP_NUM)));
				break;
			default:
				break;
		}
		
		return myData.toString();
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try{
			menu.add("Info");
		}catch(Exception e){
			Toast.makeText(ListClass.this, "Menu could not be inflated", Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().toString().equalsIgnoreCase("info")){
			if(db_select == 1 ){
				int num_of_ap  = distinct_String_data(this, WifiInterface.KEY_SSID, WifiInterface.DATABASE_TABLE_WIFI).size();
				
				// share the number of distinct access points in the whole database
				myapp = (MobilityToolApplication)this.getApplication();
				myapp.getPreferencesObjNonXml().setNumOfAccessPoints(num_of_ap);
				
				Toast toast = Toast.makeText(
						ListClass.this, 
						Html.fromHtml(
										"There are <font color=#4fa5d5>" +	
										table_numOfData + 
										"</font> entries in the database." +
										"There are <font color=#4fa5d5>" + 
										String.valueOf(num_of_ap) + 
										"</font> different access points in " +
										"the database."), 
						Toast.LENGTH_LONG);
				toast.show();
			} else {
			
				Toast toast = Toast.makeText(
						ListClass.this, 
						Html.fromHtml(
								"There are <font color=#4fa5d5>" +	
								table_numOfData + 
								"</font> entries in the database."),
						Toast.LENGTH_LONG);
				toast.show();
			}
			
		}
		return true;
	}

	/**
	 * 
	 * @param ctx
	 * @param column_grouping : column that i want a distinct list to be obtained
	 * @return a list of the distinct entries of the column that you give
	 */
	public synchronized ArrayList<String> distinct_String_data(Context ctx, String column_grouping, String table){
		// Accesspoint List
		ArrayList<String> dataList = new ArrayList<String>();
		Cursor c = null;
		/************ INITIALIZATIONS **************/
		// Create an object of the database
		DBAdapter db = new DBAdapter(ctx);
		try{
			db.open();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			Toast.makeText(this, "Database could not be opened.Retry.", Toast.LENGTH_LONG).show();
			return null;
		}
				
		///////////////////////////////////////////////////////////////
		// Retrieve the a cursor containing the different access points
		///////////////////////////////////////////////////////////////		
		c = db.myDistinctQuery(table, column_grouping, db.getDb());
		if(c != null){
			if(c.moveToFirst()){
				do{
					dataList.add(String.valueOf(c.getString(c.getColumnIndex(column_grouping))));
				}while(c.moveToNext());
				// Pass the data to main function
				Log.d(TAG, "num of ap:" + String.valueOf(dataList.size()));
			}
		}
				
		// Reset my cursor, the cursor is null catch the exception
		try{
			c.close();
			c = null;
		}catch(Exception e){
			c = null;
		}
		db.close();
		return dataList;
	}
	
	
	/** 
	 * 
	 */
	private class ListThread extends Thread {
		private boolean isRunning;

		ListThread(String ThreadName) {
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
			
			////////////////////////////////////////////////////////////////
			////	SELECTIONS 0,1 ARE TRIGGERED FROM THE MAIN MENU
			////////////////////////////////////////////////////////////////
			if(db_select == -3){ // Google Map, wifi interface
				c = list_db.myQuery(WifiInterface.DATABASE_TABLE_WIFI, WifiInterface.WIFI_ELEMENTS, selection, null);
				table_name = WifiInterface.DATABASE_TABLE_WIFI;
			} else if(db_select == -2){ // Google Map, phone interface
				c = list_db.myQuery(TelephoneInterface.DATABASE_TABLE, TelephoneInterface.NET_ELEMENTS, selection, null);
				table_name = TelephoneInterface.DATABASE_TABLE;
			} else if(db_select == -1){ // No action
				Toast.makeText(ListClass.this, "No action received.", Toast.LENGTH_SHORT).show();
				table_name = "none";
			} else if(db_select == 0){ // Db List, phone interface
				c = list_db.myQuery(TelephoneInterface.DATABASE_TABLE, TelephoneInterface.NET_ELEMENTS, selection, null);
				table_name = TelephoneInterface.DATABASE_TABLE;
			}else if(db_select == 1){ // Db List, wifi interface
				c = list_db.myQuery(WifiInterface.DATABASE_TABLE_WIFI, WifiInterface.WIFI_ELEMENTS, selection, null);
				table_name = WifiInterface.DATABASE_TABLE_WIFI;
			}
				
			if(c!= null && c.moveToFirst())
			{
				ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
					
				
				do{
					HashMap<String,String> map_entry = new HashMap<String,String>();
					String data = data_input(db_select, c);
			        map_entry.put(DBAdapter.KEY_DATETIME, c.getString(c.getColumnIndex(DBAdapter.KEY_DATETIME)));
			        map_entry.put("data", data);
			        list.add(map_entry);
			            
				}while(c.moveToNext());
					
				// size of the list
				table_numOfData = String.valueOf(list.size());
				String[] columns = new String[]{DBAdapter.KEY_DATETIME, "data"};
				int[] renderTo = new int[]{R.id.dateView_id,R.id.dataView_id};
				mAdapter = new SimpleAdapter(
							ListClass.this, 
							list, 
							R.layout.row, 
							columns, 
							renderTo);
					
			} 				
			
			// Close the database before exciting
			if(c!= null && !c.isClosed())
				c.close();
			
			runOnUiThread(new Runnable(){
				public void run() {
					if(mAdapter != null)
						setListAdapter(mAdapter);
					
					if(progress != null && progress.isShowing())
						progress.dismiss();
					list_db.close();
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
