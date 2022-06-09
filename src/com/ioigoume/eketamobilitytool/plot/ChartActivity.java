package com.ioigoume.eketamobilitytool.plot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;

import com.ioigoume.eketamobilitytool.TelephoneInterface;
import com.ioigoume.eketamobilitytool.WifiInterface;
import com.ioigoume.eketamobilitytool.database.DBAdapter;

/**
 * Temperature sensor demo chart.
 * 
 */
public class ChartActivity extends AbstractDemoChart {
	public static final String TAG = "ChartActivity";
	
	// COLUMN/ VARIABLE I WANT TO PLOT
	private String column = null;
	// TABLE THAT CONTAINS THE COLUMN
	private String table = null;
	
	// ACCESS POINT LIST ARRAY LIST
	private ArrayList<String> accessPointList_Filtered = null;
	// THE X AXIS DATA
	private List<Date[]> dateList;
	// THE DATA THAT WILL BE DRAWN, Y AXIS DATA
	private List<double[]> dataList;
	// Y AXIS VALUES RANGE
	private Integer[] yaxis ;
	// TITLE OF THE GRAPH
	private String[] titles;
	// COLORS OF LINES
	int[] colors;
	// POINTS STYLE
	PointStyle[] styles;
	// X MIN, X MAX
	private long xmin;
	private long xmax;
	
	
	XYMultipleSeriesRenderer renderer;
	XYMultipleSeriesDataset dataset;
	

	// PASS AS PARAMETER THE COLUMN WE WANT TO PLOT AND THE TABLE
	// THAT CONTAINS THAT COLUMN
	public ChartActivity(String column, String table) {
		this.column = column;
		this.table = table;
	}
	
	public void createGraph(Context ctx){
		// x data
		dateList = DateDataReturn(ctx , table, accessPointList_Filtered);
		Log.d(TAG,"dateList len:" + String.valueOf(dateList.size()));
		// y data
		dataList = yDataReturn(ctx, column, table, accessPointList_Filtered);
		Log.d(TAG, "dataList len:" + String.valueOf(dataList.size()));
		// y axis title
		yaxis = yAxisReturn(column);
		// lines titles
		titles = titleReturn(ctx, column, accessPointList_Filtered);
		Log.d(TAG,"titles length:" + String.valueOf(titles.length));
		// line colors
		colors = retColor(ctx, table, accessPointList_Filtered);
		Log.d(TAG,"colors length:" + String.valueOf(colors.length));
		// line point styles
		styles = retPointStyle(ctx, table, accessPointList_Filtered);
		Log.d(TAG,"style length:" + String.valueOf(styles.length));
		// x axis min value
		xmin = dateList.get(0)[0].getTime();
		Log.d(TAG, "1");
		// x axis max value
		xmax = dateList.get(0)[dateList.get(0).length - 1].getTime();
		Log.d(TAG, "2");
		// Create the renderer
		renderer = buildRenderer(colors, styles);
		// Create the dataset
		dataset = buildDateDataset(titles, dateList, dataList);
		Log.d(TAG, "3");
		for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
		}
		
