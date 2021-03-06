package com.ioigoume.eketamobilitytool.oml_base_proxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

public class OMLBase {
	/**
	 * The default maximum backoff exponent.
	 */
	static final int DEFAULT_MAXIMUM_BACKOFF_EXPONENT = 12;

	/**
	 * The default maximum number of tries to send a report. This value results in a retry
	 * time of about 8 hours with an unchanged retry count.
	 */
	static final int DEFAULT_MAXIMUM_RETRY_COUNT = DEFAULT_MAXIMUM_BACKOFF_EXPONENT + 5;
	
	private static final String TAG = "OMLBase";
	private String oml_exp_id;
	private String oml_name;
	private String oml_server;
	private String oml_app_name;
	// Serial number of tuble for each table in the database
	private HashMap<String, Integer> measurementPointCounter = new HashMap<String, Integer>();
	// Serial number the schema has in the header
	private HashMap<String, Integer> schemaCounter = new HashMap<String, Integer>();
 
	private StringBuilder schemaPart = new StringBuilder();
	private StringBuilder header = new StringBuilder();
	// Port to connect
	private int port;
	// Server to connect
	private String address;
	// The timestamp in secs
	private volatile long head_time;
	// schema counter
	private int schema_counter;
 
	// Object to hold the output data
	private PrintWriter out;
 
	// From String to 0.0.0.0
	private InetAddress addr;
	// Socket Address and port to connect to
	private SocketAddress servAddress;
	// Socket to connect to
	private Socket mysock;
	
	// Control variables
	// Boolean set header send to server
	private boolean headerPushed;
 
	/**
	 * OML_EXP_ID, OML_NAME, OML_SERVER - ARE ENVIROMENTAL VARIABLES
	 * @param oml_app_name : application name
	 */
	public OMLBase(String oml_app_name) {
		this(oml_app_name, System.getenv("OML_EXP_ID").toString(), System
				.getenv("OML_NAME").toString(), System.getenv("OML_SERVER")
				.toString());
	}
 
	/**
	 * @param oml_app_name: application name
	 * @param oml_exp_id: application id
	 * @param oml_name: oml database name
	 * @param oml_server: oml server and port, eg tcp:nitlab.inf.uth.gr:3003
	 */
	public OMLBase(String oml_app_name, String oml_exp_id, String oml_name,
			String oml_server) {
		this.oml_exp_id = oml_exp_id;
		this.oml_name = oml_name;
		this.oml_server = oml_server;
		this.oml_app_name = oml_app_name;
 
		schema_counter = 1;
		port = 0;
		address = "none";
		setHeaderPushed(false);
	}
 
