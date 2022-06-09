package com.ioigoume.eketamobilitytool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.database.DBAdapter;


public class BackgroundService extends Service implements ServerConstantsInterface, ApplicationGlobalVars{

	// Constant auxiliary variables
	private static final String TAG = BackgroundService.class.getSimpleName();
	
	// Database object and variables
	private DBAdapter db_adapter = null;
	
	//	Preference variable 
	private MobilityToolApplication myapp = null;
	
	//	Coordination variables
	private Criteria myCriteria;
	
	// PhoneState Variables
	private TelephoneClass myPhoneState = null;		// MONITOR THE PHONE STATE
	@SuppressWarnings("unused")
	private boolean idlecallstate = true;					// TRUE IF THE PHONE IS NOT BUSY
	@SuppressWarnings("unused")
	private boolean serviceStateIn = true;					// TRUE IF THE PHONE STATE IS NORMAL
	private ConnectivityManager myConMgr = null;			// GIVES ACCESS TO MONITOR THE CONNECTION OF THE DEVICE
	
	//Wifi Variables
	private wifiClass myWifiState = null;
	
	// BroadcastReceiver variables
	private MyBroadcastReceiver receiver = null;
	private MyIntentFilter ServerIntent = null;
	

	
	// Listeners
	public MyLocationListener myLockListen = null;			//	MONITOR THE GPS or CELLULAR ANTENNA
	private MyGpsStatusListener myGpsStatusListener = null;
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private TelephonyManager myTelMgr = null;				//	NETWORK
	private WifiManager myWifiMgr = null;					//  Wifi
	private MyPhoneStateListener ioiListener = null;			//	PHONE STATE LISTENER
	private PowerManager pm = null;							// 	POWER - NOT SLEEP
	private WakeLock myWakeLock = null;
	private LocationManager myLockMng = null;				// 	GPS
	private LocationContentObserver gpsObserver = null;		//  GPS status changed observer

