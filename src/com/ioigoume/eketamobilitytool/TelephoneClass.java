package com.ioigoume.eketamobilitytool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.ioigoume.eketamobilitytool.database.DBAdapter;

import android.content.Intent;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

public class TelephoneClass extends CoordinationClass implements
		TelephoneInterface, ApplicationGlobalVars {

	private static final String TAG = "PhoneInterfaceClass";
	private int gsmSignalStrength;
	//private int gsmBER;
	private String cellId;
	private String operatorName;
	private int networkType;
	private HashMap<String, String> elementsHash = new HashMap<String, String>();
	private HashMap<String, String> VariableType = new HashMap<String, String>();
	private ArrayList<NeighbouringCells> nCellList = new ArrayList<NeighbouringCells>();
	
	private BackgroundService bckServ;
	private PhoneInterfaceThread sampleThread;
	private MobilityToolApplication myapp = null;
	// Initialize the database
	private DBAdapter list_db = null;

	/**
	 * CONSTRUCTOR
	 */
	public TelephoneClass(BackgroundService bckServ) {
		super();
		
		/////////////////////////////
		// VARIABLE INITIALIZATION //
		/////////////////////////////
	
		// Set initial values
		gsmSignalStrength = -1;
		//gsmBER = 0;
		cellId = "none";
		operatorName = "unknown";
		networkType = -1;
		this.bckServ = bckServ;
		myapp = (MobilityToolApplication) bckServ.getApplication();
		// Create an object of the database
		list_db = new DBAdapter(bckServ);
		
		
		// Set the elements list
		elementsHash.put(KEY_GSMSIGNALSTRENGTH, rssi_dbm_recompute(gsmSignalStrength));
		elementsHash.put(KEY_NETWORKTYPE, name_of_net_type(networkType));
		//elementsHash.put(KEY_GSMBER, ber_percent(gsmBER));
		elementsHash.put(KEY_GSMCELLID, cellId);
		elementsHash.put(KEY_OPERATORNAME, operatorName);
		elementsHash.put(KEY_LONGTITUDE, String.valueOf("-1"));
		elementsHash.put(KEY_LATITUNDE, String.valueOf("-1"));
		elementsHash.put(KEY_ALTITUDE, String.valueOf("-1"));
		

		// Set variables and variable types
		VariableType.put(KEY_NETWORKTYPE, "string");
		VariableType.put(KEY_OPERATORNAME, "string");
		VariableType.put(KEY_GSMSIGNALSTRENGTH, "long");
		//VariableType.put(KEY_GSMBER, "double");
		VariableType.put(KEY_GSMCELLID, "string");
		VariableType.put(KEY_LONGTITUDE, "long");
		VariableType.put(KEY_LATITUNDE, "long");
		VariableType.put(KEY_ALTITUDE, "long");
		
		//////////////////////
		// OBJECT CREATIONS //
		//////////////////////
		// Make this object available to the whole application
		myapp.getPreferencesObjNonXml().setPhoneObj(this);
		sampleThread = new PhoneInterfaceThread("SamplingThread");
	}

	/**
	 * @author giannesegoumenos
	 * @return print to log cat all the data for debugging
	 */
	public synchronized void print() {
		Log.d("NetState",
				"Sig stre = " + String.valueOf(this.getGsmSignalStrength()));
		Log.d("NetState", "Cell Id = " + this.getCellId());
		//Log.d("NetState", "BER = " + String.valueOf(this.getGSMBer()));
		Log.d("NetState", "Net Type = " + String.valueOf(this.getNetworkType()));
		Log.d("NetState", "Oper Name = " + String.valueOf(this.getOperatorName()));
		Log.d("NetState", "Latitunde = " + String.valueOf(this.getLatitunde()));
		Log.d("NetState","Longtitude = " + String.valueOf(this.getLongtitude()));
		Log.d("NetState","Altitude = " + String.valueOf(this.getAltitude()));
	}

	public synchronized int getGsmSignalStrength() {
		return gsmSignalStrength;
	}

	public synchronized void setGsmSignalStrength(int gsmSignalStrength) {
		this.gsmSignalStrength = gsmSignalStrength;

		elementsHash.put(KEY_GSMSIGNALSTRENGTH,
				rssi_dbm_recompute(gsmSignalStrength));
		myapp.getPreferencesObjNonXml().setLogText("Signal Strength: "
				+ String.valueOf(gsmSignalStrength) + "\n");
	}

	/*
	public synchronized int getGSMBer() {
		return gsmBER;
	}

	public synchronized void setGSMBer(int gsmBER) {
		this.gsmBER = gsmBER;
		elementsHash.put(KEY_GSMBER, String.valueOf(gsmBER));
		myapp.getPreferencesObjNonXml().setLogText("BER: " + String.valueOf(gsmBER) + "\n");
	}
	*/
	
	
	public synchronized String getCellId() {
		return cellId;
	}

	public synchronized void setCellId(String cellId) {
		this.cellId = cellId;
		elementsHash.put(KEY_GSMCELLID, cellId);
		myapp.getPreferencesObjNonXml().setLogText("cell id: " + cellId + "\n");
	}

	public synchronized String getOperatorName() {
		return operatorName;
	}

	public synchronized void setOperatorName(String operatorName) {
		this.operatorName = operatorNameCompany(operatorName);
		elementsHash.put(KEY_OPERATORNAME, operatorNameCompany(operatorName));
		setImageOperatorNameCompany(operatorName);
	}

	public synchronized String getNetworkType() {
		return name_of_net_type(networkType);
	}

	public synchronized void setNetworkType(int networkType) {
		this.networkType = networkType;
		elementsHash.put(KEY_NETWORKTYPE, name_of_net_type(networkType));
		myapp.getPreferencesObjNonXml().setLogText("Network Type:" + name_of_net_type(networkType) + "\n");
	}
	
	public synchronized void setLatitunde(int latitunde) {
		super.setLatitunde(latitunde);
		elementsHash.put(KEY_LATITUNDE, String.valueOf((int)latitunde));
	}

	public synchronized void setLongtitude(int longtitude) {
		super.setLongtitude(longtitude);
		elementsHash.put(KEY_LONGTITUDE, String.valueOf((int)longtitude));
	}
	
	public synchronized void setAltitude(int altitude) {
		super.setAltitude(altitude);
		elementsHash.put(KEY_ALTITUDE, String.valueOf((int)altitude));
	}

	public synchronized ArrayList<NeighbouringCells> getnCellList() {
		return nCellList;
	}

	public synchronized void setnCellList(final List<NeighboringCellInfo> dataList) {
		if(nCellList == null || dataList == null || dataList.isEmpty() == true){
			return;
		} // if
		
		// Number of access points the driver returned after scan
		//myapp.getPreferencesObjNonXml().setNumOfAccessPoints(resultsList.size());
		
		Thread listUpdate = new Thread(new Runnable(){
			public void run() {
					Log.d(TAG, "---------Start-----------");
					// Reset the nodes in the list
					NeighbourCellsAvailableReset(nCellList);
					// New elements
					Log.d(TAG, "--" + String.valueOf(dataList.size() + "--"));
					for(NeighboringCellInfo data : dataList){
						Log.d(TAG, "cell : " + data.getCid());
						cellListScanResultProcess(data, nCellList, dataList.size());
					}
							
					// Remove old elements
					cellNotAvailableRemove(nCellList);
				} // run
			});
		listUpdate.setName("NeighboringCell Thread.");
		listUpdate.start();
	}

	public synchronized HashMap<String, String> getVariableType() {
		return VariableType;
	}
	
	/**
	 * @return HashMap : key(String) -> Variable Name, value(String) -> Variable
	 *         Value
	 */
	public synchronized HashMap<String, String> netStateToHash() {
		/*
		 * if(elementsHash != null && !elementsHash.isEmpty()){
		 * Set<Entry<String,String>> data = elementsHash.entrySet();
		 * for(Entry<String,String> indata: data){ Log.d("elementHash:key: ",
		 * indata.getKey() + ",value: " + indata.getValue()); } }
		 */
		return elementsHash;
	}


	/**
	 * 
	 * Resolve the id of the network by its number
	 * 
	 * @param networkType
	 * @return network description string
	 */
	public String name_of_net_type(int networkType) {
		String returned_type = "unKnown";
		switch (networkType) {
		case 7:
			returned_type = "1xRTT";
			break;
		case 4:
			returned_type = "CDMA";
			break;
		case 2:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.twohalfg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.twohalfg_green_150_150));
			returned_type = "EDGE";
			break;
		case 14:
			returned_type = "eHRPD";
			break;
		case 5:
			returned_type = "EVDO rev. 0";
			break;
		case 6:
			returned_type = "EVDO rev. A";
			break;
		case 12:
			returned_type = "EVDO rev. B";
			break;
		case 1:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.twohalfg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.twohalfg_green_150_150));
			returned_type = "GPRS";
			break;
		case 8:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			returned_type = "HSDPA";
			break;
		case 10:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			returned_type = "HSPA";
			break;
		case 15:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			returned_type = "HSPA+";
			break;
		case 9:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.threeg_green_150_150));
			returned_type = "HSUPA";
			break;
		case 11:
			returned_type = "iDen";
			break;
		case 13:
			returned_type = "LTE";
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.fourg_green_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.fourg_green_150_150));
			break;
		case 3:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.threeg_blue_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.threeg_blue_150_150));
			returned_type = "UMTS";
			break;
		case 0:
			StartUpActivity.ggStateImage.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
			myapp.getPreferenceObjIconState().setGgStateIcon(bckServ.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
			returned_type = "Unknown";
			break;
		default:
			returned_type = String.valueOf(networkType);
			break;
		}

		return returned_type;
	}

	/**
	 * <rssi>: 0 -113 dBm or less 1 -111 dBm 2...30 -109... -53 dBm 31 -51 dBm
	 * or greater 99 not known or not detectable Taken from - 3GP
	 * TechnicalSpecifications 27.007 0 is low and 31 is good
	 */
	public String rssi_dbm_recompute(int signalStrength) {
		int sig = Integer.valueOf(signalStrength);

		// if there is no service or not knowns
		if (sig == 99 || sig == -1)
			return "-113";

		// What happens if we are in limits
		int dbmSig = -113 + sig * 2;

		if (dbmSig <= -113)
			return "-113";
		if (dbmSig >= -51)
			return "-51";

		return String.valueOf(dbmSig);
	}

	/**
	 * 
	 * Quality Band - Range of actual BER RXQUAL_0 - Less than 0,1 % RXQUAL_1 -
	 * 0,26 % to 0,30 % RXQUAL_2 - 0,51 % to 0,64 % RXQUAL_3 - 1,0 % to 1,3 %
	 * RXQUAL_4 - 1,9 % to 2,7 % RXQUAL_5 - 3,8 % to 5,4 % RXQUAL_6 - 7,6 % to
	 * 11,0 % RXQUAL_7 - Greater than 15,0 %
	 * 
	 */
	public String ber_percent(int ber) {
		String ret_val;

		switch (ber) {
		case 0:
			ret_val = "Less than 0,1%";
			break;
		case 1:
			ret_val = "0,26% to 0,30%";
			break;
		case 2:
			ret_val = "0,51% to 0,64%";
			break;
		case 3:
			ret_val = "1,0% to 1,3%";
			break;
		case 4:
			ret_val = "1,9% to 2,7%";
			break;
		case 5:
			ret_val = "3,8% to 5,4%";
			break;
		case 6:
			ret_val = "7,6% to 11,0%";
			break;
		case 7:
			ret_val = "Greater than 15,0%";
			break;
		default:
			ret_val = "Not defined";
			break;
		}
		return ret_val;
	}
	
	
	public String ber_approx_percent(int ber) {
		String ret_val;

		switch (ber) {
		case 0:
			ret_val = "0,1";
			break;
		case 1:
			ret_val = "0,28";
			break;
		case 2:
			ret_val = "0,57";
			break;
		case 3:
			ret_val = "1,2";
			break;
		case 4:
			ret_val = "2,3";
			break;
		case 5:
			ret_val = "4,5";
			break;
		case 6:
			ret_val = "9";
			break;
		case 7:
			ret_val = "15,0";
			break;
		default:
			ret_val = "-1";
			break;
		}
		return ret_val;
	}

	/**
	 * Decode the operators code 
	 * @param operator : String, code android returns
	 * @return operators brand name
	 */
	public String operatorNameCompany(String operator) {
		String ret_val;
		int l_operator = 0;
		
		if(operator == null)
			return null;
		
		try{
			l_operator = Integer.parseInt(operator);
		}catch(Exception e){
			l_operator = 0;
		}

		switch (l_operator) {
		case 20201:
			ret_val = "Cosmote";
			break;
		case 20205:
			ret_val = "Vodafone";
			break;
		case 20209:
			ret_val = "Wind";
			break;
		case 20210:
			ret_val = "Wind";
			break;
		default:
			ret_val = "Operator not in Greece.";
			break;
		}
		return ret_val;
	}
	
	/**
	 * 
	 * @param operator
	 */
	public void setImageOperatorNameCompany(String operator) {
		int l_operator = 0;
		if(operator == null)
			return;
		try{
			l_operator = Integer.parseInt(operator);
		}catch(Exception e){
			l_operator = 0;
		}

		switch (l_operator) {
		case 20201:
			StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.cosmote_transparent_150_150));
			myapp.getPreferenceObjIconState().setOperatorStateIcon(bckServ.getResources().getDrawable(R.drawable.cosmote_transparent_150_150));
			break;
		case 20205:
			StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.vodafone_150_150));
			myapp.getPreferenceObjIconState().setOperatorStateIcon(bckServ.getResources().getDrawable(R.drawable.vodafone_150_150));
			break;
		case 20209:
			StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.wind_150_150));
			myapp.getPreferenceObjIconState().setOperatorStateIcon(bckServ.getResources().getDrawable(R.drawable.wind_150_150));
			break;
		case 20210:
			StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.wind_150_150));
			myapp.getPreferenceObjIconState().setOperatorStateIcon(bckServ.getResources().getDrawable(R.drawable.wind_150_150));
			break;
		default:
			StartUpActivity.NetWorkOperatorIndicator.setImageDrawable(bckServ.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
			myapp.getPreferenceObjIconState().setOperatorStateIcon(bckServ.getResources().getDrawable(R.drawable.question_mark_blue_150_150));
			break;
		}
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
				sampleThread.setIsRunning(false);
				sampleThread.notifyAll();
				// sampleThread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public class NeighbouringCells{

		private String cid;
		private int signalstrength;
		private String networkType;
		
		private int num_of_neighbourcells;
		private boolean apAvailable;

		// Constructor
		public NeighbouringCells() {
			cid = "unknown";
			signalstrength = -95;
			networkType = "unknown";
			apAvailable = false;
			setNum_of_neighbourcells(0);
		}
		
		// Copy Constructor
		public NeighbouringCells(NeighbouringCells cell){
			this.cid = cell.getCid();
			this.signalstrength = cell.getSignalstrength();
			this.networkType = cell.getNetworkType();
			this.apAvailable = cell.isApAvailable();
			
		}

		/**
		 * @return the cid
		 */
		public synchronized String getCid() {
			return cid;
		}

		/**
		 * @param cid the cid to set
		 */
		public synchronized void setCid(String cid) {
			this.cid = cid;
			myapp.getPreferencesObjNonXml().setLogText("Cell Id:" + String.valueOf(cid) + "\n");
		}

		/**
		 * @return the signalstrength
		 */
		public synchronized int getSignalstrength() {
			return signalstrength;
		}

		/**
		 * @param signalstrength the signalstrength to set
		 */
		public synchronized void setSignalstrength(int signalstrength) {
			this.signalstrength = signalstrength;
			myapp.getPreferencesObjNonXml().setLogText("Cell Signal Strength:" + String.valueOf(signalstrength) + "\n");
		}

		/**
		 * @return the networkType
		 */
		public synchronized String getNetworkType() {
			return networkType;
		}

		/**
		 * @param networkType the networkType to set
		 */
		public synchronized void setNetworkType(int networkType_num) {
			this.networkType = name_of_net_type(networkType_num);
			myapp.getPreferencesObjNonXml().setLogText("Network Type:" + networkType + "\n");
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
		public  synchronized void setApAvailable(boolean apAvailable) {
			this.apAvailable = apAvailable;
		}

		public synchronized int getNum_of_neighbourcells() {
			return num_of_neighbourcells;
		}

		public synchronized void setNum_of_neighbourcells(int num_of_neighbourcells) {
			this.num_of_neighbourcells = num_of_neighbourcells;
		}
	} // Neighbouring Cell Class
	

	/** 
	 * 
	 */
	private class PhoneInterfaceThread extends Thread {
		private boolean isRunning;
		private MobilityToolApplication app;

		PhoneInterfaceThread(String ThreadName) {
			super(ThreadName);
			isRunning = false;
			app = (MobilityToolApplication) bckServ.getApplication();
		}

		public boolean isRunning() {
			return isRunning;
		}

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
				// Check if the gps geopoint is a precondition for collecting
				// the data
				StoreIfNoGeopoints = app.getPreferenceObj().isWanttostore();
				sendToDatabase = Integer.parseInt(app.getPreferenceObj().getProcessValue());
				// Request neigbouring cells
				bckServ.sendBroadcast(new Intent().setAction("com.ioigoume.eketamobilitytool.NEIGHBOURHOOD"));
				boolean coordinations_exist = ((double) getLatitunde() != -1 && (double) getLongtitude() != -1);
				Log.d(TAG, "Coordination exist:" + String.valueOf(coordinations_exist));
				// insert data to the data-table only if the gps is sampling or you 
				// have explicitly specified otherwise
				if ( coordinations_exist || StoreIfNoGeopoints == true) {				
					try{
						list_db.open();
						// Insert to database
						@SuppressWarnings("unused")
						long id = list_db.insertRecord(netStateToHash(), TelephoneInterface.DATABASE_TABLE);
						list_db.close();
						// if send on sample is enabled
						if(sendToDatabase == 1 && coordinations_exist){
							// Inject to database
							try{
								app.getpreferencesOmlObject().getOmlObj().inject(TelephoneInterface.DATABASE_TABLE, hashMapToStingArrayCollection(elementsHash, TelephoneInterface.OML_PHONE_SCHEMA_ELEMENTS));
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
		} // run

		@Override
		public synchronized void start() {
			super.start();
			isRunning = true;
		}

		@Override
		public void interrupt() {
			super.interrupt();
			isRunning = false;
		}

	} // Thread Class

	
	
	/**
	 * Reset the flag indicating that the neigbouring cell is available
	 * @param cells : List of neighbouring cells
	 */
	private synchronized void NeighbourCellsAvailableReset(final ArrayList<NeighbouringCells> cells){
		if(cells != null && !cells.isEmpty()){
			for(NeighbouringCells m_cells : cells ){
				m_cells.setApAvailable(false);
			}
		}		
	}
	
	/**
	 * Removes the neighbour cells that are no more available
	 * @param cells : ArrayList of Neighbour Cells Class
	 */
	// Remove should be performed on the iterators object and not the object
	// itself
	private synchronized void cellNotAvailableRemove(final ArrayList<NeighbouringCells> cells){
		if(cells!=null && !cells.isEmpty()){
			final ArrayList<NeighbouringCells> tmp_l = new ArrayList<NeighbouringCells>();

			// Check what nodes are false and create the tmp list
			for(NeighbouringCells cell : cells){
				if(cell.isApAvailable() == false){
					tmp_l.add(cell);
				}
			}

			TelephoneClass.this.runOnUiThread(new Runnable(){
				public void run() {
					// Remove the false from my list
					try{
						// Remove all from the list
						synchronized(NEIGHBOURCELL_LOCKER){
							cells.removeAll(tmp_l);
							cells.trimToSize();
							if(myapp.getPreferencesObjNonXml().getWifiAdaptOb() != null)
								myapp.getPreferencesObjNonXml().getWifiAdaptOb().notifyDataSetChanged();
						}
					} catch(Exception e){
						e.printStackTrace();
						Log.d(TAG,"Neighbour Cell list remove has exited with exception.");
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
	private synchronized void cellListScanResultProcess(NeighboringCellInfo data,final ArrayList<NeighbouringCells> cellList, int num_of_neighbours){
		// if the list is null return
		if(cellList == null || data == null)
			return;

		// if there is at least one object in my list check 
		// iterate and check. If you find the access point update.
		// If you don't create and add a new one.
		boolean cell_exists = false;
		for(NeighbouringCells cell : cellList){
			// if an access point from before
			if(String.valueOf(data.getCid()).trim().equals(cell.getCid())){
				cell.setCid(String.valueOf(data.getCid()));
				cell.setNetworkType(data.getNetworkType());
				cell.setSignalstrength(data.getRssi());				
				cell.setApAvailable(true);
				cell_exists = true;
			} // if
			// Change the num of accesspoints for every entry
			cell.setNum_of_neighbourcells(num_of_neighbours);
		}// for
		
		if(cell_exists == false){
			final NeighbouringCells cellElement = new NeighbouringCells();
			
			cellElement.setCid(String.valueOf(data.getCid()));
			cellElement.setNetworkType(data.getNetworkType());
			cellElement.setSignalstrength(data.getRssi());
			cellElement.setNum_of_neighbourcells(num_of_neighbours);
			cellElement.setApAvailable(true);
			
			TelephoneClass.this.runOnUiThread(new Runnable(){
				public void run() {
					try{
						synchronized(SCANRESULTLIST_LOCKER){
							cellList.add(cellElement);
							if(myapp.getPreferencesObjNonXml().getWifiAdaptOb() != null)
								myapp.getPreferencesObjNonXml().getWifiAdaptOb().notifyDataSetChanged();
						}
					} catch(Exception e){
						e.printStackTrace();
						Log.d(TAG,"Cell adding to list failed with exception.");
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
}
