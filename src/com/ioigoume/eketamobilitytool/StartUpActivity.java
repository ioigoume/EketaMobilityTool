package com.ioigoume.eketamobilitytool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.database.DBAdapter;

import de.quist.app.errorreporter.ExceptionReporter;

public class StartUpActivity extends BaseClass implements TelephoneInterface, WifiInterface {

	public static final String TAG = "StartUpActivity";
	private MobilityToolApplication myapp = null;
	static public ImageButton gpsStateImage = null;
	static public ImageButton ggStateImage = null;
	static public ImageButton wifiStateImage = null;
	static public ImageButton PhoneStateImage = null;
	static public ImageButton NetWorkOperatorIndicator = null;
	static public ImageButton ServerIndicator = null;
	static public ImageButton DataMobileIndicator = null;
	
	static public ImageButton NitlabInfo = null;
	

	@Override
	protected void onResume() {
		super.onResume();
		// Set the icons to the appropriate state
		StartUpActivity.gpsStateImage.setImageDrawable(myapp.getPreferenceObjIconState().getGpsStateIcon());
		StartUpActivity.wifiStateImage.setImageDrawable(myapp.getPreferenceObjIconState().getWifiStateIcon());
		StartUpActivity.PhoneStateImage.setImageDrawable(myapp.getPreferenceObjIconState().getPhoneStateIcon());
		StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(myapp.getPreferenceObjIconState().getOperatorStateIcon());
		StartUpActivity.ggStateImage.setImageDrawable(myapp.getPreferenceObjIconState().getGgStateIcon());
		StartUpActivity.ServerIndicator.setImageDrawable(myapp.getPreferenceObjIconState().getServerStateIcon());
		StartUpActivity.DataMobileIndicator.setImageDrawable(myapp.getPreferenceObjIconState().getDataStateIcon());
		//StartUpActivity.NitlabInfo.setImageDrawable(myapp.getPreferenceObjIconState().getNitlabStateIcon());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_up_activity);

		// Register my exception reporter
		@SuppressWarnings("unused")
		ExceptionReporter reporter = ExceptionReporter.register(this);
		
		myapp = (MobilityToolApplication)StartUpActivity.this.getApplication();
		gpsStateImage = (ImageButton)findViewById(R.id.imageGpsState);
		wifiStateImage = (ImageButton)findViewById(R.id.imageWifiState);
		ggStateImage = (ImageButton)findViewById(R.id.imageGGState);
		PhoneStateImage = (ImageButton) findViewById(R.id.imagePhoneState);
		NetWorkOperatorIndicator = (ImageButton) findViewById(R.id.imageOperatorState);
		ServerIndicator = (ImageButton) findViewById(R.id.imageServerState);
		DataMobileIndicator = (ImageButton) findViewById(R.id.imageDataNetStatus);
		
		NitlabInfo = (ImageButton) findViewById(R.id.imageNitlabInfo);
		NitlabInfo.setOnClickListener(nit_listener);
	}
	