		setChartSettings(renderer, // POINTS
				"Interface Monitoring", // PLOT LABEL
				"Time", // X LABEL
				yAxisTitleReturn(column), // Y LABEL
				xmin, 
				xmax, 
				yaxis[0], // Y AXIS START VALUE
				yaxis[1], // Y AXIS FINISH VALUE
				Color.LTGRAY, // AXIS COLOR
				Color.LTGRAY); // LABELS COLOR
		
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setApplyBackgroundColor(true);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setShowGrid(true);
		renderer.setZoomEnabled(true, true);
		renderer.setPanEnabled(true, true);
	}

	
	/**
	 * 
	 * @param context
	 * @return GraphicalView, returns the view
	 */
	public GraphicalView getView(Context context) {
		return (GraphicalView)ChartFactory.getTimeChartView(context, dataset, renderer,	"h:mm:ss a");	
	}
	
	/**
	 * 
	 * @param context
	 * @return GraphicalView, empty plot View
	 */
	public void createEmptyGraph(Context context){
		dateList = new ArrayList<Date[]>();
		dataList = new ArrayList<double[]>();
		
		dateList.add(new Date[] { new Date(0) });
		dataList.add(new double[] { 0.0 });
		xmin = 0;
		xmax = 10;
		colors = new int[]{Color.BLACK};
		styles = new PointStyle[]{PointStyle.CIRCLE};
		yaxis = yAxisReturn(column);
		titles = new String[]{"empty"};
		renderer = buildRenderer(colors, styles);
		dataset = buildDateDataset(titles, dateList, dataList);
		for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
		}
		setChartSettings(renderer, // POINTS
				"Interface Monitoring", // PLOT LABEL
				"Time", // X LABEL
				"Samples", // Y LABEL
				xmin,
				xmax, 
				yaxis[0], // Y AXIS START VALUE
				yaxis[1], // Y AXIS FINISH VALUE
				Color.LTGRAY, // AXIS COLOR
				Color.LTGRAY); // LABELS COLOR

		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setApplyBackgroundColor(true);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setShowGrid(true);
		renderer.setZoomEnabled(true, true);
		renderer.setPanEnabled(true, true);
	}

	/**
	 * 
	 * @param ctx
	 *            : Context
	 * @return List of double array, each array contains the values of of
	 *         drawing. Data is being retrieved from the local database.
	 */
	private synchronized List<double[]> dataRetrieve(Context ctx, String column, String table) {
		List<double[]> datalist = new ArrayList<double[]>();
		double[] dataArray = null;
		Cursor c = null;

		String[] clm = new String[1];
		DBAdapter db = new DBAdapter(ctx);

		clm[0] = column;
		try{
			db.open();
			c = db.myQuery(table, clm, null, null);
			int i = 0;
			dataArray = new double[c.getCount()];
			if (c.moveToFirst()) {
				do {
					try {
						dataArray[i] = Double.valueOf(c.getString(c.getColumnIndex(clm[0])));
						// Remove the wifi link speed/channel rate default value
						if(column.equals(WifiInterface.KEY_LINK_SPEED) && dataArray[i] == -1){
							dataArray[i] = MathHelper.NULL_VALUE;
						}						
					} catch (NumberFormatException e) {
						//dataArray[i] = 0;
						dataArray[i] = MathHelper.NULL_VALUE;
					}
					i++;
				} while (c.moveToNext());
			}
		} catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"dataRetrieve:exited with exception.");
		} finally{
			if (dataArray != null)
				datalist.add(dataArray);
			else
				datalist = null;

			if(c!= null)
				c.close();
			
			db.close();
		}
		
		

		return datalist;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param column_ap
	 * @param column_rssi
	 * @param table
	 * @return list of double arrays containing the rssi measures for each access point
	 */
	@SuppressWarnings("unused")
	private synchronized List<double[]> dataMultipleRetrieve(Context ctx, String column_grouping, String column_dependent, String table, ArrayList<String> accesspointList) {
		// The list with my data, each array entrance is for each AccessPoint
		List<double[]> datalist = new ArrayList<double[]>();
		double[] dataArray = null;
		Cursor c = null;

		// Create a ref to my database
		DBAdapter db = new DBAdapter(ctx);
		// Open the database
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
		
		///////////////////////////////////////////////////////////
		// Retrieve for each ap the signal values and create a list
		///////////////////////////////////////////////////////////
		if(db!= null && accesspointList != null && accesspointList.size() > 0){
			// For each string in the list retrieve the data from the db
			// then store the data to an array.
			// Then put this array in an Array List
			for(String ap : accesspointList){
				String where = String.format("%s = '%s'", column_grouping, ap);
				c = db.myQuery(table, WifiInterface.WIFI_ELEMENTS, where, null);
				if (c != null) {
					//Log.d(TAG, "c : " + String.valueOf(c.getCount()));
					int i = 0;
					dataArray = new double[c.getCount()];
					if (c.moveToFirst()) {
						do {
							try {
								dataArray[i] = Double.valueOf(c.getString(c.getColumnIndex(column_dependent)));
								
								// Remove the wifi link speed/channel rate default value
								if(column.equals(WifiInterface.KEY_LINK_SPEED) && dataArray[i] == -1){
									dataArray[i] = MathHelper.NULL_VALUE;
								}
								
							} catch (NumberFormatException e) {
								//dataArray[i] = 0;
								dataArray[i] = MathHelper.NULL_VALUE;
							}
							i++;
						} while (c.moveToNext());
					}
				}

				if (dataArray != null){
					datalist.add(dataArray);
					dataArray = null;
				}
				
				// Reset my cursor, the cursor is null catch the exception
				try{
					c.close();
					c = null;
				}catch(Exception e){
					c = null;
				}
			}// for
		}// if
		
		
		// Close the database
		db.close();
		// The list with rssi values
		return datalist;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param column_ap
	 * @param column_rssi
	 * @param table
	 * @return list of double arrays containing the rssi measures for each access point
	 */
	private synchronized List<double[]> dataMultipleWhereRetrieve(Context ctx, 
			String column_grouping, 
			String column_dependent, 
			String table, 
			String[] date,
			ArrayList<String> accesspointList) {
		// The list with my data, each array entrance is for each AccessPoint
		List<double[]> datalist = new ArrayList<double[]>();
		ArrayList<Double> dataArray = new ArrayList<Double>();
		
		Cursor c = null;

		// Create a ref to my database
		DBAdapter db = new DBAdapter(ctx);
		// Open the database
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
		
		///////////////////////////////////////////////////////////
		// Retrieve for each ap the signal values and create a list
		///////////////////////////////////////////////////////////
		if(db != null && accesspointList != null && accesspointList.size() > 0 && date != null && date.length > 0){
			// For each string in the list retrieve the data from the db
			// then store the data to an array.
			// Then put this array in an Array List
			for(String ap : accesspointList){
				// Create the array to store my data
				for(String dt : date){
					// Construct the where clause
					String where = String.format("%s='%s' and %s='%s'", 
							column_grouping, 
							ap,
							DBAdapter.KEY_DATETIME,
							dt);
					c = db.myQuery(table, WifiInterface.WIFI_ELEMENTS, where, null);
					if (c != null) {
						if (c.moveToFirst()) {
							do {
								try {
									double d = Double.valueOf(c.getString(c.getColumnIndex(column_dependent)));
									
									// Remove the wifi link speed/channel rate default value
									if(column.equals(WifiInterface.KEY_LINK_SPEED) && d == -1){
										d = MathHelper.NULL_VALUE;
									}
									
									dataArray.add(d);
								} catch (NumberFormatException e) {
									dataArray.add(new Double(MathHelper.NULL_VALUE));
								}
							} while (c.moveToNext());
						}else{
							// if there is no measurement for the this time pick put a null value
							dataArray.add(new Double(MathHelper.NULL_VALUE));							
						}
					}
					
					// Reset my cursor, the cursor is null catch the exception
					try{
						c.close();
						c = null;
					}catch(Exception e){
						c = null;
					}
				}// for
				
				double[] double_data = new double[dataArray.size()];
				int i=0;
				if (dataArray != null){
					for(Double dbl : dataArray){
						double_data[i] = dbl;
						i++;
					}
					datalist.add(double_data);
					dataArray.clear();
				}// if
			}// for
			
		}// if
		
		
		// Close the database
		db.close();
		dataArray = null;
		// The list with rssi values
		return datalist;
	}
	
	
	

	/**
	 * 
	 * @param ctx
	 *            : Context
	 * @return List of Date arrays for each drawing. Data is being retrieved
	 *         from the local database
	 */
	private synchronized List<Date[]> timeRetrieve(Context ctx, String table) {
		List<Date[]> datalist = new ArrayList<Date[]>();
		Date[] timedata = null;
		SimpleDateFormat df_am_pm = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa", Locale.ENGLISH);
		long milisecs = 0;
		long old_milisecs = 0;

		String[] clm = new String[2];
		clm[0] = DBAdapter.KEY_DATETIME;

		// Obtain a pointer to the database
		DBAdapter db = new DBAdapter(ctx);
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
		
		Cursor c = db.myQuery(table, clm, null, null);
		if (db != null && c != null) {
			int i = 0;
			timedata = new Date[c.getCount()];
			if (c.moveToFirst()) {
				do {
					try {
						String date = c.getString(c.getColumnIndex(clm[0])).replaceAll("\\p{Cntrl}", "");
						milisecs = df_am_pm.parse(date).getTime();
						timedata[i] = new Date(milisecs - old_milisecs);
						i++;
					} catch (ParseException e) {
						e.printStackTrace();
						return null;
					}
				} while (c.moveToNext());
			}
		}

		if (timedata != null){
			datalist.add(timedata);
			timedata = null;
		}
		else
			datalist = null;

		// Reset my cursor, the cursor is null catch the exception
		try{
			c.close();
			c = null;
		}catch(Exception e){
			c = null;
		}
		
		db.close();

		return datalist;
	}

	
	/**
	 * 
	 * @param ctx
	 *            : Context
	 * @return List of Date arrays for each drawing. Data is being retrieved
	 *         from the local database
	 */
	@SuppressWarnings("unused")
	private synchronized List<Date[]> timeMultipleRetrieve(Context ctx, String table, ArrayList<String> grouping_factor, String grouping_col) {
		List<Date[]> datalist = new ArrayList<Date[]>();
		Date[] timedata = null;
		SimpleDateFormat df_am_pm = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa", Locale.ENGLISH);
		long milisecs = 0;
		long old_milisecs = 0;

		String[] clm = new String[2];
		clm[0] = DBAdapter.KEY_DATETIME;

		// Create and open database
		DBAdapter db = new DBAdapter(ctx);
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
		
		
		// Execute queries
		if( db!= null && grouping_factor != null){
			for(String str : grouping_factor){
				String where = String.format("%s = '%s'", grouping_col, str);
				Cursor c = db.myQuery(table, clm, where, null);
				if (c != null) {
					int i = 0;
					timedata = new Date[c.getCount()];
					if (c.moveToFirst()) {
						do {
							try {
								String date = c.getString(c.getColumnIndex(clm[0])).replaceAll("\\p{Cntrl}", "");
								milisecs = df_am_pm.parse(date).getTime();
								timedata[i] = new Date(milisecs - old_milisecs);
								i++;
							} catch (ParseException e) {
								e.printStackTrace();
								return null;
							}
						} while (c.moveToNext());
					}// if
				}// if

				
				if (timedata != null){
					datalist.add(timedata);
					timedata = null;
				}
				// Reset my cursor, the cursor is null catch the exception
				try{
					c.close();
					c = null;
				}catch(Exception e){
					c = null;
				}
			}// for
		}
		
		

		db.close();

		return datalist;
	}
	
	/**
	 * 
	 * @param ctx
	 * @param column_grouping : column that i want a distinct list to be obtained
	 * @return a list of the distinct entries of the column that you give
	 */
	public synchronized ArrayList<String> distinct_String_data(Context ctx, String column_grouping){
		// Accesspoint List
		ArrayList<String> dataList = new ArrayList<String>();
		Cursor c = null;
		// Create a ref to my database
		DBAdapter db = new DBAdapter(ctx);
		// Open the database
		db.open();
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
				
		///////////////////////////////////////////////////////////////
		// Retrieve the a cursor containing the different access points
		///////////////////////////////////////////////////////////////		
		c = db.myDistinctQuery(table, column_grouping, db.getDb());
		if(db != null && c != null){
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
	 * @param ctx
	 * @param column_grouping_first, the column that will query the database
	 * @param column_grouping_second, the column that will couple the first one
	 * @return HashMap<String, String> = <first column, second column>
	 */
	public synchronized HashMap<String,String> distinct_String_data_pairs(Context ctx, String column_grouping_first, String column_grouping_second){
		// Hashmap
		HashMap<String,String> hash = new HashMap<String,String>();
		Cursor c = null;
		// Create a ref to my database
		DBAdapter db = new DBAdapter(ctx);
		// Open the database
		db.open();
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
				
		///////////////////////////////////////////////////////////////
		// Retrieve the a cursor containing the different access points
		///////////////////////////////////////////////////////////////	
		c = db.myQuery(table, new String[]{column_grouping_first, column_grouping_second}, null, WifiInterface.KEY_BSSID);
		//c = db.myDistinctQuery(table, column_grouping_first, db.getDb());
		if(db != null && c != null){
			if(c.moveToFirst()){
				do{
					String value_1 = String.valueOf(c.getString(c.getColumnIndex(column_grouping_first)));
					String value_2 = String.valueOf(c.getString(c.getColumnIndex(column_grouping_second)));
					
					hash.put(value_1, value_2);
				}while(c.moveToNext());
				// Pass the data to main function
				Log.d(TAG, "num of ap:" + String.valueOf(hash.size()));
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
		return hash;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param column_grouping : column that i want a distinct list to be obtained
	 * @return a list of the distinct entries of the column that you give
	 * @throws ParseException 
	 */
	public synchronized Date[] distinct_Date_data_processed(Context ctx, String table){
		SimpleDateFormat df_am_pm = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa", Locale.ENGLISH);
		long milisecs = 0;
		long old_milisecs = 0;
		Date[] timedata = null;
		
		
		// The cursor that returns the values
		Cursor c = null;
		// Create a ref to my database
		DBAdapter db = new DBAdapter(ctx);
		// Open the database
		db.open();
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
				
		///////////////////////////////////////////////////////////////
		// Retrieve the a cursor containing the different access points
		///////////////////////////////////////////////////////////////		
		
		c = db.myDistinctQuery(table, DBAdapter.KEY_DATETIME, db.getDb());
		if(db != null && c != null){
			timedata = new Date[c.getCount()];
			
			try{
				if(c.moveToFirst()){
					int i=0;
					do{
						String date = c.getString(c.getColumnIndex(DBAdapter.KEY_DATETIME)).replaceAll("\\p{Cntrl}", "");
						milisecs = df_am_pm.parse(date).getTime();
						timedata[i] = new Date(milisecs - old_milisecs);
						i++;
					}while(c.moveToNext());
				}
			}catch(Exception e){
				timedata = null;
				timedata = new Date[c.getCount()];
				for(int j=0;j<timedata.length;j++){
					timedata[j] = new Date(j*1000);
				}				
			}
		}
		//for(Date dt : timedata){
		//	Log.d(TAG, dt.toGMTString());
		//}
		
		//for(String dt : raw_date_entries){
		//	Log.d(TAG, dt.toString());
		//}
					
		// Reset my cursor, the cursor is null catch the exception
		try{
			c.close();
			c = null;
		}catch(Exception e){
			c = null;
		}
		
		
		db.close();
		return timedata;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param column_grouping : column that i want a distinct list to be obtained
	 * @return a list of the distinct entries of the column that you give
	 * @throws ParseException 
	 */
	public synchronized String[] distinct_Date_data_raw(Context ctx, String table){
		String[] timedata = null;
		
		
		// The cursor that returns the values
		Cursor c = null;
		// Create a ref to my database
		DBAdapter db = new DBAdapter(ctx);
		// Open the database
		db.open();
		try{
			db.open();
		}
		catch(SQLException e)
		{
			db = null;
			e.printStackTrace();
			Log.d(TAG,"Database could not be opened.Retry.");
		}
				
		///////////////////////////////////////////////////////////////
		// Retrieve the a cursor containing the different access points
		///////////////////////////////////////////////////////////////		
		
		c = db.myDistinctQuery(table, DBAdapter.KEY_DATETIME, db.getDb());
		if(db != null && c != null){
			timedata = new String[c.getCount()];
			
			try{
				if(c.moveToFirst()){
					int i=0;
					do{
						String date = c.getString(c.getColumnIndex(DBAdapter.KEY_DATETIME));
						timedata[i] = date;
						i++;
					}while(c.moveToNext());
				}
			}catch(Exception e){
				timedata = null;
				timedata = new String[c.getCount()];
				for(int j=0;j<timedata.length;j++){
					timedata[j] = String.valueOf(j + 1);
				}				
			}
		}
		//for(Date dt : timedata){
		//	Log.d(TAG, dt.toGMTString());
		//}
		
		//for(String dt : raw_date_entries){
		//	Log.d(TAG, dt.toString());
		//}
					
		// Reset my cursor, the cursor is null catch the exception
		try{
			c.close();
			c = null;
		}catch(Exception e){
			c = null;
		}		
		
		db.close();
		return timedata;
	}
	
	
	/**
	 * @param column, the name of the column of the sqlite table
	 * @return title of the line of the graph
	 */
	private synchronized String[] titleReturn(Context ctx, String column, ArrayList<String> ap_list_filtered) {
		String[] retcolumn = null;
		ArrayList<String> ap_list = null;
		
		if( table.equalsIgnoreCase(TelephoneInterface.DATABASE_TABLE)){
			if(column.equalsIgnoreCase(TelephoneInterface.KEY_GSMSIGNALSTRENGTH))
				retcolumn = new String[]{"RSSI(dbm) - phone"};
		}else if( table.equalsIgnoreCase(WifiInterface.DATABASE_TABLE_WIFI)){
			// Get the paired hashmap
			HashMap<String, String> pairs = distinct_String_data_pairs(ctx, WifiInterface.KEY_BSSID, WifiInterface.KEY_SSID);
			try{
				if(ap_list_filtered != null && !ap_list_filtered.isEmpty()){
					for(String str : ap_list_filtered){
						if(pairs.containsKey(str))
							ap_list_filtered.set(ap_list_filtered.indexOf(str), pairs.get(str));
					}
					ap_list = ap_list_filtered;
				}else{
					ap_list = distinct_String_data(ctx, WifiInterface.KEY_BSSID);
					for(String str : ap_list){
						if(pairs.containsKey(str))
							ap_list.set(ap_list.indexOf(str), pairs.get(str));
					}
				}
				retcolumn = (String[]) ap_list.toArray(new String[ap_list.size()]);
			}catch(Exception e){
				retcolumn = new String[]{"no title"};
			}
		}else{
			retcolumn = new String[]{"no title"};
		}
			
		return retcolumn;
	}
	
	/**
	 * @param column, the name of the column of the sqlite table
	 * @return integer array where : [0] - ymin and [1] : ymax
	 */
	private synchronized Integer[] yAxisReturn(String column){
		Integer[] rety = new Integer[2];
		rety[0] = 0;
		rety[1] = 50;
		
		/**
		 * RSSI(PHONE INTERFACE) : -113dbm : -53dbm
		 * SNR	: 0 - 8 valid values
		 * RSSI(WIFI INTERFACE) : -95dbm : 0 dbm
		 */
		if( column.equalsIgnoreCase(TelephoneInterface.KEY_GSMSIGNALSTRENGTH)){
			rety[0] = -120;
			rety[1] = -50;
		}
		if( column.equalsIgnoreCase(WifiInterface.KEY_LINK_SPEED)){
			rety[0] = -5;
			rety[1] = 70;
		}
		if( column.equalsIgnoreCase(WifiInterface.KEY_WIFISIGNALSTRENGTH)){
			rety[0] = -110;
			rety[1] = 10;
		}
		
		return rety;
	} 
	
	/**
	 * @param column, the name of the column of the sqlite table
	 * @return String, the yaxis legend
	 */
	private synchronized String yAxisTitleReturn(String column){
	
		if( column.equalsIgnoreCase(TelephoneInterface.KEY_GSMSIGNALSTRENGTH)){
			return "RSSI(dbm)-Phone";
		}
		else if( column.equalsIgnoreCase(WifiInterface.KEY_LINK_SPEED)){
			return "Link Speed(Mbps)-Wifi";
		}
		else if( column.equalsIgnoreCase(WifiInterface.KEY_WIFISIGNALSTRENGTH)){
			return "RSSI(dbm)-Wifi";
		}else
			return "Samples";
		
	} 
	
	/**
	 * 
	 * @param context : context passed with intent
	 * @param column : name of the column from which we want the data
	 * @param table : name of the table
	 * @return List with all the data
	 */
	private synchronized List<double[]> yDataReturn(Context context, String column, String table, ArrayList<String> ap_list_filtered){
		List<double[]> retList = null;
		ArrayList<String> ap_list = null;
		
		if( table.equalsIgnoreCase(TelephoneInterface.DATABASE_TABLE)){
			retList = dataRetrieve(context, column, table);
		}
		//if( column.equalsIgnoreCase(PhoneInterfaceClass.KEY_GSMBER)){
		//	retList = dataRetrieve(context, column, table);
		//}
		if( table.equalsIgnoreCase(WifiInterface.DATABASE_TABLE_WIFI)){
			if(ap_list_filtered != null && ap_list_filtered.size() > 0 ){
				ap_list = ap_list_filtered;
			}
			else{
				ap_list = distinct_String_data(context, WifiInterface.KEY_BSSID);
			}
			String[] date = distinct_Date_data_raw(context, table);
			retList = dataMultipleWhereRetrieve(context, 
					WifiInterface.KEY_BSSID, 
					column, 
					table,
					date,
					ap_list);
		}
		
		return retList;
	}
	
	
	/**
	 * 
	 * @param context
	 * @param table, the name of the table
	 * @return list of date arrays with data for each measurement
	 */
	private synchronized List<Date[]> DateDataReturn(Context context, String table, ArrayList<String> ap_list_filtered){
		List<Date[]> retList = null;
		Date[] date_ar = null;
		ArrayList<String> ap_list = null;
		
		if( table.equalsIgnoreCase(TelephoneInterface.DATABASE_TABLE)){
			retList = timeRetrieve(context, table);
		}
		
		// If an exception occurs i will get the counter as a return
		if( table.equalsIgnoreCase(WifiInterface.DATABASE_TABLE_WIFI)){
			if(ap_list_filtered != null && ap_list_filtered.size() > 0){
				ap_list = ap_list_filtered;
			}else{
				ap_list = distinct_String_data(context, WifiInterface.KEY_BSSID);				
			}
			date_ar = distinct_Date_data_processed(context, table);
			retList = new ArrayList<Date[]>();			
			for(int i=0; i<ap_list.size(); i++){
				retList.add(date_ar);
			}
		}
		
		return retList;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param table: name of the table
	 * @return array of pointstyles
	 */
	private synchronized PointStyle[] retPointStyle(Context ctx, String table, ArrayList<String> ap_list_filtered){
		PointStyle[] retPoint = null;
		ArrayList<String> ap_list = null;
		
		if( table.equalsIgnoreCase(TelephoneInterface.DATABASE_TABLE)){
			retPoint = new PointStyle[]{PointStyle.CIRCLE};
		}
		if( table.equalsIgnoreCase(WifiInterface.DATABASE_TABLE_WIFI)){
			if(ap_list_filtered != null && ap_list_filtered.size() > 0){
				retPoint = new PointStyle[ap_list_filtered.size()];
			}else{
				ap_list = distinct_String_data(ctx, WifiInterface.KEY_BSSID);
				retPoint = new PointStyle[ap_list.size()];
			}
			for(int i=0; i<retPoint.length; i++){
				retPoint[i] = PointStyle.CIRCLE;
			}
		}
		
		return retPoint;
	}
	
	
	/**
	 * 
	 * @param ctx
	 * @param table
	 * @return
	 */
	private synchronized int[] retColor(Context ctx, String table, ArrayList<String> ap_list_filtered){
		int[] retCol = null;
		ArrayList<String> ap_list = null;
		
		if( table.equalsIgnoreCase(TelephoneInterface.DATABASE_TABLE)){
			retCol = new int[]{Color.BLUE};
		}
		if( table.equalsIgnoreCase(WifiInterface.DATABASE_TABLE_WIFI)){
			if(ap_list_filtered != null && ap_list_filtered.size() > 0){
				retCol = new int[ap_list_filtered.size()];
			}else{
				ap_list = distinct_String_data(ctx, WifiInterface.KEY_BSSID);
				retCol = new int[ap_list.size()];
			}
			Random randomRGB = new Random();
			for(int i=0; i<retCol.length; i++){
				retCol[i] = Color.argb( 255,  randomRGB.nextInt(255),   randomRGB.nextInt(255),   randomRGB.nextInt(255));
			}
		}
		
		return retCol;
	}

	/**
	 * @return the column
	 */
	public synchronized String getColumn() {
		return column;
	}

	/**
	 * @param column the column to set
	 */
	public synchronized void setColumn(String column) {
		this.column = column;
	}

	/**
	 * @return the table
	 */
	public synchronized String getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	public synchronized void setTable(String table) {
		this.table = table;
	}

	/**
	 * @return the accessPointList_Filtered
	 */
	public synchronized ArrayList<String> getAccessPointList_Filtered() {
		return accessPointList_Filtered;
	}

	/**
	 * @param accessPointList_Filtered the accessPointList_Filtered to set
	 */
	public synchronized void setAccessPointList_Filtered(ArrayList<String> accessPointList_Filtered) {
		this.accessPointList_Filtered = accessPointList_Filtered;
	}
}