	/**
	 * 1.CONNECT TO SERVER
	 * 2.CREATE THE HEADER
	 * 3.INJECT THE HEADER TO THE SERVER
	 */
	public synchronized boolean start(){
 
		/**
		 * CONNECT TO SERVER
		 */
		boolean connection = connectToServer(oml_server);
 
		/**
		 * CREATE THE HEADER
		 */
		create_head();
 
		/**
		 *  SEND THE HEADER
		 */
		boolean injection = inject_head();
		
		if( connection && injection ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * CLOSE THE CONNECTION TO THE SERVER
	 */
	public synchronized void close() {
		/**
		 * DISCONNECT FROM SERVER
		 */
		srvDisconnect();
	}
	
	
	/**
	 * SCHEMA DESCRIPTION
	 * The description of the schema used in each measurement stream is:
	 * a space-delimited concatenation of the following elements:
	 * 
	 *  - schema id
	 *  - name of the schema/ table
	 *  - a sequence of name, type pairs, one for each element. 
	 *    The name and type in each pair are separated by a ':'
	 *  
	 * Each client should number its measurement streams contiguously 
	 * starting from 1.
	 *
	 * ADD TABLE AND SCHEMA FOR THAT TABLE
	 * @param table_name: String - Name of the table to be added in the database
	 * @param variable_seq : String - Columns of the table, syntax and grammatic according to OML TEXT Protocol
	 */
	public synchronized void addmp(String table_name, String variable_seq) {
		schemaPart.append(	"schema: " + 
							String.valueOf(schema_counter) + // #2 : stream_id
							" " + 
							table_name + // #3 : table name 
							" " + 
							variable_seq + //#4 : data
							"\n" );
		
		schemaCounter.put(table_name, schema_counter);
		schema_counter++;
		// track the measurement schemas
		measurementPointCounter.put(table_name, 0);
 
	}
	
	/**
	 * SENDS SINGLE LINE TUPLE TO THE SERVER
	 * @param table_name
	 * @param data
	 * @return boolean status
	 */
	public synchronized boolean inject(String table_name, String[] data){
		StringBuilder msg = new StringBuilder();
		// Create the tuples from the inputs 
		// and return them in the <msg>
		if(!create_tuples(table_name,data,msg))
			return false;
 
		/**
		 * TRY SEND TO SERVER
		 */
		try {
			out.print(msg.toString());
			mysock.getOutputStream().flush();
			out.flush();
			Log.i(TAG,"Tuple injected.");
		} catch (IOException ioException) {
			Log.w(TAG,"I could not connect");
			srvDisconnect();
			return false;
		} catch (NullPointerException e) {
			Log.e(TAG, "Null Pointer Exception");
			return false;
		}
		return true;
	}
	
	
	/**
	 * SENDS MULTIPLE TUPLES TO THE SERVER
	 * @param table_name
	 * @param data
	 * @return boolean status
	 */
	public synchronized boolean inject_mass(String table_name, List<String[]> data){
		StringBuilder msg = new StringBuilder();
		// Create the tuples from the inputs 
		// and return them in the <msg>
		for(String[] tuple : data){
			if(!create_tuples(table_name,tuple,msg))
				return false;
			Log.i(TAG, "tuple:" + msg.toString());
			/**
			 * TRY SEND TO SERVER
			 */
			try {
				out.print(msg.toString());
				mysock.getOutputStream().flush();
				out.flush();
				msg.delete(0, msg.length());
				Log.v(TAG,"Injected");
			} catch (IOException ioException) {
				if (ioException instanceof SocketException && ioException.getMessage().contains("Permission denied")) {
					Log.e(TAG, "You don't have internet permission", ioException);
				}
				srvDisconnect();
				Log.w(TAG, "You could not connect.", ioException);
				return false;
				
			} catch (NullPointerException e) {
				Log.e(TAG, "Null Pointer Exception");
				srvDisconnect();
				return false;
			}
		}
		return true;
	}
	
	
	/*********************************************************************
	 * 				END OF TEXT PROTOCOL MAIN FUNCTIONS
	 *********************************************************************/

	
	/**
	 * JUST CREATE THE HEADER
	 */
	public synchronized void create_head(){
		// Take current time
		head_time = System.currentTimeMillis();
		float lcl_head_time = head_time/1000L;
		String time = String.valueOf(lcl_head_time);
		 
		header.append("protocol: 1\n");
		header.append("experiment-id: " + oml_exp_id + "\n");
		header.append("start_time: " + time + "\n");
		header.append("sender-id: " + oml_name + "-sender\n");
		header.append("app-name: " + oml_app_name + "\n");
	}

	/**
	 * SEND THE HEADER OF THE MESSAGE
	 * 
	 * @return
	 * @throws IOException 
	 */
	private synchronized boolean inject_head(){
		if(schemaPart.toString().isEmpty())
			return false;
		
		// Assembly the head
		StringBuilder msg = new StringBuilder();
		msg.append(header.toString());
		msg.append(schemaPart.toString());
		msg.append("content: text\n");
		msg.append("\n");
 
		try {
			Log.i(TAG, "head:\n" + msg.toString());
			out.print(msg.toString());
			mysock.getOutputStream().flush();
			out.flush();
		} catch (IOException ioException) {
			if (ioException instanceof SocketException && ioException.getMessage().contains("Permission denied")) {
				Log.e(TAG, "You don't have internet permission", ioException);
			}
			Log.w(TAG,"I could not connect.");
			srvDisconnect();
			return false;
		} catch (NullPointerException e) {
			Log.e(TAG, "Null Pointer Exception:inject head");
			return false;
		}
		return true;
	}
	
	/**
	 * Measurement Tuple Serialization
	 * Each tuple is serialized into a new-line terminated string with 
	 * all elements separated by a TAB. 
	 * 
	 * In addition, TREE NEW ELEMENTS are inserted before the measurements 
	 * themselves. These three elements are defined as follows:
	 * 
	 *  - time_stamp: A time stamp in seconds relative to the 
	 *                header's 'start-time'
	 *  - stream_id:  This is the same number as used in the 'schema' 
	 *                header definition
	 *  - seq_no: 	  A sequence number in the context of the 
	 *                specific measurement stream.
	 *   
	 *   The sequence numbers of each measurement stream is independent of 
	 *   the others. That is, the first measurement in a given stream should 
	 *   have *seq_no*=0, and all subsequent measurements in the stream 
	 *   should increment the sequence number by 1.
	 *
	 * CREATE TUPLE
	 */
	public synchronized boolean create_tuples(String table_name, String[] data, StringBuilder tuples){
		int seq_no = 0;
		int schema_id = 0;
		
		// msg is null
		if(tuples == null){
			return false;
		}
		
		if (!measurementPointCounter.isEmpty()) {
			seq_no = measurementPointCounter.get(table_name);
			measurementPointCounter.put(table_name, seq_no + 1);
		} else
			return false;
 
		if (!schemaCounter.isEmpty()) {
			schema_id = schemaCounter.get(table_name);
		} else
			return false;
 
		if (!isSrvConnected())
			return false;
 
		
		/**
		 * CALCULATE THE TIME INTERVAL
		 */
		long cur_time = System.currentTimeMillis();
		long dif_time = cur_time - head_time;
		float lcl_dif_time = dif_time/1000L;
		String dif_time_str = String.valueOf(lcl_dif_time);
 
		/**
		 * CREATE THE TUPLE STRING
		 */
		tuples.append(	dif_time_str + // #1 : timestamp
						"\t" + 
						String.valueOf(schema_id) + // #2 : stream_id
						"\t" + 
						String.valueOf(seq_no) // #3 : seq_no
					); 
		
		for (int i = 0; i < data.length; i++) { // #4 : data
			tuples.append("\t" + data[i]);
		}
		
		tuples.append("\n");
		//Log.i(TAG,"tuples : " + tuples.toString());
		return true;
	}
 
	
	//////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////
	///////////////			CONTROL FUNCTIONS			//////////////////
	//////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////
	
	
	public synchronized boolean isHeaderPushed() {
		return headerPushed;
	}
	
	public synchronized void setHeaderPushed(boolean headerPushed) {
		this.headerPushed = headerPushed;
	}
	
	public synchronized String getHeader(){
		return header.toString();
	}
	
	public synchronized String getSchema(){
		return schemaPart.toString();
	}
 
	public synchronized String getOml_exp_id() {
		return oml_exp_id;
	}
 
	public synchronized String getOml_name() {
		return oml_name;
	}
 
	public synchronized String getOml_server() {
		return oml_server;
	}
 
	public synchronized int getPort() {
		return port;
	}
 
	private synchronized void setPort(int port) {
		this.port = port;
	}
 
	public synchronized String getAddress() {
		return address;
	}
 
	private synchronized void setAddress(String address) {
		this.address = address;
	}
 
	public int getSchemaCounter() {
		return schema_counter;
	}
 
	private synchronized boolean connectToServer(String oml_server) {
		if (oml_server == null)
			return false;
 
		String[] server = oml_server.split(":");
		if (server.length != 3)
			return false;
 
		if (!server[0].toLowerCase().equals("tcp")) {
			Log.w(TAG,"No tcp tag in url.");
			return false;
		}
		
		setAddress(server[1]);
		setPort(Integer.valueOf(server[2]));
 
		// Connect to server
		if(srvConnect() == -1){
			return false;
		}else
			return true;
 
	}
 
	/**
	 *  Connect to the server
	 * @return -1 : No connection was established
	 *          0 : Connection was successfully established
	 */
	private synchronized int srvConnect() {
 
		try {
			// The address to connect to
			Log.i(TAG,"Address:" + getAddress());
			Log.i(TAG, "Port:" + String.valueOf(getPort()));
			addr = InetAddress.getByName(getAddress());
			// The address plus the port
			servAddress = new InetSocketAddress(addr, getPort());

			// Try to connect
			mysock = new Socket();
			// This socket will block/ try connect for 5s
			mysock.connect(servAddress, 5000);
			// Show me if i was connected successfully
			if (mysock.isConnected()) {
				if(out == null){
					out = new PrintWriter(mysock.getOutputStream(),true);
					Log.i(TAG,"New socket and new streamer was created.");
				}
			}
 
		} catch (NullPointerException e) {
			Log.e(TAG, "Null Pointer occured.");
			return -1;
		} catch (UnknownHostException e) {
			Log.w(TAG,"Server does not exist.");
			return -1;
		} catch (IOException e) {
			if (e instanceof SocketException && e.getMessage().contains("Permission denied")) {
				Log.w(TAG, "You don't have internet permission", e);
			} else if(e instanceof ConnectException && e.getMessage().contains("Connection refused")){
				Log.w(TAG, "Connection is refused, the service on the server is probably down.", e);
			} else {
				//e.printStackTrace();
				Log.w(TAG, "Could not connect");
			}
			return -1;
		}
 
		return 0;
	}
 
	/**
	 * Disconnect from server
	 * @return 0 : Connection closed successfully
	 * -1 - Connection was closed or could not be closed
	 */
	private synchronized int srvDisconnect() {
		if (mysock != null && mysock.isConnected()) {
			try {
				out.close();
				mysock.close();
			} catch (IOException e) {
				return -1;
			} catch (NullPointerException e) {
				return 0;
			} finally {
				out = null;
				mysock = null;
			}
			return 0;
		}
		return 0;
	}
 
	/**
	 * @return true : socket is connected
	 * @return false: socket is not connected
	 * 
	 */
	public synchronized boolean isSrvConnected() {
		boolean isConnected = false;
		try {
			isConnected = !mysock.isClosed();
		} catch (Exception e) {
			isConnected = false;
		}
		return isConnected;
	}
 
	/**
	 * Test if the Socket is open
	 * @return true : if the socket is open
	 */
	public synchronized boolean isSockOpen(){
		try{
			if(out.checkError()){
				return false;
			}
		}catch(Exception e){
			return false;
		}
		return true;
	}
}