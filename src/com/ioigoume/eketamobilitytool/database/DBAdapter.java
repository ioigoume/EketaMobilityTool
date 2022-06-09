package com.ioigoume.eketamobilitytool.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.TelephoneInterface;
import com.ioigoume.eketamobilitytool.WifiInterface;

public class DBAdapter {
	
	public ProgressDialog pgdlg = null;


	/**
	 * 	NAME & VERSION OF THE DATABASE
	 */
	public static final String DATABASE_NAME = "MeasuresDB";
	private static final int DATABASE_VERSION = 20;
	/**
	 * 	AUXILIARY VARIABLES
	 */
	private static final String DATABASE_TEMP_TABLE = "temp_table";
	public static final String KEY_DATETIME = "date_time";
	private static final String TAG = "DBAdapter";


	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	// /////////////////////////////////////////
	// //// CREATE THE DATABASE 		////////
	// /////////////////////////////////////////

	public class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			Log.d("DBHelper Constructor", DATABASE_NAME);
		}

		/**
		 * Creates the tables in the database
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			try {

				Log.d(TAG, "onCreate");
				db.execSQL(TelephoneInterface.DATABASE_NET_CREATE);
				db.execSQL(WifiInterface.DATABASE_CREATE_WIFI);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Upgrades the database from an older version to a newer one
		 */
		@Override
		public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", the data will be copied");

			HashMap<String, String> dbInfo = DBCreateComms(db);
			Set<Entry<String, String>> set = dbInfo.entrySet();
			for(Entry<String,String> setObj : set ){
				UpgradeDataBase(db, setObj.getKey(), DATABASE_TEMP_TABLE,
						setObj.getValue());
			}
			
			Log.d("OnUpgrade", "Finished upgrading");
		}

	}

	/**
	 * 
	 * @return The database
	 * @throws SQLException
	 */
	public synchronized DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close the database if not null and open
	 */
	public synchronized void close() {
		if (db != null) {
			if(db.isOpen())
				db.close();
		}
	}

	/**
	 * 
	 * @return SQLiteDataBase variable, the database object
	 */
	public synchronized SQLiteDatabase getDb() {
		return db;
	}
	
	/**
	 * Delete the database
	 */
	public synchronized void delDB(SQLiteDatabase db){
		final File mDataBaseFile;
		try{
			// Get the path of the database
			mDataBaseFile = context.getDatabasePath(DATABASE_NAME);
			// Close the database
			if(db != null && db.isOpen())
				db.close();
			// Delete the database file
			mDataBaseFile.delete();
			// Send the database object to garbage collector
			db = null;
		}catch(Exception e){
			e.printStackTrace();
			Log.d(TAG, "delDB:exited with exception.");
		}
	}
	
	/**
	 * Drop the tables from the database
	 */
	public synchronized void onDrop(SQLiteDatabase db) {
		try {

			Log.d(TAG, "onDrop");
			db.execSQL(TelephoneInterface.DATABASE_NET_DROP);
			db.execSQL(WifiInterface.DATABASE_WIFI_DROP);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d(TAG,"onDrop:exited with exception.");
		}
	}


	/**
	 * 
	 * @param list, 
	 * @param table_name , name of the database
	 * @return
	 */
	public synchronized long insertRecord(HashMap<String,String> list, 
											String table_name) {
		
		ContentValues initialValues = new ContentValues();
		// Use the current format to match with the plotting function
		SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa", Locale.ENGLISH);
		String currentDateTimeString = sf.format(new Date());

		if ( list == null ){
			return 0;
		}
		
		if (list != null) {
			Set<Entry<String,String>> setHash = list.entrySet();
			for(Entry<String, String> obj : setHash){
				initialValues.put(obj.getKey(), obj.getValue());
			}
		}
		
		initialValues.put(DBAdapter.KEY_DATETIME, currentDateTimeString);
		long ret = 0;
		try{
			ret = db.insertWithOnConflict(table_name, null, initialValues,
				SQLiteDatabase.CONFLICT_REPLACE);
			Log.d(TAG, "Inserted");
		} catch(SQLiteException e){
			Toast.makeText(context, "Delete the database and retry sampling.",	Toast.LENGTH_LONG).show();
			Log.e(TAG, "Not Inserted");
		} catch(Exception e){
			e.printStackTrace();
			Log.e(TAG, "Insert Record excited with unknown exception");
		}

		return ret;

	}

	/**
	 *  ---updates a record---
	 * @param rowId			: The row to be updated
	 * @param key_rowID		: The key_row id
	 * @param list			: HashMap object containing the data to be inserted to the db
	 * @param log			: longitude
	 * @param lat			: latitude
	 * @param table_name	: the name of the table that the record belongs to
	 * @return				: true if the update is completed successfully
	 */
	public synchronized boolean updateRecord(long rowId,
								String key_rowID, 
								HashMap<String,String> list,
								String table_name) {
		boolean update = false;
		ContentValues args = new ContentValues();
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(
				new Date());
		if (list == null)
			return false;
		if (list != null) {
			Set<Entry<String, String>> hashSet = list.entrySet();
			for(Entry<String, String> obj : hashSet){
				args.put(obj.getKey(), obj.getValue());
			}
		}

		args.put(KEY_DATETIME, currentDateTimeString);
		try{
			update = db.update(table_name, args, key_rowID + "=" + rowId, null) > 0;
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"updateRecord:exited with exception.");
		} 
		
		return update;
		
	}

	
	/**
	 * 
	 * @param table_name		: String variable, the table name that we will address the query
	 * @param table_headers		: String[] table, the table headers/columns we want to retrieve
	 * @param selection			: the where clause without the word where. Use single quotes for string variables. If null is passed all the rows will be returned.
	 * @return					: Cursor variable, if it succeeds it returns a cursor to a content provider
	 */
	// --- return the results of a query
	public synchronized Cursor myQuery(String table_name, String[] table_headers,
			String selection, String groupBy) {
		Cursor s = null;
		try{
		s = db.query(table_name, table_headers, selection, null, groupBy, null,
				null, null);
		}catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"myQuery:exited with exception.");
		}
		return s;
	}
	
	
	/**
	 * 
	 * @param table_name : the name of the table to run the query to
	 * @param db : the database object that holds the table
	 * @return a cursor containing the data, or null if an exception occurs
	 */
	public synchronized Cursor myDistinctQuery(String table_name,String column_name, SQLiteDatabase db){
		Cursor s = null;
		String sql = "";
		try{
			sql = String.format("select distinct %s from %s;",column_name, table_name);
			s = db.rawQuery(sql, null);
		}catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"myDistinctQuery:exited with exception.");
		}
		return s;
	}

	
	/**
	 * 
	 * @param rowId			: long variable, the unique row num we want to delete
	 * @param table_name	: String variable, the table name we will address the query
	 * @param key_rowID		: String variable, the header name of the unique row id
	 * @return				: boolean variable, true if the row is deleted successfully
	 */
	// ---deletes a particular record---
	public synchronized boolean deleteContact(long rowId, String table_name, String key_rowID) {
		boolean deleted = false;
		try{
			deleted = db.delete(table_name, key_rowID + "=" + rowId, null) > 0;
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"deleteContact:exited with exception.");
		}
		return deleted;
	}

	
	/**
	 * 
	 * @param table_headers	: String[] table, the table headers/columns we want to retrieve
	 * @param table_name	: String variable, the table name we will address the query
	 * @return				: Cursor variable, if it succeeds it returns a cursor to a content provider
	 */
	// ---retrieves all the records---
	public synchronized Cursor getAllRecords(String[] table_headers, String table_name) {
		Cursor c = null;
		try{
			c = db.query(table_name, table_headers, null, null, null, null,
				null, null);
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"getAllRecords:exited with exception.");
		}
		return c;
	} 

	
	/**
	 * 
	 * @param rowId			: long variable, the unique row num we want to get
	 * @param table_headers	: String[] table, the table headers/columns we want to retrieve
	 * @param table_name	: String variable, the table name we will address the query
	 * @param key_rowID		: String variable, the header name of the unique row id
	 * @return				: Cursor variable, if it succeeds it returns a cursor to a content provider
	 * @throws SQLException
	 */
	// ---retrieves a particular record---
	public synchronized Cursor getRecord(long rowId, String[] table_headers,
			String table_name, String key_rowID) throws SQLException {
		Cursor mCursor = null;
		try{
			mCursor = db.query(true, table_name, table_headers, key_rowID + "=" + rowId, null, null, null, null, null);
		} catch (Exception e){
			e.printStackTrace();
			Log.d(TAG,"getRecord:exited with exception.");
		} finally {
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
		}
		return mCursor;
	}

	
	/**
	 * 
	 * @param tables		: String[] table, the names of the tables in the database
	 * @return				: String variable, info of the procedure
	 */
	// ----Delete all the elements in the database----
	public synchronized String deleteAllelements(String[] tables, SQLiteDatabase db) {
		String ret_str = "";
		Cursor c = null;
		StringBuffer str = null;
		try {
			str = new StringBuffer();
			String sql;

			if (tables.length > 0) {
				for (int i = 0; i < tables.length; i++) {
					// Count Measures
					sql = String.format("select * from %s;", tables[i]);
					c = db.rawQuery(sql, null);
					str.append(String.valueOf(c.getCount())	+ " rows where deleted from table "	+ tables[i]);
					if(i<tables.length - 1){
						str.append(", ");
					}
					// Delete measures
					sql = String.format("delete from %s;", tables[i]);
					db.execSQL(sql);
				}
			}
			
			ret_str = str.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "deleteAllElements:exited with exception.");
			ret_str = "Data could not be erased.";
		} finally {
			if (!c.isClosed())
				c.close();
			
			c = null;
			str = null;
		}
		return ret_str;
	}
	
	/**
	 * 
	 * @param table_name 	: String variable, the table name we will address the query
	 * @return				: long variable, the id num of the last element of the table 
	 */
	public synchronized long lastEntryId(String table_name){
		long max_id = 0;
		Cursor c = null;
		try{
			String sql = String.format("select MAX(%s) from %s",BaseColumns._ID, table_name);
			c = db.rawQuery(sql, null);
			if(c.moveToFirst()){
				do{
					max_id = c.getLong(c.getColumnIndex("MAX("+BaseColumns._ID+")"));
				}while(c.moveToNext());
			}
		} catch(Exception e){
			Log.e(TAG,"numofentries func: exited with exception.");
			max_id = 0;
		}finally{
			if(c != null)
				c.close();
		}

		return max_id;
	}

	/**
	 * 
	 * @param table_name	: String variable, the table name we will address the query
	 * @return				: int variable, number of entries in the table
	 */
	public synchronized int numofentries(String table_name) {
		int count = 0;
		Cursor c = null;
		try{
			String sql = String.format("select * from %s", table_name);
			c = db.rawQuery(sql, null);
			count = c.getCount();
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"numofentries func: exited with exception.");
		}finally{
			if(c != null)
				c.close();
		}

		return count;
	}

	
	/**
	 * 
	 * @param db			: SQliteDatabse variable, the database object
	 * @param tableName		: String variable, the table name we will address the query
	 * @return				: List<String> variable, contains the columns names
	 */
	public synchronized List<String> GetColumns(SQLiteDatabase db, String tableName) {
		List<String> ar = null;
		Cursor c = null;
		try {
			c = db.rawQuery("select * from " + tableName + " limit 1", null);
			if (c != null) {
				ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
			}
		} catch (Exception e) {
			Log.d(tableName, e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (c != null)
				c.close();
		}
		return ar;
	}

	/**
	 * 
	 * @param list
	 * @param delim
	 * @return
	 */
	public synchronized String join(List<String> list, String delim) {
		StringBuilder buf = new StringBuilder();
		int num = list.size();
		for (int i = 0; i < num; i++) {
			if (i != 0)
				buf.append(delim);
			buf.append((String) list.get(i));
		}
		return buf.toString();
	}
	

	/**
	 * @param db			: SQliteDatabse variable, the database object
	 * @return : String[] table, contains the names of the tables of the database
	 */
	public synchronized String[] DBtableNames(SQLiteDatabase db) {
		String[] tables = null;
		Cursor c = null;
		try{
			c = db.query("sqlite_master", new String[] { "tbl_name" },
					"type='table'", null, null, null, null, null);
			if (c.moveToFirst()) {
				tables = new String[c.getCount() - 2];
				int i = 0;
				do {
					if(!c.getString(c.getColumnIndex("tbl_name")).contains("android_metadata") &&
							!c.getString(c.getColumnIndex("tbl_name")).contains("sqlite_sequence")){
						tables[i] = c.getString(c.getColumnIndex("tbl_name"));
						i++;
					}
				} while (c.moveToNext());
				c.close();
			}
		}catch(Exception e){
			e.printStackTrace();
			Log.d(TAG, "DBTAbleNames:Query could not be performed.");
		} finally {
			if(c != null)
				c.close();
		}
		return tables;

	}
	

	/**
	 * @param db			: SQliteDatabse variable, the database object
	 * @return 				: HashMap, keys-> table name, values -> create command
	 *         
	 */
	public synchronized HashMap<String, String> DBCreateComms(SQLiteDatabase db) {
		HashMap<String, String> comms = new HashMap<String, String>();
		String query = "select tbl_name,sql from sqlite_master where type=?";
		String[] args = new String[]{"table"};
		
		Cursor c = null;
		try {
			c = db.rawQuery(query, args);
			if (c.moveToFirst()) {
				do {
					if(!c.getString(c.getColumnIndex("tbl_name")).contains("android_metadata") &&
							!c.getString(c.getColumnIndex("tbl_name")).contains("sqlite_sequence")){
					comms.put(c.getString(c.getColumnIndex("tbl_name")),
							c.getString(c.getColumnIndex("sql")));
					}
				} while (c.moveToNext());
			}
		} catch (Exception e) {
			Log.d(TAG, "DBCreateComms: exited with exception.");
			e.printStackTrace();
		} finally{
			comms = null;
		}

		return comms;

	}

	// ---Copy the database to an outputstream---
	
	
	/**
	 * 
	 * @param inputStream		: InputStream variable, stream that will be copied
	 * @param outputStream		: OutputStream variable, stream that will be pasted
	 * @throws IOException
	 */
	public synchronized void CopyDB(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		// ---copy 1K bytes at a time---
		byte[] buffer = new byte[1024];
		int length;
		try{
		while ((length = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length);
		}
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"CopyDB: exited with exception.");
		} finally{
			inputStream.close();
			outputStream.close();
		}
	}

	// ---Upgrade the database to the new version--
	

	/**
	 * 
	 * @param db			: SQliteDatabse variable, the database object
	 * @param table			: String variable, the table name we will address the query
	 * @param temp_table	: String variable, the temp table name we will address the query
	 * @param sqlite_create	: String variable, the create sqlite command for the table
	 */
	public void UpgradeDataBase(SQLiteDatabase db, String table,
			String temp_table, String sqlite_create) {
		String substr = sqlite_create.substring(sqlite_create.indexOf('('));
		// Create the sql create command
		String temp_db_create = String.format(
				"create temporary table if not exists %s %s", temp_table,
				substr);
		try{
			// Log.d("upgrade", temp_db_create);
			db.execSQL(temp_db_create);
			db.execSQL(String.format("INSERT INTO %s SELECT * FROM %s;",
					temp_table, table));
			db.execSQL(String.format("DROP TABLE IF EXISTS %s;", table));
			db.execSQL(sqlite_create);
			db.execSQL(String.format("INSERT INTO %s SELECT * FROM %s;", table,
					temp_table));
			db.execSQL(String.format("DROP TABLE IF EXISTS %s;", temp_table));
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG, "Database Upgrade could not be performed.");
		}
	}

}