	// ///////////// MENU STAFF

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.menu, menu);
			//Set the start/stop sampling icon
			// correct: (menu, null)
			setSamplingStartStopIcon(menu, null);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			
			// Switches between the items on the menu click
			// At these point i have only the preference item
			switch (item.getItemId()){
					case R.id.itemMenu:
						startActivity(new Intent(StartUpActivity.this, PrefsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
						break;
					case R.id.itemService:
						// if you have not set name and surname break
						if(myapp.getPreferenceObj().getName() == "none" && myapp.getPreferenceObj().getSurname() == "none"){
							Toast.makeText(this, "Give Name and Surname.", Toast.LENGTH_LONG).show();
							break;
						}
						// Set the appropriate button image, this is defined by the state of the bakcground service
						int state = setSamplingStartStopIcon(null, item);
						Intent intent = new Intent(StartUpActivity.this, BackgroundService.class);
						intent.addFlags(Service.START_STICKY);
						switch(state){
							case 0:
								startService(intent);
								break;
							case 1:
								stopService(intent);
								break;
							default:
								break;
						}
						break;
					case R.id.itemDelDatabaseContent:
						if(!isServiceRunning(("." + BackgroundService.class.getSimpleName()).trim())){
							final DBAdapter tmpAdapter = new DBAdapter(StartUpActivity.this);
							tmpAdapter.open();
							final String[] tables = tmpAdapter.DBtableNames(tmpAdapter.getDb());
							//
							//	DIALOG THAT HELPS THE USER CHOOSE WHAT TO DELETE FROM DATABASE
							//
							final CharSequence[] items_del = {TelephoneInterface.DATBASE_TABLE_PUBLIC_NAME, WifiInterface.DATBASE_TABLE_PUBLIC_NAME, DBAdapter.DATABASE_NAME};
					        AlertDialog.Builder builder_del = new AlertDialog.Builder(this);
					        View title_view = (View) View.inflate(this, R.layout.alert_dialog_title, null);
					        TextView mTitle = (TextView) title_view.findViewById(R.id.alertTitle);
					        mTitle.setText("Pick item to delete");
					        builder_del.setCustomTitle(title_view);
					        builder_del.setSingleChoiceItems(items_del, -1, new DialogInterface.OnClickListener(){
					            public void onClick(DialogInterface dialogInterface, int item) {
					            	switch(item){
						            	case 0:
						            		for(String table : tables){
						            			if(table.equalsIgnoreCase(TelephoneInterface.DATABASE_TABLE)){
						            				String[] tmp = {table};
						            				String str = tmpAdapter.deleteAllelements(tmp,tmpAdapter.getDb());
						            				tmpAdapter.close();
						            				Toast.makeText(StartUpActivity.this, str, Toast.LENGTH_LONG).show();
						            			}
						            		}
						            		break;
						            	case 1:
						            		for(String table : tables){
						            			if(table.equalsIgnoreCase(WifiInterface.DATABASE_TABLE_WIFI)){
						            				String[] tmp = {table};
						            				String str = tmpAdapter.deleteAllelements(tmp,tmpAdapter.getDb());
						            				tmpAdapter.close();
						            				Toast.makeText(StartUpActivity.this, str, Toast.LENGTH_LONG).show();
						            			}
						            		}
						            		break;
						            	case 2:
						            		tmpAdapter.onDrop(tmpAdapter.getDb());
						            		tmpAdapter.delDB(tmpAdapter.getDb());
						            		Toast.makeText(StartUpActivity.this, "Tables and DBase dropped", Toast.LENGTH_LONG).show();
						            		break;
					            		default:
					            			break;
					            	}
					            	dialogInterface.dismiss();
					            }
					        });
					        builder_del.setCancelable(false);
					        builder_del.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								
								public void onClick(DialogInterface dialog, int which) {
									return;
								}
							});
					        
					        builder_del.create().show();
						}
						else
						{
							Toast.makeText(this, "The Service is still Active", Toast.LENGTH_LONG).show();
						}
						break;
					case R.id.itemGoogleMap:
						final CharSequence[] items_list_googlemap = {"3G Data", "Wifi Data"};
				        AlertDialog.Builder builder_list_googlemap = new AlertDialog.Builder(this);
				        View title_view = (View) View.inflate(this, R.layout.alert_dialog_title, null);
				        TextView mTitle = (TextView) title_view.findViewById(R.id.alertTitle);
				        mTitle.setText("Pick Table");
				        builder_list_googlemap.setCustomTitle(title_view);
				        builder_list_googlemap.setSingleChoiceItems(items_list_googlemap, -1, new DialogInterface.OnClickListener(){
				            public void onClick(DialogInterface dialogInterface, int item) {
				            	switch(item){
					            	case 0:
					            		Intent phone = new Intent(StartUpActivity.this,	MapViewActivity.class);
					            		phone.setAction(String.valueOf(item));
					            		startActivity(phone);
					            		dialogInterface.dismiss();
					            		break;
					            	case 1:
					            		Intent wifi = new Intent(StartUpActivity.this,	MapViewActivity.class);
					            		wifi.setAction(String.valueOf(item));
					            		startActivity(wifi);
					            		dialogInterface.dismiss();
					            		break;
				            	}
				            }
				        });
				        builder_list_googlemap.setCancelable(false);
				        builder_list_googlemap.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						});
				        builder_list_googlemap.create().show();
						
						break;
					case R.id.itemConsoleMonitor:
						Intent console = new Intent(StartUpActivity.this, ConsoleMonitorActivity.class);
						startActivity(console);
						break;
					case R.id.itemGridDB:
						//
						//	DIALOG THAT HELPS THE USER CHOOSE THE TABLE TO LIST VIEW
						//
						final CharSequence[] items_list = {TelephoneInterface.DATBASE_TABLE_PUBLIC_NAME, WifiInterface.DATBASE_TABLE_PUBLIC_NAME};
				        AlertDialog.Builder builder_list = new AlertDialog.Builder(this);
				        View title_view_list = (View) View.inflate(this, R.layout.alert_dialog_title, null);
				        TextView mTitle_list = (TextView) title_view_list.findViewById(R.id.alertTitle);
				        mTitle_list.setText("Pick Table");
				        builder_list.setCustomTitle(title_view_list);
				        builder_list.setSingleChoiceItems(items_list, -1, new DialogInterface.OnClickListener(){
				            public void onClick(DialogInterface dialogInterface, int item) {
				            	// SHOW THE LISTVIEW
				            	Intent grid = new Intent(StartUpActivity.this, ListClass.class);
				            	// Action 0: phone interface, Action 1: wifi interface
				            	grid.setAction(String.valueOf(item));
								grid.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);
								startActivity(grid);
				                dialogInterface.dismiss();
				            }
				        });
				        builder_list.setCancelable(false);
				        builder_list.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						});
				        builder_list.create().show();
						
						break;
					case R.id.itemPlot:
						
						//
						//	DIALOG THAT HELPS THE USER CHOOSE THE VARIABLE TO SEE THE GRAPH
						//
						final CharSequence[] items_plot = { TelephoneInterface.PHONE_SIGNAL_STRENGTH, 
															WifiInterface.WIFI_SIGNAL_STRENGTH,
															WifiInterface.WIFI_CHANNEL_RATE} ;
						
				        AlertDialog.Builder builder_plot = new AlertDialog.Builder(this);
				        builder_plot.setTitle("Pick Variable");
				        View title_view_plot = (View) View.inflate(this, R.layout.alert_dialog_title, null);
				        TextView mTitle_plot = (TextView) title_view_plot.findViewById(R.id.alertTitle);
				        mTitle_plot.setText("Pick Variable");
				        builder_plot.setCustomTitle(title_view_plot);
				        builder_plot.setSingleChoiceItems(items_plot, -1, new DialogInterface.OnClickListener(){
				            public void onClick(DialogInterface dialogInterface, int item) {
				            	// SHOW THE GRAPH
				            	switch(item){
				            		case 0:
				            			String values_home[] = new String[]{TelephoneInterface.KEY_GSMSIGNALSTRENGTH, TelephoneInterface.DATABASE_TABLE};
				            			
				            			Intent plot_phone = new Intent(StartUpActivity.this, ChartActivityLayout.class);
				            			plot_phone.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				            			plot_phone.putExtra("class", values_home);
				            			
				            			startActivity(plot_phone);
						                dialogInterface.dismiss();
				            			break;
				            		case 1:
				            			String values_wifi[] = new String[]{WifiInterface.KEY_WIFISIGNALSTRENGTH, WifiInterface.DATABASE_TABLE_WIFI};
				            			
				            			Intent plot_wifi = new Intent(StartUpActivity.this, ChartActivityLayout.class);
				            			plot_wifi.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				            			plot_wifi.putExtra("class", values_wifi);
				            			
				            			startActivity(plot_wifi);
						                dialogInterface.dismiss();
				            			break;
				            		case 2:
				            			String values_wifi_rate[] = new String[]{WifiInterface.KEY_LINK_SPEED, WifiInterface.DATABASE_TABLE_WIFI};
				            			
				            			Intent plot_wifi_rate = new Intent(StartUpActivity.this, ChartActivityLayout.class);
				            			plot_wifi_rate.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				            			plot_wifi_rate.putExtra("class", values_wifi_rate);
				            			
				            			startActivity(plot_wifi_rate);
				            			dialogInterface.dismiss();
				            			break;
				            		default:
				            			break;
				            	}
				            }
				        });
				        builder_plot.setCancelable(false);
				        builder_plot.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						});
				        builder_plot.create().show();
				        
						break;
					case R.id.itemPushToServer:
						// wifi_tuples
						ArrayList<String[]> wifi_tuples = massiveTuples(	StartUpActivity.this.getApplicationContext(),
																	WifiInterface.DATABASE_TABLE_WIFI,
																	WifiInterface.OML_WIFI_SCHEMA_ELEMENTS,
																	0);
						
						// phone_tuples
						ArrayList<String[]> phone_tuples = massiveTuples(StartUpActivity.this.getApplicationContext(),
																	TelephoneInterface.DATABASE_TABLE,
																	TelephoneInterface.OML_PHONE_SCHEMA_ELEMENTS,
																	0);
						
						if(wifi_tuples != null)
							Log.i(TAG,"num of wifi tuples:" + String.valueOf(wifi_tuples.size()));
						if(phone_tuples != null)
							Log.i(TAG,"num of phone tuples:" + String.valueOf(phone_tuples.size()));
						
						if(wifi_tuples == null && phone_tuples == null){
							Toast.makeText(this, "No data to send on server.", Toast.LENGTH_SHORT).show();
							break;
						}
						
						if(wifi_tuples.size() == 0 && phone_tuples.size() == 0){
							Toast.makeText(this, "No data to send on server.", Toast.LENGTH_SHORT).show();
							break;
						}
						
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
							Toast.makeText(this, "Database sent successfully to server.", Toast.LENGTH_SHORT).show();
							
						}else{
							Toast.makeText(this, "Could not connect to server. Try again later.", Toast.LENGTH_SHORT).show();
						}
						break;
					case R.id.itemExtractToSD:
						//
						// IF THERE IS AN SD MEMORY IN THE PHONE SAVE A COPY OF DATABASE IN THERE
						//
						try {
		                    File sd = null;
		                    // Put the database
		                    if(Environment.isExternalStorageRemovable()){
		                    	sd = Environment.getExternalStorageDirectory();
		                    } else{
		                    	sd = Environment.getExternalStorageDirectory().getParentFile();
		                    	String filepath = sd.getAbsolutePath().toString() + "/" + ret_external_folder(sd);
		                    	sd = new File(filepath);
		                    	if(!sd.isDirectory())
		                    		sd = Environment.getExternalStorageDirectory();
		                    	
		                    }
		                    
		                    // Current Date
		                    String db_time_stamp = ret_current_date();
		                    db_time_stamp = db_time_stamp.replace(":", "_");
		                    db_time_stamp = db_time_stamp.replace(" ", "_");
		                    db_time_stamp = db_time_stamp.replace("/", "_");
		                    Log.d(TAG, db_time_stamp);
		                   
		                    
		                    // Take the database
		                    File data = Environment.getDataDirectory();
		                   
		                    if (sd.canWrite()) {
		                        String currentDBPath = "//data//com.ioigoume.eketamobilitytool//databases//" + DBAdapter.DATABASE_NAME;
		                        String backupDBPath = "backupMeasuresDatabase_" + db_time_stamp +".db";
		                        
		                        File currentDB = new File(data, currentDBPath);
		                        File backupDB = new File(sd, backupDBPath);

		                        if (currentDB.exists()) {
		                        	FileInputStream input = new FileInputStream(currentDB);
		                        	FileOutputStream output = new FileOutputStream(backupDB);
		                        	FileChannel src = input.getChannel();
		                            FileChannel dst = output.getChannel();
		                            
		                            dst.transferFrom(src, 0, src.size());
		            
		                            output.flush();
		                            src.close();
		                            dst.close();
		                            output.close();
		                            input.close();
		                            if(backupDB.exists()){
		                            	backupDB.setReadable(true);
		                            	Toast.makeText(this, "Database has been copied successfully.", Toast.LENGTH_LONG).show();
		                            }else{
		                            	Toast.makeText(this, "Database has not been copied successfully.", Toast.LENGTH_LONG).show();
		                            }
		                        }
		                        else{
		                        	Toast.makeText(this, "The db you specified does not exist.", Toast.LENGTH_LONG).show();
		                        }
		                        	
		                    }else{
		                    	Toast.makeText(this, "Can not write on sd", Toast.LENGTH_LONG).show();
		                    }
		                } catch (Exception e) {
		                	e.printStackTrace();
		                	Toast.makeText(this, "Problem copying the database to external storage.", Toast.LENGTH_LONG).show();
		                }
						break;
					case R.id.itemRealTimeList:
						final CharSequence[] items_list_monitor = {"3G Data", "Wifi Data"};
				        AlertDialog.Builder builder_list_monitor = new AlertDialog.Builder(this);
				        View title_view_monitor = (View) View.inflate(this, R.layout.alert_dialog_title, null);
				        TextView mTitle_monitor = (TextView) title_view_monitor.findViewById(R.id.alertTitle);
				        mTitle_monitor.setText("Pick List");
				        builder_list_monitor.setCustomTitle(title_view_monitor);
				        builder_list_monitor.setSingleChoiceItems(items_list_monitor, -1, new DialogInterface.OnClickListener(){
				            public void onClick(DialogInterface dialogInterface, int item) {
				            	switch(item){
					            	case 0:
					            		Intent phone_list = new Intent(StartUpActivity.this, RealTimeListViewMonitorNeighbourCell.class);
										phone_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
										// Start the activity
										startActivity(phone_list);
					            		dialogInterface.dismiss();
					            		break;
					            	case 1:
					            		Intent wifi_list = new Intent(StartUpActivity.this, RealTimeListViewWifiMonitor.class);
										wifi_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
										// Start the activity
										startActivity(wifi_list);
					            		dialogInterface.dismiss();
					            		break;
				            	}
				            }
				        });
				        builder_list_monitor.setCancelable(false);
				        builder_list_monitor.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						});
				        builder_list_monitor.create().show();
						break;
					default:
						break;
			}
			return true;
		}

		// --- SERVICE CHECK CONTROL USING THE SYSTMEM
		// Check if the service is running
		public boolean isServiceRunning(String serviceName) {
			boolean serviceRunning = false;
			ActivityManager am = (ActivityManager) StartUpActivity.this.getSystemService(ACTIVITY_SERVICE);
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

		
		/**
		 * 
		 * @return the folder that is the external device
		 */
		public String ret_external_folder(File mnt){
			if(mnt!= null && mnt.exists()){
				String[] dirChildren = mnt.list();
				if(dirChildren == null){
					Toast.makeText(this, "mnt directory is empty", Toast.LENGTH_LONG).show();
				}else{
					for(String str : dirChildren){
						if(str.contains("ext")){
							return str;
						}
					}
				}
			}
			return null;
		}
		
		
		/**
		 * @return string: current date with the following format,  yyyy/MM/dd HH:mm:ss
		 */
		public String ret_current_date(){
			// Get current time_date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
     	    //get current date time with Date()
     	    Date date = new Date(System.currentTimeMillis());
     	     
     	    // Return the current date in the format i specified above
     	    return dateFormat.format(date);
		}
		
		
		/**
		 * Set the icon and the text of the menu item after checking the state of the background service
		 * @param item: the menu item
		 * @return state: int , if equals to 0(zero): service is stopped, if equals to 1(one) service is running
		 */
		private int setSamplingStartStopIcon(Menu menu, MenuItem item){
			int state = 0;

			// The service is not running
			if(!isServiceRunning(("." + BackgroundService.class.getSimpleName()).trim()))
			{
				if(item != null){
					item.setIcon(android.R.drawable.ic_media_pause);
					item.setTitle(R.string.stringStopServiceItem);
				}
				// correct: menu != null
				if(menu != null){
					MenuItem m_item = menu.findItem(R.id.itemService);
					m_item.setIcon(android.R.drawable.ic_media_play);
					m_item.setTitle(R.string.stringTitleStartService);
				}
				// the state is stopped
				state = 0;
			}else{	// The service is running
				if(item != null){
					item.setIcon(android.R.drawable.ic_media_play);
					item.setTitle(R.string.stringTitleStartService);
				}
				if(menu != null){
					MenuItem m_item = menu.findItem(R.id.itemService);
					m_item.setIcon(android.R.drawable.ic_media_pause);
					m_item.setTitle(R.string.stringStopServiceItem);
				}
				// the state is running
				state = 1;
			}
			
			return state;
		}
		
		
		/**
		 *  Nitlab image Button ClickListener
		 */
		OnClickListener nit_listener = new OnClickListener() {
			public void onClick(View v) {
				//Opern default browser and connect to http://nitlab.inf.uth.gr
				Intent nitlabintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://nitlab.inf.uth.gr"));
				nitlabintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(nitlabintent);
			}

		};
		
		
		
		/**
		 * @param ctx : Context
		 * @param table_name : String, the table i want to retrieve data from
		 * @param tuple_elements : String[], the columns of the table i want to retrieve
		 * @param max_id : long, the id under which i want to search for rows
		 * @return : List<String[]> : each entry contains an array with the elements obtained of one row
		 */
		private synchronized ArrayList<String[]> massiveTuples(Context ctx, String table_name, String[] tuple_elements, long start_id) {
			Cursor c = null;
			ArrayList<String[]> tuple_data_ret = null;
			/************ INITIALIZATIONS **************/
			// Create an object of the database
			DBAdapter db = new DBAdapter(ctx);
			try{
				db.open();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				Toast.makeText(ctx, "Database could not be opened.Retry.", Toast.LENGTH_LONG).show();
				db = null;;
				return null;
			}
					
			///////////////////////////////////////////////////////////////
			// Retrieve the a cursor containing the different access points
			///////////////////////////////////////////////////////////////	
			String whereClause = String.format("%s > %s and %s != -1 and %s != -1 and %s != -1", 
												BaseColumns._ID, 
												String.valueOf(start_id),
												TelephoneInterface.KEY_ALTITUDE,
												TelephoneInterface.KEY_LONGTITUDE,
												TelephoneInterface.KEY_LATITUNDE);
			c = db.myQuery(table_name, tuple_elements, whereClause, null);
			if(c != null){
				// Data arraylist
				ArrayList<String[]> tuple_data = new ArrayList<String[]>();
				ArrayList<String> tuple_data_tmp = new ArrayList<String>();
				
				if(c.moveToFirst()){
					do{
						for(String tuple_element : tuple_elements){
							tuple_data_tmp.add(String.valueOf(c.getString(c.getColumnIndex(tuple_element))));
						}
						tuple_data.add((String[])tuple_data_tmp.toArray(new String[tuple_data_tmp.size()]));
						tuple_data_tmp.clear();
					}while(c.moveToNext());
				} // if
				if(tuple_data != null && tuple_data.size() > 0){
					tuple_data_ret = tuple_data;
				}
			} // if
					
			// Reset my cursor, the cursor is null catch the exception
			try{
				c.close();
				c = null;
			}catch(Exception e){
				c = null;
			}
			db.close();
			db = null;
			
			
			return tuple_data_ret;
		}
}
