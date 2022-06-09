package com.ioigoume.eketamobilitytool;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ioigoume.eketamobilitytool.customAdapterPhone.phoneListCustomAdapter;
import com.ioigoume.eketamobilitytool.customAdapterWifi.wifiListCustomAdapter;
import com.ioigoume.eketamobilitytool.oml_base_proxy.OMLBase;

public class MobilityToolApplication extends Application implements OnSharedPreferenceChangeListener, ServerConstantsInterface{

	private static final String TAG = "MobilityToolApplication"; 
	private PreferencesValues prefValues = null;
	private SharedPreferences myprefs = null;
	private PreferencesValuesNonXml nonXmlValues = null;
	private PreferenceIconState iconState = null;
	private PreferencesInternetStatus internetStatus = null;
	private PreferencesOmlObject omlObjPref = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		myprefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		myprefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		myprefs.unregisterOnSharedPreferenceChangeListener(this);
	}


	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		// Reset only for the values that are in the xml files
		prefValues = null;
		
		/*
		* CHECK IF THE SERVER IS SETTED SUCCESFULLY AND RESET THE OML OBJECT
		*/
		if(key == getString(R.string.prefs_key_ChooseServer)){
			omlObjPref = null;
		}
	}
	 
	// When getting create if it is null
	public PreferencesValues getPreferenceObj(){
		if(prefValues == null){
			String myValue = myprefs.getString(getString(R.string.key_samplingItem), "5000");
			String processValue = myprefs.getString(getString(R.string.key_serverUploadProcess), "1");
			String serverChoice = myprefs.getString(getString(R.string.prefs_key_ChooseServer), "1");
			String name = myprefs.getString(getString(R.string.prefs_key_PersonalInfoName), "none");
			String surname = myprefs.getString(getString(R.string.prefs_key_PersonalInfoSurname), "none");
			boolean store = myprefs.getBoolean(getString(R.string.prefs_key_BooleanStoreIfGpsExists), false);
			String uploadChoice = myprefs.getString(getString(R.string.key_upload), "2");
			
			prefValues = new PreferencesValues(myValue, processValue, serverChoice, surname, name, store, uploadChoice);
		}
		return prefValues;
	}
	
	// When getting create if it is null
	public PreferencesValuesNonXml getPreferencesObjNonXml(){
		if(nonXmlValues == null){
			nonXmlValues = new PreferencesValuesNonXml(0, 0, null, null, null, null, 0);
		}
		return nonXmlValues;
	} 
	
	// Get the icon state object
	public PreferenceIconState getPreferenceObjIconState(){
		if(iconState == null){
			iconState = new PreferenceIconState();
		}
		return iconState;
	}
	
	// Get the internet status object
	public PreferencesInternetStatus getPreferenceInternetStatus() {
		if(internetStatus == null){
			internetStatus = new PreferencesInternetStatus(false, false, false);
		}
		return internetStatus;
	}
	

	// Get the oml object
	public PreferencesOmlObject getpreferencesOmlObject(){
		if( omlObjPref == null){
			omlObjPref = new PreferencesOmlObject();
		}
		return omlObjPref;
	}
	
	// Close and terminate the oml object
	public void terminateOmlObject(){
		if(omlObjPref != null){
			omlObjPref.omlObjTerminate();
		}
		omlObjPref = null;
	}
	
	// Holds the oml text protocol instance status
	public class PreferencesOmlObject{
		private OMLBase omlObj;

		public PreferencesOmlObject(){
			
			String surname_name = getPreferenceObj().getSurname() + "_" + getPreferenceObj().getName();
			String appId = getApplicationContext().getResources().getString(R.string.title_activity_start_up) + "_" + surname_name;
			appId = appId.replaceAll(" ", "_");
			omlObj = new OMLBase(
					appId, 
					appId, 
					surname_name + "-exp2", 
					getPreferenceObj().getFullServerString());
			
			// Add the schema
			omlObj.addmp( TelephoneInterface.DATABASE_TABLE, TelephoneInterface.PHONE_SCHEMA);
			omlObj.addmp( WifiInterface.DATABASE_TABLE_WIFI, WifiInterface.WIFI_SCHEMA);
			
			Log.i(TAG, "OML text protocol header schema is created");
			
			// If i have not defined a server do not try to connect
			if(getPreferenceObj().getFullServerString() == "0"){
				return;
			}else{ 
				// if a server is defined connect
				Thread head_inject = new Thread(new Runnable(){
					public void run(){
							omlObj.start();
					}
				});
				try{
					head_inject.start();
					head_inject.join();
					if(omlObj.isSockOpen()){
						if( Integer.parseInt(((MobilityToolApplication)getApplicationContext()).getPreferenceObj().getProcessValue()) == 1 ){
							((MobilityToolApplication)getApplicationContext()).getPreferenceObjIconState().setServerStateIcon(getApplicationContext().getResources().getDrawable(R.drawable.server_on_150_150));
							StartUpActivity.ServerIndicator.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.server_on_150_150));
							Log.i(TAG, "OML text protocol header is injected");
						}
					}
				} catch(InterruptedException e){
					Log.w(TAG, "Head injection to server interrupted.");
				}
			}			
		}
		
		public OMLBase getOmlObj() {
			return omlObj;
		}

		public void omlObjTerminate() {
			if(omlObj != null){
				omlObj.close();
				StartUpActivity.ServerIndicator.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.server_off_150_150));
			}
			omlObj = null;
		}
		
		
	}
	
	
	
	// Holds the internet connection status
	public class PreferencesInternetStatus{
		private boolean connected;
		private boolean phoneInternetConnected;
		private boolean wifiInternetConnected;
		
		
		public PreferencesInternetStatus(boolean connected, boolean phoneInternetConnected, boolean wifiInternetConnected) {
			this.connected = connected;
			this.phoneInternetConnected = phoneInternetConnected;
			this.wifiInternetConnected = wifiInternetConnected;
		}
		
		/**
		 * @return the connected
		 */
		public boolean isConnected() {
			return connected;
		}

		/**
		 * @param connected the connected to set
		 */
		public void setConnected(boolean connected) {
			this.connected = connected;
		}

		/**
		 * @return the phoneInternetConnected
		 */
		public boolean isPhoneInternetConnected() {
			return phoneInternetConnected;
		}

		/**
		 * @param phoneInternetConnected the phoneInternetConnected to set
		 */
		public void setPhoneInternetConnected(boolean phoneInternetConnected) {
			this.phoneInternetConnected = phoneInternetConnected;
		}

		/**
		 * @return the wifiInternetConnected
		 */
		public boolean isWifiInternetConnected() {
			return wifiInternetConnected;
		}

		/**
		 * @param wifiInternetConnected the wifiInternetConnected to set
		 */
		public void setWifiInternetConnected(boolean wifiInternetConnected) {
			this.wifiInternetConnected = wifiInternetConnected;
		}

	}
	
	/////////// HOLDS THE VALUES OF ALL PREFERENCES IN XML FILES
	public class PreferencesValues{
		private String sampletime = null;
		private String processValue = null;
		@SuppressWarnings("unused")
		private String serverChoice = null;
		private String name = null;
		private String surname = null;
		private boolean wanttostore = false;
		private String uploadChoice = null;
		private String fullServerString = null;
		
		
		private boolean wanttoconnect = false;
		private String serverName = null;
		private String portName = null;
		
		
		PreferencesValues(String sampletime, String processValue, String serverChoice, String surname, String name, boolean store, String uploadChoice){
			this.sampletime = sampletime;
			this.processValue = processValue;
			this.serverChoice = serverChoice;
			
			setWantToConnect(serverChoice);
			setretServerName(serverChoice);
			setretPortNum(serverChoice);
			setSurname(surname);
			setName(name);
			setWanttostore(store);
			setUploadChoice(uploadChoice);
			setFullServerString(serverChoice);
		}
		
		public String getSamplingTime(){
			return sampletime;		
		}
		
		public String getProcessValue(){
			return processValue;
		}
		
		
		/**
		 * @param pushChoice the pushChoice to set
		 */
		public void setProcessValue(String processValue) {
			this.processValue = processValue;
		}

		
		
		private void setWantToConnect(String serverChoice){
			switch(Integer.valueOf(serverChoice)){
				case 1:
					this.wanttoconnect = false;
					break;
				case 2:
					this.wanttoconnect = true;
					break;
				default:
					this.wanttoconnect = false;
					break;
			}
		}
		
		public boolean getWantToConnect(){
			return wanttoconnect;
		}
		
		
		private void setretServerName(String serverChoice){
			switch(Integer.valueOf(serverChoice)){
			case 1:
				this.serverName = "none";
				break;
			case 2:
				this.serverName = nitlabServerURL;
				break;
			default:
				this.serverName = "none";
				break;
			}
		}
		
		public String retServerName(){
			return serverName;
		}
		
		private void setretPortNum(String serverChoice){
			switch(Integer.valueOf(serverChoice)){
			case 1:
				this.portName = "0";
				break;
			case 2:
				this.portName = nitlabServerPort;
				break;
			default:
				this.portName = "0";
				break;
			}
		}
		
		public String retPortNum(){
			return portName;
		}

		public String getFullServerString() {
			return fullServerString;
		}

		public void setFullServerString(String serverChoice) {
			switch(Integer.valueOf(serverChoice)){
			case 1:
				this.fullServerString = "0";
				break;
			case 2:
				this.fullServerString = nitlabServerURLPORTPROTO;
				break;
			default:
				this.fullServerString = "0";
				break;
			}
		}

		public String getSurname() {
			return surname;
		}

		private void setSurname(String surname) {
			this.surname = surname;
		}

		public String getName() {
			return name;
		}

		private void setName(String name) {
			this.name = name;
		}

		public boolean isWanttostore() {
			return wanttostore;
		}

		private void setWanttostore(boolean wanttostore) {
			this.wanttostore = wanttostore;
		}

		public String getUploadChoice() {
			return uploadChoice;
		}

		private void setUploadChoice(String uploadChoice) {
			this.uploadChoice = uploadChoice;
		}

		
	}
	

	/////////// HOLDS THE VALUES OF ALL PREFERENCES OUTSIDE XML FILES
	public class PreferencesValuesNonXml{
		private wifiClass wifiObj; 
		private TelephoneClass phoneObj;
		
		private wifiListCustomAdapter wifiAdaptOb;
		private phoneListCustomAdapter phoneAdaptOb;
		
		private StringBuilder logText;
		private int numOfAccessPoints;
		private long wifiLastID;
		private long phoneLastID;

		
		public PreferencesValuesNonXml( long wifiLastID, long phoneLastId, phoneListCustomAdapter phoneAdaptOb, wifiListCustomAdapter wifiAdaptOb, TelephoneClass phoneObj, wifiClass wifiObj, int numOfAccessPoints) {
			this.wifiObj = wifiObj;
			this.wifiAdaptOb = wifiAdaptOb;
			this.phoneObj = phoneObj;
			this.wifiAdaptOb = wifiAdaptOb;
			this.logText = new StringBuilder();
			this.wifiLastID = wifiLastID;
			this.phoneLastID = phoneLastId;
		}
		
		/**
		 * @return the wifiObj
		 */
		public synchronized wifiClass getWifiObj() {
			return wifiObj;
		}

		/**
		 * @param wifiObj the wifiObj to set
		 */
		public synchronized void setWifiObj(wifiClass wifiObj) {
			this.wifiObj = wifiObj;
		}

		public synchronized TelephoneClass getPhoneObj() {
			return phoneObj;
		}

		public synchronized void setPhoneObj(TelephoneClass phoneObj) {
			this.phoneObj = phoneObj;
		}

		public synchronized wifiListCustomAdapter getWifiAdaptOb() {
			return wifiAdaptOb;
		}

		public synchronized void setWifiAdaptOb(wifiListCustomAdapter wifiAdaptOb) {
			this.wifiAdaptOb = wifiAdaptOb;
		}

		public phoneListCustomAdapter getPhoneAdaptOb() {
			return phoneAdaptOb;
		}

		public void setPhoneAdaptOb(phoneListCustomAdapter phoneAdaptOb) {
			this.phoneAdaptOb = phoneAdaptOb;
		}

		public synchronized String getLogText() {
			return logText.toString();
		}

		public synchronized void setLogText(String logText) {
			this.logText.append(logText);
		}

		public int getNumOfAccessPoints() {
			return numOfAccessPoints;
		}

		public void setNumOfAccessPoints(int numOfAccessPoints) {
			this.numOfAccessPoints = numOfAccessPoints;
		}

		public long getPhoneLastID() {
			return phoneLastID;
		}

		public void setPhoneLastID(long phoneLastID) {
			this.phoneLastID = phoneLastID;
		}

		public long getWifiLastID() {
			return wifiLastID;
		}

		public void setWifiLastID(long wifiLastID) {
			this.wifiLastID = wifiLastID;
		}
	}



	/////////// HOLDS THE STATE OF THE ICONS
	public class PreferenceIconState{
		private Drawable wifiStateIcon;
		private Drawable phoneStateIcon;
		private Drawable gpsStateIcon;
		private Drawable serverStateIcon;
		private Drawable operatorStateIcon;
		private Drawable dataStateIcon;
		private Drawable ggStateIcon;
		private Drawable nitlabStateIcon;
		
		public PreferenceIconState(){
			this.wifiStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.wifi_red_150_150);
			this.phoneStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.state_red_150_150);
			this.ggStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150);
			this.nitlabStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.nitlab);
			this.gpsStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.gps_red_150_150);
			this.serverStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.server_off_150_150);
			this.operatorStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.question_mark_blue_150_150);
			this.dataStateIcon = MobilityToolApplication.this.getResources().getDrawable(R.drawable.gg_data_red_150_150);
		}

		/**
		 * @return the wifiStateIcon
		 */
		public synchronized Drawable getWifiStateIcon() {
			return wifiStateIcon;
		}

		/**
		 * @param wifiStateIcon the wifiStateIcon to set
		 */
		public synchronized void setWifiStateIcon(Drawable wifiStateIcon) {
			this.wifiStateIcon = wifiStateIcon;
		}

		/**
		 * @return the phoneStateIcon
		 */
		public synchronized Drawable getPhoneStateIcon() {
			return phoneStateIcon;
		}

		/**
		 * @param phoneStateIcon the phoneStateIcon to set
		 */
		public synchronized void setPhoneStateIcon(Drawable phoneStateIcon) {
			this.phoneStateIcon = phoneStateIcon;
		}

		/**
		 * @return the gpsStateIcon
		 */
		public synchronized Drawable getGpsStateIcon() {
			return gpsStateIcon;
		}

		/**
		 * @param gpsStateIcon the gpsStateIcon to set
		 */
		public synchronized void setGpsStateIcon(Drawable gpsStateIcon) {
			this.gpsStateIcon = gpsStateIcon;
		}

		/**
		 * @return the serverStateIcon
		 */
		public synchronized Drawable getServerStateIcon() {
			return serverStateIcon;
		}

		/**
		 * @param serverStateIcon the serverStateIcon to set
		 */
		public synchronized void setServerStateIcon(Drawable serverStateIcon) {
			this.serverStateIcon = serverStateIcon;
		}

		/**
		 * @return the operatorStateIcon
		 */
		public synchronized Drawable getOperatorStateIcon() {
			return operatorStateIcon;
		}

		/**
		 * @param operatorStateIcon the operatorStateIcon to set
		 */
		public synchronized void setOperatorStateIcon(Drawable operatorStateIcon) {
			this.operatorStateIcon = operatorStateIcon;
		}

		/**
		 * @return the dataStateIcon
		 */
		public synchronized Drawable getDataStateIcon() {
			return dataStateIcon;
		}

		/**
		 * @param dataStateIcon the dataStateIcon to set
		 */
		public synchronized void setDataStateIcon(Drawable dataStateIcon) {
			this.dataStateIcon = dataStateIcon;
		}

		/**
		 * @return the ggStateIcon
		 */
		public synchronized Drawable getGgStateIcon() {
			return ggStateIcon;
		}

		/**
		 * @param ggStateIcon the ggStateIcon to set
		 */
		public synchronized void setGgStateIcon(Drawable ggStateIcon) {
			this.ggStateIcon = ggStateIcon;
		}

		/**
		 * @return the nitlabStateIcon
		 */
		public synchronized Drawable getNitlabStateIcon() {
			return nitlabStateIcon;
		}

		/**
		 * @param nitlabStateIcon the nitlabStateIcon to set
		 */
		public synchronized void setNitlabStateIcon(Drawable nitlabStateIcon) {
			this.nitlabStateIcon = nitlabStateIcon;
		}
		
		
	}
}

