package com.ioigoume.eketamobilitytool;

import android.provider.BaseColumns;

import com.ioigoume.eketamobilitytool.database.DBAdapter;

public interface TelephoneInterface {
	
	
	
	/**
	 * 	ELEMENTS OF NET DATA TABLES - COLUMN HEADERS
	 */
	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_GSMSIGNALSTRENGTH = "signal_strength";
	public static final String KEY_NETWORKTYPE = "network_type";
	public static final String KEY_GSMCELLID = "gsm_cell_id";
	public static final String KEY_OPERATORNAME = "operator_name";
	public static final String KEY_LONGTITUDE = "longtitude"; 
	public static final String KEY_LATITUNDE = "latitude"; 
	public static final String KEY_ALTITUDE = "altitude";
	/**
	 * 	NAME OF THE NET TABLE
	 */
	public static final String DATABASE_TABLE = "phoneinterface_measures";
	public static final String DATBASE_TABLE_PUBLIC_NAME = "PhoneInterface Data";
	public static final String DATABASE_NET_SCHEMA = "1";
	/**
	 * 	STRING TABLE CONTAINING ALL THE COLUMN HEADERS
	 */
	public static final String[] NET_ELEMENTS = new String[] { 	
				KEY_ROWID,
				KEY_GSMSIGNALSTRENGTH, 
				KEY_NETWORKTYPE, 
				KEY_GSMCELLID,
				KEY_OPERATORNAME, 
				KEY_LONGTITUDE, 
				KEY_LATITUNDE, 
				KEY_ALTITUDE, 
				DBAdapter.KEY_DATETIME };
	/**
	 * 	STRING CONTAINING THE CREATE SQL FOR THE TABLE
	 */
	public static final String DATABASE_NET_CREATE = String.format(
			"create table if not exists %s("
					+ "%s INTEGER primary key autoincrement,"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN',"
					+ "%s VARCHAR not null default 'UNKNOWN');",
			DATABASE_TABLE, KEY_ROWID, KEY_GSMSIGNALSTRENGTH, KEY_NETWORKTYPE,
			KEY_GSMCELLID, KEY_OPERATORNAME, KEY_LONGTITUDE,
			KEY_LATITUNDE,KEY_ALTITUDE, DBAdapter.KEY_DATETIME);


	/**
	 * STRING CONTAINING THE DROP SQL COMMAND FOR THE TABLE
	 */
	public static final String DATABASE_NET_DROP = String.format(
			"drop table if exists %s", 
			DATABASE_TABLE);
	
	/**
	 * STRING CONTAININT THE SCHEMA OF THE PHONE TABLE IN NITLAB SERVER
	 */
	public static final String PHONE_SCHEMA = 	"signal_strength:long " +
												"network_type:string " +
												"cell_id:string " +
												"operator_name:string " +
												"longtitude:long " +
												"latitude:long " +
												"altitude:long " +
												"date:string";
	/**
	 * 	STRING TABLE CONTAINING THE ELEMENTS OF THE OML SCHEMA
	 */
	public static final String[] OML_PHONE_SCHEMA_ELEMENTS = new String[] { 	
		KEY_GSMSIGNALSTRENGTH, 
		KEY_NETWORKTYPE, 
		KEY_GSMCELLID,
		KEY_OPERATORNAME, 
		KEY_LONGTITUDE, 
		KEY_LATITUNDE, 
		KEY_ALTITUDE, 
		DBAdapter.KEY_DATETIME };
	
	
	/**
	 * PLOT NAMES
	 */
	public static final String PHONE_SIGNAL_STRENGTH = "Phone Signal Strength";
}
