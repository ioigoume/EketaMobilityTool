package com.ioigoume.eketamobilitytool;

import android.provider.BaseColumns;

import com.ioigoume.eketamobilitytool.database.DBAdapter;

public interface WifiInterface {
	/**
	 * CONSTANTS
	 */
	public static final String CUSTOM_LISTING = "access_point_list"; 
	
	/**
	 * ELEMENTS OF WIFI DATA TABLE - COLUMN HEADERS
	 */
	public static final String KEY_ROWID_WIFI = BaseColumns._ID;
	public static final String KEY_WIFISIGNALSTRENGTH = "Wifi_Signal_Strength";
	public static final String KEY_SSID = "SSID_network_name";
	public static final String KEY_CHANNEL = "Wifi_Channel_Frequency";
	public static final String KEY_CHANNEL_NUM = "Wifi_Channel_Num";
	public static final String KEY_BSSID = "Wifi_BSSID";
	public static final String KEY_CAPABILITIES = "Wifi_Capabilities";	
	public static final String KEY_AP_NUM = "Number_Of_Access_Points";
	public static final String KEY_LINK_SPEED = "Link_Speed";
	public static final String KEY_LONGTITUDE = "longtitude"; 
	public static final String KEY_LATITUNDE = "latitude"; 
	public static final String KEY_ALTITUDE = "altitude";
	/**
	 * 	NAME OF THE WIFI TABLE
	 */
	public static final String DATABASE_TABLE_WIFI = "Wifi_Measures";
	public static final String DATBASE_TABLE_PUBLIC_NAME = "Wifi Data";
	public static final String DATABASE_WIFI_SCHEMA = "2";
	/**
	 * 	STRING TABLE CONTAINING ALL THE COLUMN HEADERS
	 */
	public static final String[] WIFI_ELEMENTS = new String[] { 
			KEY_ROWID_WIFI,
			KEY_WIFISIGNALSTRENGTH, 
			KEY_SSID, 
			KEY_CHANNEL,
			KEY_CHANNEL_NUM,
			KEY_BSSID,
			KEY_CAPABILITIES,
			KEY_AP_NUM,
			KEY_LINK_SPEED,
			KEY_LONGTITUDE, 
			KEY_LATITUNDE, 
			KEY_ALTITUDE,
			DBAdapter.KEY_DATETIME };

	/**
	 * 	STRING CONTAINING THE CREATE SQL FOR THE TABLE
	 */
	public static final String DATABASE_CREATE_WIFI = String.format(
			"create table if not exists %s("
					+ "%s INTEGER primary key autoincrement,"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN');",
			DATABASE_TABLE_WIFI, 
			KEY_ROWID_WIFI,
			KEY_WIFISIGNALSTRENGTH, 
			KEY_SSID, 
			KEY_CHANNEL,
			KEY_CHANNEL_NUM,
			KEY_BSSID,
			KEY_CAPABILITIES,
			KEY_AP_NUM,
			KEY_LINK_SPEED,
			KEY_LONGTITUDE, 
			KEY_LATITUNDE, 
			KEY_ALTITUDE,
			DBAdapter.KEY_DATETIME);
	
	/**
	 * STRING CONTAINING THE DROP SQL COMMAND FOR THE TABLE
	 */
	public static final String DATABASE_WIFI_DROP = String.format(
			"drop table if exists %s", 
			DATABASE_TABLE_WIFI);
	
	/**
	 * STRING CONTAINING THE WIFI DATABASE TABLE SCHEMA EXISTING IN THE NITLAB SERVER 
	 */
	public static final String WIFI_SCHEMA = 	"signal_strength:long " +
												"bssid:string " +
												"ssid:string " +
												"channel_num:long " +
												"channel_fr:long " +
												"num_of_ap:long " +
												"capabilities:string " +
												"link_speed:long " +
												"longtitude:long " +
												"latitude:long " +
												"altitude:long " +
												"date:string";
	/**
	 * 	STRING TABLE CONTAINING THE SCHEMA ELEMENTS
	 */
	public static final String[] OML_WIFI_SCHEMA_ELEMENTS = new String[] { 
			KEY_WIFISIGNALSTRENGTH, 
			KEY_BSSID, 
			KEY_SSID,
			KEY_CHANNEL_NUM,
			KEY_CHANNEL,
			KEY_AP_NUM,
			KEY_CAPABILITIES,
			KEY_LINK_SPEED,
			KEY_LONGTITUDE, 
			KEY_LATITUNDE, 
			KEY_ALTITUDE,
			DBAdapter.KEY_DATETIME };
	