	// //////////// CREATE MY SERVICE (METHOD)

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "on BackgroundService Create");
		
		// /////////// CALL MY SERVICES

		// Power management service
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// Telephone management service
		myTelMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// Notification manager Service
		notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Location Manager Service
		myLockMng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Wifi Manager Service
		myWifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// Connectivity Manager
		myConMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//////////////////////////////////////
		//	INITIALIZE OBJECT AND THREADS   //
		//////////////////////////////////////
		/**
		 * THE OBJECTS MUST BE INITIALIZED BEFORE THE LISTENERS
		 */		
		//	INITIALIZE THE WIFI CLASS - CONTAINS IT'S OWN SAMPLING THREAD
		myWifiState = new wifiClass(myWifiMgr, BackgroundService.this);
		// INITIALIZE THE PHONESTATE CLASS - CONTAINS THE SAMPLING THREAD
		myPhoneState = new TelephoneClass(BackgroundService.this);
		//	CREATE GPS OBSERVER OBJECT
		gpsObserver = new LocationContentObserver(new Handler());
	
		
		// THE LISTENERS MUST BE INITIALIZED BEFORE THE BROADCAST RECEIVERS
		// IT SEEMS THAT IF YOU DO THIS AFTERWARDS THERE IS NO CONNECTION AND
		// NO TRIGGER. TO BE SAFE INITIALIZE EARLY ENOUGH TO GIVE TIME TO THE 
		// SYSTEM TO MAKE A FULL START UP
		//	 	INITIALIZE THE LISTENERS		
		/////////////////////////////////////
		// Create the PHONE STATE LISTENER //
		/////////////////////////////////////
		ioiListener = new MyPhoneStateListener();
		//////////////////////////////////
		// Create the LOCATION Listener //
		//////////////////////////////////
		myLockListen = new MyLocationListener();
		//////////////////////////////////
		// Create the GPS loc Listener  //
		//////////////////////////////////
		myGpsStatusListener = new MyGpsStatusListener();
		
		
		
		// INITIALIZE THE CONNECTION TO THE PREFERENCES
		if (myapp == null) 
		myapp = (MobilityToolApplication) BackgroundService.this.getApplication();
		
		//////////////////////////////////////////////////
		// INITIALIZE THE BROADCAST RECEIVER - OBSERVER	//
		//////////////////////////////////////////////////
		// Broadcast Receiver
		receiver = new MyBroadcastReceiver();
		// Intent Filter
		ServerIntent = new MyIntentFilter();
		///////////////////////////////////////
		// 		Start the gps observer       //  
		///////////////////////////////////////
		getContentResolver().registerContentObserver(
			Settings.Secure.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
			false,
			gpsObserver);


		
		///////////////////////////////////////
		// Define what the os should monitor //
		///////////////////////////////////////
		myTelMgr.listen(ioiListener, // What should the phone state listener listen to
			PhoneStateListener.LISTEN_SIGNAL_STRENGTH |
			PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
			PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
			PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
			PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
			PhoneStateListener.LISTEN_SERVICE_STATE |
			PhoneStateListener.LISTEN_CELL_LOCATION |
			PhoneStateListener.LISTEN_CALL_STATE |
			PhoneStateListener.LISTEN_DATA_ACTIVITY |
			PhoneStateListener.LISTEN_DATA_CONNECTION_STATE); // This is all one function
		///////////////////////////////////
		//	INITIALIZE THE POWER CONTROL //
		///////////////////////////////////
		myWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "call_lock");
		
		//////////////////////////////////////
		//	DISABLE THE PUSH ON SERVER PREF //
		//////////////////////////////////////
		
	
	}

	
	///////////////////////////////////////////////////
	//////////// START THE SERVICE (METHOD) ///////////
	///////////////////////////////////////////////////

	@Override
	public synchronized void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		// INITIALIZE THE LAYOUT OBJECTS
		myapp.getPreferencesObjNonXml().setLogText("Starting...\n");
		
		Log.d(TAG, "onStart - Starting Everything");
		
		// INITIALIZE THE DATABASE OBJECT
		Log.d(TAG, "onStart - OpenDB");
		db_adapter = new DBAdapter(BackgroundService.this);
		// Open the database
		db_adapter.open();
		
		////////////////////////////////////////////////////
		// Find the last elements id of the wifi table and 
		// the phone table before
		// new ones are added
		myapp.getPreferencesObjNonXml().setWifiLastID(db_adapter.lastEntryId(WifiInterface.DATABASE_TABLE_WIFI));
		myapp.getPreferencesObjNonXml().setPhoneLastID(db_adapter.lastEntryId(TelephoneInterface.DATABASE_TABLE));
		// Info print
		Log.i(TAG, "Phone Last Entry:" + String.valueOf(myapp.getPreferencesObjNonXml().getPhoneLastID()));
		Log.i(TAG, "Wifi Last Entry:" + String.valueOf(myapp.getPreferencesObjNonXml().getWifiLastID()));
		
		////////////////////////////////////////////////////
		
		///////////////////////////////////////
		// Set the starting state of the gps //
		///////////////////////////////////////
		
		
		Log.d(TAG, "onStart - Location Listener paramemeters");
		myCriteria = new Criteria();							// Create the criteria object
		myCriteria.setAccuracy(Criteria.ACCURACY_FINE);			// Set the accuracy to fine
		if(myLockMng.getBestProvider(myCriteria, true) != null){
			myLockMng.addGpsStatusListener(myGpsStatusListener);	// Register the gps listener before requesting location updates
			myLockMng.requestLocationUpdates(						// Request the location update
			   		myLockMng.getBestProvider(myCriteria, true),	// Return the best provider, knowing the criteria
			   		0,												// Time to wait for requesting updates
			   		0,												// Distance to wait for requesting updates
			   		myLockListen									// Returns the data to the listener
			   		);
			// Update status icons
			StartUpActivity.gpsStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gps_blue_150_150));
			myapp.getPreferenceObjIconState().setGpsStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gps_blue_150_150));
		}
	    ///////////////////////
		// Start the threads //
	    ///////////////////////
	    
		// Start the phone state local sampling thread
	    // This thread sends the data to the local smarthphone database
	    Log.d(TAG, "onStart - Start PhoneState sampling thread");
	    if(!myPhoneState.isSampleThreadRunning()){
			myPhoneState.startSamplingThread();
		}
	    
	    
	    // Start the wifi state local sampling thread
	    // This thread sends the data to the local smartphone database
	    Log.d(TAG, "onStart - Start WifiState sampling thread");
	    if(!myWifiState.isSampleThreadRunning()){
	    	myWifiState.startSamplingThread();
	    }
		
		
		// Do not let the device go to sleeping mode
		Log.d(TAG, "onStart - Do not let me sleep");
		myWakeLock.acquire();
		// Register the broadcast receiver
		this.registerReceiver(receiver, ServerIntent);
		
		Log.d(TAG , "onStart - Started everything");
		myapp.getPreferencesObjNonXml().setLogText("Started...\n");
		
		if(db_adapter!= null){
			db_adapter.close();
			db_adapter = null;
		}
		
	}

	////////////////////////////////////////////////
	/////////// STOP THE SERVICE (METHOD) //////////
	////////////////////////////////////////////////

	@Override
	public synchronized void onDestroy() {
		displayNotificationMessage("Stopping Background Service");
		super.onDestroy();
		
		////////////////////////////////////////////////////////////
		/////		SEND ALL THE TUPLES TOGETHER		////////////
		////////////////////////////////////////////////////////////
		
		// Process value == 2 means that you send samples on exit
		if( Integer.parseInt(myapp.getPreferenceObj().getProcessValue()) == 2 ){
			
			// wifi_tuples
			ArrayList<String[]> wifi_tuples = massiveTuples(	BackgroundService.this.getApplicationContext(),
														WifiInterface.DATABASE_TABLE_WIFI,
														WifiInterface.OML_WIFI_SCHEMA_ELEMENTS,
														myapp.getPreferencesObjNonXml().getWifiLastID());
			
			// phone_tuples
			ArrayList<String[]> phone_tuples = massiveTuples(BackgroundService.this.getApplicationContext(),
														TelephoneInterface.DATABASE_TABLE,
														TelephoneInterface.OML_PHONE_SCHEMA_ELEMENTS,
														myapp.getPreferencesObjNonXml().getPhoneLastID());
			
			if(wifi_tuples != null)
				Log.i(TAG,"num of wifi tuples:" + String.valueOf(wifi_tuples.size()));
			if(phone_tuples != null)
				Log.i(TAG,"num of phone tuples:" + String.valueOf(phone_tuples.size()));
			
			// Send to oml
			Intent i = new Intent();
			if(phone_tuples != null)
				i.putExtra("phone_tuples", phone_tuples);
			if(wifi_tuples != null)
				i.putExtra("wifi_tuples", wifi_tuples);
			
			if(!(wifi_tuples == null && phone_tuples == null)){
				i.setClass(BackgroundService.this, AsyncOmlTextProtocolPushsService.class);
				startService(i);
				Log.i(TAG,"Service started");
			}
		}
		
		////////////////////////////////////////////////////////////
		/////		DESTROYING EVERYTHING				////////////
		////////////////////////////////////////////////////////////
		
		
		Log.d(TAG , "onDestroy - start Cleaning");
		// FINALIZE THE LAYOUT OBJECTS
		myapp.getPreferencesObjNonXml().setLogText("Closing...\n");
		StartUpActivity.gpsStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gps_red_150_150));
		myapp.getPreferenceObjIconState().setGpsStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gps_red_150_150));
		
		StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
		myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
		
		StartUpActivity.PhoneStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.state_red_150_150));
		myapp.getPreferenceObjIconState().setPhoneStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.state_red_150_150));
		
		StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
		myapp.getPreferenceObjIconState().setOperatorStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
		
		StartUpActivity.ggStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
		myapp.getPreferenceObjIconState().setGgStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
		
		StartUpActivity.ServerIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));
		myapp.getPreferenceObjIconState().setServerStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));
		
		StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
		myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
		
		//StartUpActivity.NitlabInfo.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
		//myapp.getPreferenceObjIconState().setNitlabStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
		
		/**
		 * UREGISTER LISTENERS AND MONITORING INTERFACES
		 */	
		if(receiver != null)
			this.unregisterReceiver(receiver);
		if(gpsObserver != null)
			getContentResolver().unregisterContentObserver(gpsObserver);
		
		/**
		 *  Clean the monitoring managers
		 */
		if(myLockMng != null && myLockListen != null)
		{
			myLockMng.removeGpsStatusListener(myGpsStatusListener);
			myLockMng.removeUpdates(myLockListen);
			myLockMng = null;
			myLockListen = null;
		}
		
		/**
		 *  UNREGISTER PHONESTATE LISTENERS AND CLEAN
		 */
		if(myTelMgr != null){
			myTelMgr.listen(ioiListener, PhoneStateListener.LISTEN_NONE);
			myTelMgr = null;
		}
		
		if(ioiListener != null){
			ioiListener = null;
		}
		
		/**
		 * SAMPLING PHONE INTERFACE THREAD CLEAN
		 */
		Log.d(TAG, "onDestroy - cleaning local phonestate thread");
		// Stop the sampling thread and clean the object
		if(myPhoneState != null){
			myPhoneState.stopSamplingThread();
			while(myPhoneState.isSampleThreadRunning()){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			myPhoneState = null;
		}
		Log.d(TAG, "onDestroy - cleaned local phonestate thread");
		
		
		Log.d(TAG, "onDestroy - cleaning local wifistate thread");
		/**
		 *  SAMPLING WIFI INTERFACE THREAD CLEAN
		 */
		if(myWifiState != null){
			myWifiState.stopSamplingThread();
			while(myWifiState.isSampleThreadRunning()){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			myWifiState = null;
		}
		Log.d(TAG, "onDestroy - cleaned local wifistate thread");
		
		
		/**
		 *  RELEASE THE NON SLEEP PHONE STATE MODE
		 */
		if (myWakeLock.isHeld())
			myWakeLock.release();
		
		
		/**
		 * CLEAN THE SERVICES
		 */
		if(myConMgr != null)
			myConMgr = null;
		if(myWifiMgr != null)
			myWifiMgr = null;
		
		
		myapp.getPreferencesObjNonXml().setLogText("Closed...\n");
		myapp.getPreferencesObjNonXml().setLogText("---------\n");
		
		/**
		 *  CLEAN THE SHARED APPLICATION PREFERENCE OBJECT
		 */
		if(myapp != null){
			// Close the server connection
			myapp.terminateOmlObject();
			// Set the status icon
			myapp.getPreferenceObjIconState().setServerStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));
			StartUpActivity.ServerIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));
			// Finalize the applicatin item
			myapp = null;
		}
		
		// I am awakened and i am cleaning
		Log.d(TAG,"onDestroy - Everything cleaned");
	}

	// Service override unimplemented method
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public synchronized TelephonyManager retTelMgr(){
		return myTelMgr;
	}

	/**
	 * 
	 * @param message : the message to be displayed as a notification
	 */
	private void displayNotificationMessage(String message) {
		Notification notify = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		
		// The service is not running
		if(!isServiceRunning(("." + BackgroundService.class.getSimpleName()).trim())){
			Intent start = new Intent();
			start.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			// Notification that does not redirect to other Activities
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(BackgroundService.this, "Nitalb - BackgroundService", message, contentIntent);
			notificationMgr.notify(R.string.app_notification_id, notify);
		}else{	// The service is running
			Intent start = new Intent(BackgroundService.this, StartUpActivity.class);
			start.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// Notification that redirects to another Activity
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(this, "Nitalb - BackgroundService", message, contentIntent);
			notificationMgr.notify(R.string.app_notification_id, notify);
		}
	}	
	
	
	/**
	 *  --- SERVICE CHECK CONTROL USING THE SYSTMEM
	 * @param serviceName
	 * @return boolean
	 * Check if the service is running
	 */
	public boolean isServiceRunning(String serviceName) {
		boolean serviceRunning = false;
		ActivityManager am = (ActivityManager) BackgroundService.this.getSystemService(ACTIVITY_SERVICE);
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
	 * @author menios junior
	 *
	 */
	private class LocationContentObserver extends ContentObserver {
		public LocationContentObserver( Handler h ) {
		    super( h );
		}
	
		public void onChange(boolean selfChange) {
			// Start Location monitoring
			if(myLockMng!= null && myLockMng.getBestProvider(myCriteria, true) != null){
				myLockMng.addGpsStatusListener(myGpsStatusListener);	// Register the gps listener before requesting location updates
				myLockMng.requestLocationUpdates(						// Request the location update
				   		myLockMng.getBestProvider(myCriteria, true),	// Return the best provider, knowing the criteria
				   		0,												// Time to wait for requesting updates
				   		0,												// Distance to wait for requesting updates
				   		myLockListen									// Returns the data to the listener
				   		);
			}
			if(myLockMng.getBestProvider(myCriteria, true) != null){
				displayNotificationMessage("Gps Status : On");
			} else{
				displayNotificationMessage("Gps Status : Off");
			}
		}
	}
	
	/**
	 * 
	 * @author menios junior
	 *
	 */
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action != null){
				if( action.equals("com.ioigoume.eketamobilitytool.NOSERVER") ){	
					myapp.terminateOmlObject();
					myapp.getPreferenceObjIconState().setServerStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));
					StartUpActivity.ServerIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));
				}
				if( action.equals("com.ioigoume.eketamobilitytool.SERVERON") ){
					// Try to connect to server
					if( Integer.parseInt(myapp.getPreferenceObj().getProcessValue()) == 1 ){
						myapp.getpreferencesOmlObject();
					}
				}
			
				if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
	                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
	                        WifiManager.WIFI_STATE_UNKNOWN));
	            }
				
				if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
					
				}
				
				if(action.equals("com.ioigoume.eketamobilitytool.NEIGHBOURHOOD")){
					
					List<NeighboringCellInfo> cellList = (List<NeighboringCellInfo>) myTelMgr.getNeighboringCellInfo();
					if(cellList != null){
						myPhoneState.setnCellList(cellList);
					}else{
						Log.d(TAG, "GetNeighbourhoodCells:Cell List is null.");
					}
					
				}
				
				// 3G AND WIFI CONNECTION CHECK
				if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
					try{
						handleConnectionChange((NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));
						handleNoConnection(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
					} 
					//catch(NetworkOnMainThreadException e){
					//	Log.e(TAG,"Connect socket from a different thread.");
					//} 
					catch(Exception e){
						Log.e(TAG,"Connect socket from a different thread.");
					}
				}
				
				// WIFI SCAN RESULTS
				if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
					handleWifiScanData(myWifiMgr.getScanResults());
				}
			}
			
		}
		
		// Handle the wifi scan result data
		private void handleWifiScanData(List<ScanResult> resultsList){
			// Check if there is a list and if this list is empty
			if(myWifiState != null){
				myWifiState.setAccessPointList(resultsList);
			}
		}
		
		private void handleNoConnection(boolean noconnection){
			// There is no internet connection available( 3G or Wifi unavailable )
			if(noconnection == true){
				// Set the shared property
				myapp.getPreferenceInternetStatus().setConnected(false);
				// Close the server
				myapp.terminateOmlObject();
				// Set the status icon
				StartUpActivity.ServerIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.server_off_150_150));				
			}else{
				// There is internet connection available
				myapp.getPreferenceInternetStatus().setConnected(true);
			}
		}
		
		/**
		 * 
		 * @param info
		 */
		private void handleConnectionChange(NetworkInfo info){
			switch (info.getType()){
			case ConnectivityManager.TYPE_WIFI:
				switch (info.getState()) {
	                case CONNECTED:
	                    WifiInfo wifiInfo = myWifiMgr.getConnectionInfo();
	                    if (wifiInfo.getSSID() == null || wifiInfo.getBSSID() == null) {
	                    	displayNotificationMessage("handleNetworkStateChanged: Got connected event but SSID or BSSID are null. SSID: "
	                                + wifiInfo.getSSID()
	                                + ", BSSID: "
	                                + wifiInfo.getBSSID() + ", ignoring event");
	                    	break;
	                        }
	                    displayNotificationMessage("Wifi Connected.");
	                    StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_green_150_150));
	                    /**
	                     *	CONNECT TO SERVER FOR DATA UPLOAD  
	                     */
	                    if( Integer.parseInt(myapp.getPreferenceObj().getProcessValue()) == 1 ){	
		                    // First disconnect if you are connected with other interface
		                    myapp.terminateOmlObject();
		                    // Reconnect throught the wifi interface
		                    myapp.getpreferencesOmlObject();
	                    }
	                    // Status icons
	                    myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_green_150_150));
	                    myapp.getPreferenceInternetStatus().setConnected(true);
	                    myapp.getPreferenceInternetStatus().setWifiInternetConnected(true);
	                    break;
	                case DISCONNECTED:
	                	displayNotificationMessage("Wifi Disconnected.");
	                	StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
	                	myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
	                	myapp.getPreferenceInternetStatus().setConnected(false);
	                	myapp.getPreferenceInternetStatus().setWifiInternetConnected(false);
	                    break;
					case CONNECTING:
						displayNotificationMessage("Wifi Connecting.");
						StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
						myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setWifiInternetConnected(false);
						break;
					case DISCONNECTING:
						displayNotificationMessage("Wifi Disconnecting.");
						StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
						myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setWifiInternetConnected(false);
						break;
					case SUSPENDED:
						displayNotificationMessage("Wifi Suspended.");
						StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
						myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setWifiInternetConnected(false);
						break;
					case UNKNOWN:
						displayNotificationMessage("Wifi Unknown.");
						StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
	                	myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
	                	myapp.getPreferenceInternetStatus().setConnected(false);
	                	myapp.getPreferenceInternetStatus().setWifiInternetConnected(false);
						break;
					default:
						break;
				}
				break;
				
				
			case ConnectivityManager.TYPE_MOBILE:
				switch (info.getState()) {
	                case CONNECTED:
	                    displayNotificationMessage("Mobile Internet Connected.");
	                    StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_green_150_150));
	                    // 1 : only through wifi
	                    // 2 : any available interface
	                    if(Integer.parseInt(myapp.getPreferenceObj().getUploadChoice()) == 2){
	                    	if( Integer.parseInt(myapp.getPreferenceObj().getProcessValue()) == 1 ){
	                    		myapp.getpreferencesOmlObject();
	                    	}
	                    }
	                    // Set state icon
	                    myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_green_150_150));	                    
	                    myapp.getPreferenceInternetStatus().setConnected(true);
	                    myapp.getPreferenceInternetStatus().setPhoneInternetConnected(true);
	                    break;
	                case DISCONNECTED:
	                	displayNotificationMessage("Mobile Internet Disconnected.");
	                	StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
	                	myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
	                	myapp.getPreferenceInternetStatus().setConnected(false);
	                	myapp.getPreferenceInternetStatus().setPhoneInternetConnected(false);
	                    break;
					case CONNECTING:
						displayNotificationMessage("Mobile Internet Connecting.");
						StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_blue_150_150));
						myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_blue_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setPhoneInternetConnected(false);
						break;
					case DISCONNECTING:
						displayNotificationMessage("Mobile Internet Disconnecting.");
						StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_blue_150_150));
						myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_blue_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setPhoneInternetConnected(false);
						break;
					case SUSPENDED:
						displayNotificationMessage("Mobile Internet Suspended.");
						StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
						myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setPhoneInternetConnected(false);
						break;
					case UNKNOWN:
						displayNotificationMessage("Mobile Unknown.");
						StartUpActivity.DataMobileIndicator.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
						myapp.getPreferenceObjIconState().setDataStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gg_data_red_150_150));
						myapp.getPreferenceInternetStatus().setConnected(false);
						myapp.getPreferenceInternetStatus().setPhoneInternetConnected(false);
						break;
					default:
						break;
				}
				
			case ConnectivityManager.TYPE_WIMAX:
				if (info.isConnected()){}
					
				break;
			default:
				break;
			}
		}
		

        private void handleWifiStateChanged(int wifiState) {
            if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            	myWifiState.setAccessPointList(null);
            	StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
            	myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_red_150_150));
            	myapp.getPreferenceInternetStatus().setConnected(false);
            	displayNotificationMessage("Wifi Disabled.");
            } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            	StartUpActivity.wifiStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
            	myapp.getPreferenceObjIconState().setWifiStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.wifi_blue_150_150));
            	displayNotificationMessage("Wifi Enabled.");
            }
        }
	}
	
	/**
	 * 
	 * @author menios junior
	 *
	 */
	private class MyPhoneStateListener extends PhoneStateListener{
		@Override
		public synchronized void onDataConnectionStateChanged(int state, int networkType) {
			super.onDataConnectionStateChanged(state, networkType);
			try{
				myPhoneState.setNetworkType(networkType);
			}catch(NullPointerException e){
				if(myPhoneState == null)
					Log.d(TAG, "PhoneState is null");
			}
		}

		@Override
		public synchronized void onServiceStateChanged(ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);
			try{
				switch(serviceState.getState()){
					case ServiceState.STATE_EMERGENCY_ONLY:
						serviceStateIn = false;
						StartUpActivity.PhoneStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.state_blue_150_150));
						myapp.getPreferenceObjIconState().setPhoneStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.state_blue_150_150));
						Log.d("servicestate", "***********EMERGENCY ONLY********");
						break;
					case ServiceState.STATE_IN_SERVICE:
						serviceStateIn = true;
						StartUpActivity.PhoneStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.state_green_150_150));
						myapp.getPreferenceObjIconState().setPhoneStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.state_green_150_150));
						Log.d("servicestate", "***********IN SERVICE********");
						break;
					case ServiceState.STATE_OUT_OF_SERVICE:
						serviceStateIn = false;
						StartUpActivity.PhoneStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.state_red_150_150));
						myapp.getPreferenceObjIconState().setPhoneStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.state_red_150_150));
						Log.d("servicestate", "***********OUT OF SERVICE********");
						break;
					case ServiceState.STATE_POWER_OFF:
						serviceStateIn = false;
						StartUpActivity.PhoneStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.state_yellow_150_150));
						myapp.getPreferenceObjIconState().setPhoneStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.state_yellow_150_150));
						Log.d("servicestate", "***********POWER OFF********");
						break;
					default: break;
				} 
			}catch(NullPointerException e){
			
			}
		}
		
		@Override
		public synchronized void onSignalStrengthChanged(int asu) {
			super.onSignalStrengthChanged(asu);
		}
		
		@Override
		public synchronized void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			
			// Cell Location
			try{
				//myPhoneState.setNetworkType(myTelMgr.getNetworkType());
				CellLocation location = (CellLocation)myTelMgr.getCellLocation();
				myPhoneState.setCellId(String.valueOf(((GsmCellLocation)location).getCid()));
			} catch(NullPointerException e){
				Log.d(TAG, "no getCellLocation.");
			}		
			
			try{
				//myPhoneState.setGSMBer(signalStrength.getGsmBitErrorRate());
				myPhoneState.setGsmSignalStrength(signalStrength.getGsmSignalStrength());
				myPhoneState.setOperatorName(myTelMgr.getNetworkOperator());
			} catch(NullPointerException e){
				if(myPhoneState == null)
					Log.d(TAG, "PhoneState is null");
					
			}			
			
			if(myapp != null)
				myapp.getPreferencesObjNonXml().setLogText("----------\n");
			
		}

		@Override
		public synchronized void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			// Wait until proxy and client is initialized initialized
			try{
				 switch (state) {
				    case TelephonyManager.CALL_STATE_IDLE:
				        Log.d("phonestate", "***********IDLE********");
				        idlecallstate = true;
				        break;
				    case TelephonyManager.CALL_STATE_OFFHOOK:
				        Log.d("phonestate", "***********OFFHOOK********");
				        idlecallstate =false;
				        break;
				    case TelephonyManager.CALL_STATE_RINGING:
				        Log.d("phonestate", "***********RINGING********");
				        idlecallstate = false;
				        break;
				    default: break;
				    }
			}catch(NullPointerException e){
				
			}
			
		}

		@Override
		public synchronized void onDataActivity(int direction) {

			super.onDataActivity(direction);
			//Toast.makeText(BackgroundService.this, String.valueOf(direction), Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * 
	 * @author menios junior
	 *
	 */
	private class MyLocationListener implements LocationListener{
		public synchronized void onLocationChanged(Location location) {
			// synchronized function that take the new coordinates
			int lat = (int)(location.getLatitude()* 1E6);
			int lng = (int)(location.getLongitude()* 1E6);
			int alt = (int)(location.getAltitude()* 1E6);
			
			Log.d("onLocationChanged", String.valueOf(lat) + "  " + String.valueOf(lng) + " " + String.valueOf(alt) );
			
			// If my objects are null exit
			try{
				// PHONE INTERFACE
				myPhoneState.setLongtitude(lng);
				myPhoneState.setLatitunde(lat);
				myPhoneState.setAltitude(alt);
				
				// WIFI INTERFACE
				myWifiState.setLongtitude(lng);
				myWifiState.setLatitunde(lat);
				myWifiState.setAltitude(alt);
			} catch(NullPointerException e){
				if(myPhoneState == null || myWifiState == null){
					Log.d(TAG, "PhoneState or WifiState is null");
				}
			}
		}

		public synchronized void onProviderDisabled(String provider) {
			try{
				myPhoneState.setLongtitude(-1);
				myPhoneState.setLongtitude(-1);
				myPhoneState.setAltitude(-1);
			}catch(NullPointerException e){
				if(myPhoneState == null){
					Log.d(TAG, "PhoneState is null");
				}
			}
			Log.d("Location Listener", "Provider Disabled");
			
		}

		public synchronized void onProviderEnabled(String provider) {
			Log.d("Location Listener", "Provider Enabled");				
		}

		public synchronized void onStatusChanged(String provider, int status,
				Bundle extras) {
			Log.d("Location Listener", "Status Changed");
			
		}
	}
	
	/**
	 * 
	 * @author menios junior
	 *
	 */
	private class MyGpsStatusListener implements GpsStatus.Listener{
		public void onGpsStatusChanged(int event) {
			switch(event){
			case GpsStatus.GPS_EVENT_STARTED:
				StartUpActivity.gpsStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gps_blue_150_150));
				myapp.getPreferenceObjIconState().setGpsStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gps_blue_150_150));
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				StartUpActivity.gpsStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gps_red_150_150));
				myapp.getPreferenceObjIconState().setGpsStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gps_red_150_150));
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				myapp.getPreferencesObjNonXml().setLogText("GPS is ON!!!");
				StartUpActivity.gpsStateImage.setImageDrawable(BackgroundService.this.getResources().getDrawable(R.drawable.gps_green_150_150));
				myapp.getPreferenceObjIconState().setGpsStateIcon(BackgroundService.this.getResources().getDrawable(R.drawable.gps_green_150_150));
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				break;
			}
			
		}
	}
	
	/**
	 * 
	 * @author menios junior
	 *
	 */
	private class MyIntentFilter extends IntentFilter{
		MyIntentFilter(){
			this.addAction("com.ioigoume.eketamobilitytool.NOSERVER");
			this.addAction("com.ioigoume.eketamobilitytool.SERVERON");
			this.addAction("android.net.wifi.WIFI_STATE_CHANGED");
			this.addAction("android.net.wifi.STATE_CHANGE");
			this.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
			this.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			this.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			this.addAction("com.ioigoume.eketamobilitytool.NEIGHBOURHOOD");
		}
	}
	
	
	////////////////////////////////////////////////////////////////////
	//                          M E T H O D S
	////////////////////////////////////////////////////////////////////
	
	/**
	 * @return null if unconfirmed
	 */
	// Reflection approach
	@SuppressWarnings("unused")
	private synchronized Boolean isMobileDataEnabled(){
	    Object connectivityService = getSystemService(CONNECTIVITY_SERVICE); 
	    ConnectivityManager cm = (ConnectivityManager) connectivityService;

	    try {
	        Class<?> c = Class.forName(cm.getClass().getName());
	        Method m = c.getDeclaredMethod("getMobileDataEnabled");
	        m.setAccessible(true);
	        return (Boolean)m.invoke(cm);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
		
		/**
		 * @param ctx : Context
		 * @param table_name : String, the table i want to retrieve data from
		 * @param tuple_elements : String[], the columns of the table i want to retrieve
		 * @param max_id : long, the id under which i want to search for rows
		 * @return : List<String[]> : each entry contains an array with the elements obtained of one row
		 */
		protected synchronized ArrayList<String[]> massiveTuples(Context ctx, String table_name, String[] tuple_elements, long start_id) {
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
