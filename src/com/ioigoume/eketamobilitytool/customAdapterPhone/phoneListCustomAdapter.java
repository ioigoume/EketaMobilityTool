package com.ioigoume.eketamobilitytool.customAdapterPhone;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ioigoume.eketamobilitytool.R;
import com.ioigoume.eketamobilitytool.TelephoneClass.NeighbouringCells;

/**
 * THIS CLASS CREATES MY CUSTOM ADAPTER
 * @author menios junior
 * @param <AccessPointClass>
 *
 */

public class phoneListCustomAdapter extends BaseAdapter{

		private Context context;
		private ArrayList<NeighbouringCells> ncell;
		
		/**
		 * @param context, pass the Activity object
		 * @param accesspoint, pass the arraylist taken from the wificlass
		 */
		public phoneListCustomAdapter(Context context, ArrayList<NeighbouringCells> ncell){
			this.context = context;
			this.ncell = ncell;
		}
		
		/**
		 * The default constructor passes null to context and accesspoint arraylist
		 */
		public phoneListCustomAdapter(){
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
		 * @return the NeighbourCell List
		 */
		public ArrayList<NeighbouringCells> getNeighbourCellList() {
			return ncell;
		}

		/**
		 * @param accesspoint the accesspoint to set
		 */
		public void setNeightbourCellList(ArrayList<NeighbouringCells> cells) {
			this.ncell = cells;
		}

		public int getCount() {
			return ncell.size();
		}

		public Object getItem(int position) {
			return ncell.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			NeighbouringCells cell_pos = ncell.get(position);
			View v = new phoneListViewRowLayout(this.context, cell_pos );
			//v.setBackgroundColor((position % 2) == 1 ? Color.GRAY : Color.BLACK);
			v.setBackgroundColor(context.getResources().getColor(R.color.black));
			
			return v;
		}
}