	/**
	 * PLOT NAMES
	 */
	public static final String WIFI_SIGNAL_STRENGTH = "Wifi Signal Strength";
	public static final String WIFI_CHANNEL_RATE = "Wifi Channel Rate";
	
	
	// CHANNEL 1
	public static int channel_l_1 = 2401;
	public static int channel_c_1 = 2412;
	public static int channel_u_1 = 2423;

	// CHANNEL 2
	public static int channel_l_2 = 2404;
	public static int channel_c_2 = 2417;
	public static int channel_u_2 = 2428;
	
	// CHANNEL 3
	public static int channel_l_3 = 2411;
	public static int channel_c_3 = 2422;
	public static int channel_u_3 = 2433;
	
	// CHANNEL 4
	public static int channel_l_4 = 2416;
	public static int channel_c_4 = 2427;
	public static int channel_u_4 = 2438;
	
	// CHANNEL 5
	public static int channel_l_5 = 2421;
	public static int channel_c_5 = 2432;
	public static int channel_u_5 = 2443;
	
	// CHANNEL 6
	public static int channel_l_6 = 2426;
	public static int channel_c_6 = 2437;
	public static int channel_u_6 = 2448;
	
	// CHANNEL 7
	public static int channel_l_7 = 2431;
	public static int channel_c_7 = 2442;
	public static int channel_u_7 = 2453;
	
	// CHANNEL 8
	public static int channel_l_8 = 2436;
	public static int channel_c_8 = 2447;
	public static int channel_u_8 = 2458;
	
	// CHANNEL 9
	public static int channel_l_9 = 2441;
	public static int channel_c_9 = 2452;
	public static int channel_u_9 = 2463;

	// CHANNEL 10
	public static int channel_l_10 = 2446;
	public static int channel_c_10 = 2457;
	public static int channel_u_10 = 2468;
	
	// CHANNEL 11
	public static int channel_l_11 = 2451;
	public static int channel_c_11 = 2462;
	public static int channel_u_11 = 2473;
	
	// CHANNEL 12
	public static int channel_l_12 = 2456;
	public static int channel_c_12 = 2467;
	public static int channel_u_12 = 2478;
	
	// CHANNEL 13
	public static int channel_l_13 = 2461;
	public static int channel_c_13 = 2472;
	public static int channel_u_13 = 2483;
	
	// CHANNEL 14
	public static int channel_l_14 = 2473;
	public static int channel_c_14 = 2484;
	public static int channel_u_14 = 2495;
	
	// CHANNEL NON-OVERLAPPING FREQUENCIES FOR 2.4GHz WLAN 
	// 802.11b
	public static int frequency_value_b = 22;
	public static String frequency_metric_b = "Mhz";
	public static int channel_1_non_b = 2412;
	public static int channel_6_non_b = 2437;
	public static int channel_11_non_b = 2462;
	public static int channel_14_non_b = 2484;
	
	// 802.11g/n
	public static int frequency_value_g_n = 20;
	public static String frequency_value_g_n_subcarriers = "16,5";
	public static String frequency_metric_g_n = "Mhz";
	public static int channel_1_non_g_n = 2412;
	public static int channel_5_non_g_n = 2432;
	public static int channel_9_non_g_n = 2452;
	public static int channel_13_non_g = 2472;
	
	// 802.11g/n
	public static int frequency_value_n = 40;
	public static String frequency_value_n_subcarriers = "33,75";
	public static String frequency_metric_n = "Mhz";
	public static int channel_1_non_n = 2422;
	public static int channel_5_non_n = 2462;
	
	
	/**
	 * AS AN ACCESS POINT ID USE THE SSID AND NOT THE BSSID
	 */
	
}
