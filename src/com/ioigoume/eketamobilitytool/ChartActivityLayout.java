package com.ioigoume.eketamobilitytool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ioigoume.eketamobilitytool.plot.ChartActivity;

public class ChartActivityLayout extends Activity{

	public static final String TAG = "ChartActivityLayout";
	public static final int MAX_ITEM_SELECTION = 9;
	private String column = null;
	private String table = null;
	private ChartActivity chartObject = null;
	private GraphicalView plot_view = null;
	private ProgressDialog progress = null;
	private ChartThread chthread = null;
	private int state = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /**
         * THE DATA SENDED BY MY STARTUP ACTIVITY
         */
        Intent dataFromMenu = getIntent();
        String[] col_table = dataFromMenu.getExtras().getStringArray("class");
        try{
        	column = col_table[0];
        	table = col_table[1];
        	if(table.equals(TelephoneInterface.DATABASE_TABLE))
        		state = 1;
        }catch(Exception e){
        	Toast.makeText(this, "No data passed from menu.", Toast.LENGTH_SHORT).show();
        	return;
        } 
        
        /**
         * THE CHART OBJECT
         */
	    chartObject = new ChartActivity(column, table);
	    /**
	     * THE GRAPH
	     */
	    Log.d(TAG, String.valueOf(state));
	    createAsyncGraph();
    }
    
    // Help function
    private void createAsyncGraph(){
    	try{
		    progress = ProgressDialog.show(ChartActivityLayout.this, "Wait!", "Chart is loading...", true, false);
		    chthread = new ChartThread("Chart Thread");
		    chthread.start();
	    } catch(Exception e){
	    	if(progress != null && progress.isShowing())
				progress.dismiss();
	    	Toast.makeText(ChartActivityLayout.this, "Chart could not be created.",Toast.LENGTH_LONG).show();
	    }  
    }

    // Create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Get the accesspoint list
    	try{
    		menu.add("Filter Chart");
    	}catch(Exception e){
    		Toast.makeText(ChartActivityLayout.this, "Error creating the menu", Toast.LENGTH_SHORT).show();
    		//getMenuInflater().inflate(R.menu.activity_chart_simple, menu);
    	}
        return true;
    }

	// Sellect an item from the menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getTitle().toString().equalsIgnoreCase("Filter Chart")){
			// In here save the items that are selected from the user
			final ArrayList<String> items_selected = new ArrayList<String>();
			// Take the list of the access points
			final HashMap<String,String> ap_list = chartObject.distinct_String_data_pairs(ChartActivityLayout.this, WifiInterface.KEY_BSSID, WifiInterface.KEY_SSID);
			final HashMap<String,String> reverse_list = new HashMap<String,String>();
    		// Create the char sequence
			final CharSequence[] ap_list_str = new CharSequence[ap_list.size()];
			// Fill the char sequence with the data from the hashmap
			int i=0;
			for(Map.Entry<String, String> entry : ap_list.entrySet()){
				ap_list_str[i] = entry.getValue();
				reverse_list.put(entry.getValue(), entry.getKey());
				i++;
			}
			
			for(Map.Entry<String, String> entry : reverse_list.entrySet()){
				Log.d(TAG, "Key:" + entry.getKey());
				Log.d(TAG, "Value:" + entry.getValue());
			}
       
			if(ap_list_str.length == 0){
				Toast.makeText(ChartActivityLayout.this, "No choices to make.", Toast.LENGTH_SHORT).show();
				return false;
			}	
			
			// Create the alert dialog
	        AlertDialog.Builder builder_filter = new AlertDialog.Builder(this);
	        builder_filter.setMultiChoiceItems(ap_list_str, null, new DialogInterface.OnMultiChoiceClickListener() {
	        	// Keep count of the items i have selected
	        	int max_item_selection = 0;
	        	// What happens if i press the check box
				public void onClick(DialogInterface dialog, int item, boolean isChecked) {
					// Uncheck interface if max selection is reached
					if(max_item_selection > MAX_ITEM_SELECTION){
						Toast.makeText(ChartActivityLayout.this, "Max Item Selection:5", Toast.LENGTH_SHORT).show();
						ListView checkableListView = ((AlertDialog)dialog).getListView();
						checkableListView.setItemChecked(item, false);
					}
					
					String key = ap_list_str[item].toString();
					// Add item to the list
					if(isChecked == true ){
						if(max_item_selection <= MAX_ITEM_SELECTION){
							if(items_selected.add(reverse_list.get(key)))
								max_item_selection++;
						}
					} // Remove item from the list
					if(isChecked == false){ // Remove item from the list if the max limit is reached
						if(items_selected.remove(reverse_list.get(key)))
							max_item_selection--;
					} 
					
					
					Log.d(TAG,"---counter:" + String.valueOf(max_item_selection));
				}
				
			});
	        builder_filter.setCancelable(false);
	        
	        // Filter button press
	        builder_filter.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					if(items_selected.isEmpty()){
						Toast.makeText(ChartActivityLayout.this, "You selected nothing", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if(chartObject == null || plot_view == null){
						Toast.makeText(ChartActivityLayout.this, "Some variables are null", Toast.LENGTH_SHORT).show();
						return;
					}
					
					for(String str : items_selected){
						Log.d(TAG, str);
					}
			        // Set new data
			        chartObject.setAccessPointList_Filtered(items_selected);
			        // Dismiss the alert dialog
					dialog.dismiss();
			        // Start createing the graph
					state = 1;
				    createAsyncGraph();	
				       
					return;
					
				}
			});
	        
	        // Cancel button press
	        builder_filter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					return;
				}
			});
	        
	        View title_view = (View) View.inflate(this, R.layout.alert_dialog_title, null);
	        TextView mTitle = (TextView) title_view.findViewById(R.id.alertTitle);
	        mTitle.setText("Pick Charts");
	        builder_filter.setCustomTitle(title_view);
	        builder_filter.create().show();
		}
		
		return true;
	}

	
	
	/** 
	 * 
	 */
	private class ChartThread extends Thread {
		private boolean isRunning;

		ChartThread(String ThreadName) {
			super(ThreadName);
			isRunning = false;
		}

		@SuppressWarnings("unused")
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
		    
			// Create the graph on the background
			try{
				switch(state){
					case 0:
						chartObject.createEmptyGraph(ChartActivityLayout.this);
						break;
					case 1 :
						chartObject.createGraph(ChartActivityLayout.this);
						break;
				}
		    }catch(Exception e){
		    	chartObject.createEmptyGraph(ChartActivityLayout.this);
		    }
		    runOnUiThread(new Runnable(){
				public void run() {
					plot_view = (GraphicalView)chartObject.getView(ChartActivityLayout.this);
					progress.dismiss();
					setContentView(plot_view);
				}
			});
		    
		    Log.d(TAG,"Thread exiting.");
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
}
