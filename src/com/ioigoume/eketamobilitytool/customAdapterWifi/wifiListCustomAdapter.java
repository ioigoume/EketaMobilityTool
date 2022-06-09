package com.ioigoume.eketamobilitytool.customAdapterWifi;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ioigoume.eketamobilitytool.R;
import com.ioigoume.eketamobilitytool.wifiClass.AccessPointClass;

/**
 * THIS CLASS CREATES MY CUSTOM ADAPTER
 * @author menios junior
 * @param <AccessPointClass>
 *
 */

public class wifiListCustomAdapter extends BaseAdapter{

		private Context context;
		private ArrayList<AccessPointClass> accesspoint;
		
		/**
		 * @param context, pass the Activity object
		 * @param accesspoint, pass the arraylist taken from the wificlass
		 */
		public wifiListCustomAdapter(Context context, ArrayList<AccessPointClass> accesspoint){
			this.context = context;
			this.accesspoint = accesspoint;
		}
		
		/**
		 * The default constructor passes null to context and accesspoint arraylist
		 */
		public wifiListCustomAdapter(){
			this(null,null);
		}

		/**
		 * @return the context
		 */
		public Context getContext() {
			return context;
		}

		/**
		 * @param context the context to set
		 */
		public void setContext(Context context) {
			this.context = context;
		}

		/**
		 * @return the accesspoint
		 */
		public ArrayList<AccessPointClass> getAccesspoint() {
			return accesspoint;
		}

		/**
		 * @param accesspoint the accesspoint to set
		 */
		public void setAccesspoint(ArrayList<AccessPointClass> accesspoint) {
			this.accesspoint = accesspoint;
		}

		public int getCount() {
			return accesspoint.size();
		}

		public Object getItem(int position) {
			return accesspoint.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			AccessPointClass access = accesspoint.get(position);
			View v = new WifiListViewRowLayout(this.context, access );
			//v.setBackgroundColor((position % 2) == 1 ? Color.GRAY : Color.BLACK);
			if(access.getLink_speed() != -1){
				v.setBackgroundColor(context.getResources().getColor(R.color.dark_dark_gray));
			}else{
				v.setBackgroundColor(context.getResources().getColor(R.color.black));
			}
			
			return v;
		}
}
