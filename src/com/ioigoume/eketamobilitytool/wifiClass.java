package com.ioigoume.eketamobilitytool;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ioigoume.eketamobilitytool.database.DBAdapter;

public class wifiClass extends CoordinationClass implements ApplicationGlobalVars, WifiInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1537399648698425588L;
	public static final String TAG = "wificlass";
	private ArrayList<AccessPointClass> apList = new ArrayList<AccessPointClass>();
	private HashMap<String, String> VariableType = new HashMap<String, String>();
	private HashMap<String, String> wifi_elements_list = new HashMap<String, String>();
	private WifiClassThread sampleThread;
	private WifiManager myWifiMgr;
	
	private BackgroundService bckServ;
	private MobilityToolApplication myapp = null;
	// Initialize the database
	private DBAdapter list_db = null;

	wifiClass(WifiManager myWifiMgr, BackgroundService bckServ) {
		/////////////////////////////
		// VARIABLE INITIALIZATION //
		/////////////////////////////
		
		this.myWifiMgr = myWifiMgr; 
		this.bckServ = bckServ;
		
		// Put a reference of my object int the MobilityToolApplication
		// Get application returns null if call from within an Activities constructor
		myapp = ((MobilityToolApplication)bckServ.getApplication());
		// Create an object of the database
		list_db = new DBAdapter(bckServ);
		
		// Initialize the elements list
		wifi_elements_list.put(KEY_CHANNEL, "-1");
		wifi_elements_list.put(KEY_CHANNEL_NUM, "-1");
		wifi_elements_list.put(KEY_SSID, "unknown");
		wifi_elements_list.put(KEY_BSSID, "unknown");
		wifi_elements_list.put(KEY_CAPABILITIES, "unknown");
		wifi_elements_list.put(KEY_WIFISIGNALSTRENGTH, "-95");
		wifi_elements_list.put(KEY_AP_NUM, "0");
		wifi_elements_list.put(KEY_LINK_SPEED, "-1");
		wifi_elements_list.put(KEY_LATITUNDE, "-1");
		wifi_elements_list.put(KEY_LONGTITUDE, "-1");
		wifi_elements_list.put(KEY_ALTITUDE, "-1");
				
		// Set variables and variable types
		VariableType.put(KEY_CHANNEL, "long");
		VariableType.put(KEY_CHANNEL_NUM, "long");
		VariableType.put(KEY_SSID, "string");
		VariableType.put(KEY_BSSID, "string");
		VariableType.put(KEY_CAPABILITIES, "string");
		VariableType.put(KEY_WIFISIGNALSTRENGTH, "long");
		VariableType.put(KEY_AP_NUM, "long");
		VariableType.put(KEY_LINK_SPEED, "long");
		VariableType.put(KEY_LONGTITUDE, "long");
		VariableType.put(KEY_LATITUNDE, "long");
		VariableType.put(KEY_ALTITUDE, "long");
		
		
		//////////////////////
		// OBJECT CREATIONS //
		//////////////////////
		myapp.getPreferencesObjNonXml().setWifiObj(this);
		sampleThread = new WifiClassThread("Wifi Thread");
	}
	
	/**
	 * 
	 * @return List containing all the access Points detected
	 */
	public synchronized ArrayList<AccessPointClass> getAccessPointList(){
		return apList;
	}
	
	public synchronized void setAccessPointList(final List<ScanResult> resultsList){
		if(apList == null){
			return;
		} // if
		
		// Number of access points the driver returned after scan
		//myapp.getPreferencesObjNonXml().setNumOfAccessPoints(resultsList.size());
		myapp.getPreferencesObjNonXml().setLogText("----------");
		Thread listUpdate = new Thread(new Runnable(){
			public void run() {
					//Log.d(TAG, "---------Start-----------");
					synchronized(SCANRESULTLIST_LOCKER){
						// Reset the nodes in the list
						apListAvailableReset(apList);
						// New elements
						//Log.d(TAG,"Sum : --" + String.valueOf(resultsList.size() + "--"));
						synchronized(SCANRESULTLIST_LOCKER){
							// Check if there is anything in the list
							if(resultsList != null && resultsList.isEmpty() == false){
								// if you find something process it
								for(ScanResult rs : resultsList){
									//Log.d(TAG, "result : " + rs.SSID);
									apListScanResultProcess(rs, apList, resultsList.size());
								} // for
							}// if
						}
						// Remove old elements
						apListNotAvailableRemove(apList);
					}
				} // run
			});
		listUpdate.setName("ScanResult Thread.");
		listUpdate.start();
		
		//Log.w(TAG, "size of apList : " + String.valueOf(apList.size()));
	} // function

	
	public synchronized void setLatitunde(int latitunde) {
		super.setLatitunde(latitunde);
		wifi_elements_list.put(KEY_LATITUNDE,	String.valueOf((int)latitunde));
	}

	public synchronized void setLongtitude(int longtitude) {
		super.setLongtitude(longtitude);
		wifi_elements_list.put(KEY_LONGTITUDE,String.valueOf((int)longtitude));
	}

	public synchronized void setAltitude(int altitude) {
		super.setAltitude(altitude);
		wifi_elements_list.put(KEY_ALTITUDE, String.valueOf((int)altitude));
	}

	/**
	 * @return the wifi_elements_list
	 */
	public synchronized HashMap<String, String> getWifi_elements_list() {
		return wifi_elements_list;
	}
	
	/**
	 * 
	 * @return true: the thread is running
	 */
	public synchronized boolean isSampleThreadRunning() {
		return sampleThread.isRunning();
	}

	/**
	 * START THE THREAD
	 * 
	 * @return
	 */
	public synchronized boolean startSamplingThread() {
		sampleThread.start();
		if (sampleThread.isRunning())
			return true;
		else
			return false;
	}

	/**
	 * STOP THE THREAD
	 */
	public synchronized void stopSamplingThread() {
		synchronized (sampleThread) {
			try {
				sampleThread.interrupt();
				sampleThread.notifyAll();
				// sampleThread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param list : type ArrayList<AccessPointClass>
	 * @return : deep clone list
	 */
	protected synchronized ArrayList<AccessPointClass> deepCloneList(ArrayList<AccessPointClass> list){
		ArrayList<AccessPointClass> cloneList = new ArrayList<AccessPointClass>();
		for(AccessPointClass ap : list){
			cloneList.add(new AccessPointClass(ap));
		}
		
		return cloneList;
	}

	public class AccessPointClass{
		private int channel_frequency;
		private int channel_num;
		private String ssid;
		private int signalstrength;
		private String bssid;
		private String capabilities;
		private int num_accesspoints;
		private int link_speed;
		
		private boolean apAvailable;

		// Constructor
		public AccessPointClass() {
			channel_frequency = -1;
			channel_num = -1;
			ssid = "unknown";
			signalstrength = -105;
			bssid = "unknown";
			capabilities = "unknown";
			apAvailable = false;
			num_accesspoints = 0;
			link_speed = -1;
		}
		
		// Copy Constructor
		public AccessPointClass(AccessPointClass ap){
			this.ssid = ap.getSsid();
			this.signalstrength = ap.getSignalstrength();
			this.num_accesspoints = ap.getNum_accesspoints();
			this.channel_num = ap.getChannel_num();
			this.channel_frequency = ap.getChannel_frequency();
			this.capabilities = ap.getCapabilities();
			this.bssid = ap.getBssid();
			this.apAvailable = ap.isApAvailable();
			this.link_speed = ap.getLink_speed();
			
		}

		public synchronized int getChannel_frequency() {
			return channel_frequency;
		}

		public synchronized void setChannel_frequency(int channel_frequency) {
			this.channel_frequency = channel_frequency;
			setChannel_num(frequencyToChannelNum(channel_frequency));
		}

		public synchronized int getChannel_num() {
			return channel_num;
		}

		public synchronized void setChannel_num(int channel_num) {
			this.channel_num = channel_num;
			// Console Monitor
			myapp.getPreferencesObjNonXml().setLogText("Channel num:" + String.valueOf(channel_num) + "\n");
		}

		public synchronized String getSsid() {
			return ssid;
		}

		public synchronized void setSsid(String ssid) {
			this.ssid = ssid;
			// Console Monitor
			myapp.getPreferencesObjNonXml().setLogText("SSID:" + String.valueOf(ssid) + "\n");
		}

		public synchronized int getSignalstrength() {
			return signalstrength;
		}

		public synchronized int getLink_speed() {
			return link_speed;
		}

		public synchronized void setLink_speed(int link_speed) {
			this.link_speed = link_speed;
		}

		public synchronized void setSignalstrength(int signalstrength) {
			this.signalstrength = signalstrength;
			// Console Monitor
			myapp.getPreferencesObjNonXml().setLogText("Signal Strength:" + String.valueOf(signalstrength) + "\n");
		}

		public synchronized String getBssid() {
			return bssid;
		}

		public synchronized void setBssid(String bssid) {
			this.bssid = bssid;
			// Console Monitor
			myapp.getPreferencesObjNonXml().setLogText("BSSID:" + String.valueOf(bssid) + "\n");
		}

		public synchronized String getCapabilities() {
			return capabilities;
		}

		public synchronized void setCapabilities(String capabilities) {
			this.capabilities = capabilities;
			// Console Monitor
			myapp.getPreferencesObjNonXml().setLogText("Capabilities:" + String.valueOf(capabilities) + "\n");
		}


		/**
		 * @return the apAvailable
		 */
		public synchronized boolean isApAvailable() {
			return apAvailable;
		}

		/**
		 * @param apAvailable the apAvailable to set
		 */
		public synchronized void setApAvailable(boolean apAvailable) {
			this.apAvailable = apAvailable;
		}
		
		public int getNum_accesspoints() {
			return num_accesspoints;
		}

		public void setNum_accesspoints(int num_accesspoints) {
			this.num_accesspoints = num_accesspoints;
		}
	}
	
	/** 
	 * 
	 */
	private class WifiClassThread extends Thread {
		private boolean isRunning;
		private MobilityToolApplication app;

		WifiClassThread(String ThreadName) {
			super(ThreadName);
			isRunning = false;
			app = (MobilityToolApplication) bckServ.getApplication();
		}

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
			boolean StoreIfNoGeopoints = false;
			// 1: send on sampling
			// 2: send on service stop
			int sendToDatabase = 0;
  
			while (isRunning) {
				// Get the available access points
				myWifiMgr.startScan();
				
				// Check if the gps geopoint is a precondition for collecting the data
				StoreIfNoGeopoints = app.getPreferenceObj().isWanttostore();
				sendToDatabase = Integer.parseInt(app.getPreferenceObj().getProcessValue());
				boolean coordinations_exist = ((double) getLatitunde() != -1 && (double) getLongtitude() != -1);
				
				
				// insert data to the data-table only if the gps is sampling or 
				// there are no gps data if the user wishes to
				if ( coordinations_exist	|| StoreIfNoGeopoints == true) {
					// Add data to local database
					// A concurrency violation occurs due to multiple thread
					// Synchronization over the list is needed
					
					if(apList != null && !apList.isEmpty()){
						
						// Create a deep copy for processing
						ArrayList<AccessPointClass> tmp_apList = null;
						synchronized(SCANRESULTLIST_LOCKER){
							tmp_apList = deepCloneList(apList);
						}
						
						for(AccessPointClass ap : tmp_apList){
							wifi_elements_list.put(KEY_CHANNEL,	String.valueOf(ap.getChannel_frequency()));
							wifi_elements_list.put(KEY_CHANNEL_NUM,	String.valueOf(ap.getChannel_num()));
							wifi_elements_list.put(KEY_WIFISIGNALSTRENGTH,	String.valueOf(ap.getSignalstrength()));
							wifi_elements_list.put(KEY_SSID, String.valueOf(ap.getSsid()));
							wifi_elements_list.put(KEY_BSSID, String.valueOf(ap.getBssid()));
							wifi_elements_list.put(KEY_CAPABILITIES, String.valueOf(ap.capabilities));
							wifi_elements_list.put(KEY_LINK_SPEED, String.valueOf(ap.getLink_speed()));
							wifi_elements_list.put(KEY_AP_NUM, String.valueOf(apList.size()));
							try{
								list_db.open();
								list_db.insertRecord(getWifi_elements_list(), WifiInterface.DATABASE_TABLE_WIFI);
								list_db.close();
								// if send on sample is enabled
								if(sendToDatabase == 1  && coordinations_exist){
									// Inject to database
									try{
										app.getpreferencesOmlObject().getOmlObj().inject(WifiInterface.DATABASE_TABLE_WIFI, hashMapToStingArrayCollection(getWifi_elements_list(), WifiInterface.OML_WIFI_SCHEMA_ELEMENTS));
									} catch(NullPointerException i){
										Log.e(TAG,"Push to server is not possible due to null exception.");
									}
								}
							} catch(NullPointerException e){
								Log.e(TAG, "One element is null, unable to put entry in database.");
							} catch (IllegalStateException e){								
								Log.e(TAG, "Database is closed, unable to put entry in database.");
							} catch (Exception e){		
								Log.e(TAG, "Unknown exception, unable to put entry in database.");
							}
						} // for
					} // if
				} // if
				synchronized (this) {
					try {
						wait(Integer.valueOf(app.getPreferenceObj()
								.getSamplingTime()));

					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} // synchronized
			}// while
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
	
	/**
	 * Reset the flag indicating that the access point is available
	 * @param ap : List of access points
	 */
	private synchronized void apListAvailableReset(final ArrayList<AccessPointClass> ap){
		if(ap != null && !ap.isEmpty()){
			synchronized(SCANRESULTLIST_LOCKER){
				for(AccessPointClass m_ap : ap ){
					m_ap.setApAvailable(false);
				}
			}
		}		
	}
	
	/**
	 * Removes the access points that are no more available
	 * @param ap : ArrayList of Access Point Class
	 */
	// Remove should be performed on the iterators object and not the object
	// itself
	private synchronized void apListNotAvailableRemove(final ArrayList<AccessPointClass> ap){
		if(ap!=null && !ap.isEmpty()){
			final ArrayList<AccessPointClass> tmp_l = new ArrayList<AccessPointClass>();

			// Check what nodes are false and create the tmp list
			for(AccessPointClass ap_ob : ap){
				if(ap_ob.isApAvailable() == false){
					tmp_l.add(ap_ob);
				}
			}

			wifiClass.this.runOnUiThread(new Runnable(){
				public void run() {
					// Remove the false from my list
					try{
						// Remove all from the list
						synchronized(SCANRESULTLIST_LOCKER){
							ap.removeAll(tmp_l);
							ap.trimToSize();
							if(myapp.getPreferencesObjNonXml().getWifiAdaptOb() != null)
								myapp.getPreferencesObjNonXml().getWifiAdaptOb().notifyDataSetChanged();
						}
					} catch(Exception e){
						e.printStackTrace();
						Log.d(TAG,"AP list remove has exited with exception.");
					}
				}
			});			
		}
	}
	
	
	/**
	 * 
	 * @param data : ScanResult Object returned from WifiManager
	 * @param apList : ArrayList that contains the access points that are available
	 */
	private synchronized void apListScanResultProcess(ScanResult data,final ArrayList<AccessPointClass> apList, int num_of_accesspoints){
		// if the list is null return
		if(apList == null || data == null)
			return;
		
		// Take the info from the connected acess point
		WifiInfo wifinfo = myWifiMgr.getConnectionInfo();
		
		// if there is at least one object in my list check 
		// iterate and check. If you find the access point update.
		// If you don't create and add a new one.
		boolean ap_exists = false;
		for(AccessPointClass ap : apList){
			// if an access point from before
			if(data.BSSID.toString().trim()== ap.getBssid().toString().trim()){
				ap.setChannel_frequency(data.frequency);
				ap.setSignalstrength(data.level);
				ap.setSsid(data.SSID);
				ap.setBssid(data.BSSID);
				ap.setCapabilities(data.capabilities);
				ap.setApAvailable(true);
				ap_exists = true;
				try{
					if(wifinfo.getBSSID().trim().equals(ap.getBssid().trim())){
						ap.setLink_speed(wifinfo.getLinkSpeed());
					}else{
						ap.setLink_speed(-1);
					}
				}catch(NullPointerException e){
					Log.w(TAG, "Wifi Info is null.",e);
					ap.setLink_speed(-1);
				}
			} // if
			// Change the num of accesspoints for every entry
			ap.setNum_accesspoints(num_of_accesspoints);
		}// for
		
		if(ap_exists == false){
			final AccessPointClass apElement = new AccessPointClass();
			apElement.setBssid(data.BSSID);
			apElement.setChannel_frequency(data.frequency);
			apElement.setSignalstrength(data.level);
			apElement.setSsid(data.SSID);
			apElement.setCapabilities(data.capabilities);
			apElement.setNum_accesspoints(num_of_accesspoints);
			apElement.setApAvailable(true);
			try{
				if(wifinfo.getBSSID().trim().equals(apElement.getBssid().trim())){
					apElement.setLink_speed(wifinfo.getLinkSpeed());
				}else{
					apElement.setLink_speed(-1);
				}
			}catch(NullPointerException e){
				Log.w(TAG, "Wifi Info is null.",e);
				apElement.setLink_speed(-1);
			}
			
			wifiClass.this.runOnUiThread(new Runnable(){
				public void run() {
					try{
						synchronized(SCANRESULTLIST_LOCKER){
							apList.add(apElement);
							if(myapp.getPreferencesObjNonXml().getWifiAdaptOb() != null)
								myapp.getPreferencesObjNonXml().getWifiAdaptOb().notifyDataSetChanged();
						}
					} catch(Exception e){
						e.printStackTrace();
						Log.d(TAG,"AP adding to list failed with exception.");
					}	
				}
				
			});
					
		}// if
	}
	
	
	/**
	 * @param hashData : HashMap<String, String> : <key, element> : <Column Name, Column Value >
	 * @param elements : String[] : elements you want to chose from hashmap
	 * @return String[] : data for database
	 */
	private synchronized String[] hashMapToStingArrayCollection(HashMap<String, String> hashData, String[] elements){
		String[] mydata = new String[elements.length];
		int i=0;
		for(String element : elements){
			mydata[i] = hashData.get(element);
			i++;
		}
		// Use the current format to match with the plotting function
		SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa", Locale.ENGLISH);
		String currentDateTimeString = sf.format(new Date());
		mydata[mydata.length-1] = currentDateTimeString; 
		
		return mydata;
	}
	
	
	/**
	 * 
	 * @param frequency
	 *            : frequency in Mhz
	 * @return channel number
	 */
	private synchronized int frequencyToChannelNum(int frequency) {
		int channel = 0;
		switch (frequency) {
		case channel_c_1:
			channel = 1;
			break;
		case channel_c_2:
			channel = 2;
			break;
		case channel_c_3:
			channel = 3;
			break;
		case channel_c_4:
			channel = 4;
			break;
		case channel_c_5:
			channel = 5;
			break;
		case channel_c_6:
			channel = 6;
			break;
		case channel_c_7:
			channel = 7;
			break;
		case channel_c_8:
			channel = 8;
			break;
		case channel_c_9:
			channel = 9;
			break;
		case channel_c_10:
			channel = 10;
			break;
		case channel_c_11:
			channel = 11;
			break;
		case channel_c_12:
			channel = 12;
			break;
		case channel_c_13:
			channel = 13;
			break;
		case channel_c_14:
			channel = 14;
			break;
		default:
			break;
		}
		return channel;
	}
}
