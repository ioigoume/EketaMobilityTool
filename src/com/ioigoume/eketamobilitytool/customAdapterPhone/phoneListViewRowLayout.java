package com.ioigoume.eketamobilitytool.customAdapterPhone;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioigoume.eketamobilitytool.R;
import com.ioigoume.eketamobilitytool.TelephoneClass.NeighbouringCells;

public class phoneListViewRowLayout extends LinearLayout {

	int white_color = getResources().getColor(R.color.white);
	int light_blue = getResources().getColor(R.color.lightBlue);
	int black_color = getResources().getColor(R.color.black);
	
	public phoneListViewRowLayout(Context context) {
		super(context);
	}
	
	public phoneListViewRowLayout(Context context, NeighbouringCells cellInfo) {
		this(context);

		// give the row layout an id
		setId(1);
		// Row layout
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(0, 6, 0, 6);
		
		
		
		// image panel, here there is an image and underneath a text
		LinearLayout.LayoutParams Params_row = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout Panel = new LinearLayout(context);
		Panel.setOrientation(LinearLayout.VERTICAL);
		Panel.setGravity(Gravity.CENTER);
		// Load the panel in the row
		Params_row.setMargins(6, 0, 6, 0);
		Panel.addView(Panel,Params_row);
		
		
		
		
		/**
		 * RSSI PANEL, IMAGE AND SIGNAL STRENGTH
		 */
		LinearLayout.LayoutParams Params_rssi = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		Params_rssi.weight = 0.94f;
		LinearLayout PanelRSSI = new LinearLayout(context);
		PanelRSSI.setOrientation(LinearLayout.VERTICAL);
		PanelRSSI.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		
			// RSSI IMAGE
			ImageView ivLogo = new ImageView(context);
			ivLogo.setPadding(5, 1, 5, 0);
			Drawable myPhoneIcon = (Drawable)icon_Choose_and_Aplly_Phone(cellInfo.getSignalstrength());
			ivLogo.setImageDrawable(myPhoneIcon);
			LinearLayout.LayoutParams image_height_width = new LinearLayout.LayoutParams(65, 60);
			image_height_width.gravity = Gravity.CENTER_HORIZONTAL;
			ivLogo.setLayoutParams(image_height_width);
			//ivLogo.setMaxHeight(20);
			//ivLogo.setMaxWidth(20);
			//image:add
			PanelRSSI.addView(ivLogo);
			
			// RSSI TEXT
			TextView rssi = new TextView( context );
			LinearLayout.LayoutParams rssi_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rssi_params.gravity = Gravity.CENTER_HORIZONTAL;
			rssi.setLayoutParams(rssi_params);
			rssi.setTextSize(14);
			rssi.setPadding(2, 0, 5, 0);
			rssi.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			String rssi_str =  "<font color=#4fa5d5>" + String.valueOf(rssi_dbm_recompute(cellInfo.getSignalstrength())) + "</font>";
			String rssi_dbm =  "<font color=#FFFFFF>dbm</font>";
			rssi.setText( Html.fromHtml(rssi_str + " " + rssi_dbm), TextView.BufferType.SPANNABLE );
			PanelRSSI.addView(rssi);     
		addView(PanelRSSI,Params_rssi);
		

		/**
		 * CELL ID, NETWORK TYPE
		 */
		// vertical layer for frequency and channel num
		LinearLayout.LayoutParams Params_channel = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		Params_channel.weight = 0.45f;
		LinearLayout PanelContent = new LinearLayout(context);
		PanelContent.setOrientation(LinearLayout.VERTICAL);
		PanelContent.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		PanelContent.setPadding(10, 10, 5, 0);
		
			// CELLID
			TextView cellid = new TextView( context );
			cellid.setTextSize(16);
			cellid.setPadding(0, 0, 0, 2);
			cellid.setTextColor(white_color);
			cellid.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			cellid.setText("ssid: " + String.valueOf(cellInfo.getCid()));
			PanelContent.addView(cellid);  
			
			// NETWORK TYPE
			TextView net_type = new TextView( context );
			net_type.setTextSize(14);
			net_type.setPadding(0, 0, 0, 2);
			net_type.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			
			// Frequency
			SpannableString net_str = new SpannableString("Network Type: ");
			net_str.setSpan(new ForegroundColorSpan(white_color), 0, net_str.length(), 0);			
			// Number frequency
			String type_of_net = cellInfo.getNetworkType();
			SpannableString sp_ch_num = new SpannableString(type_of_net);
			sp_ch_num.setSpan(new ForegroundColorSpan(light_blue), 0, sp_ch_num.length(), 0);
			
			net_type.setText(TextUtils.concat(net_str , type_of_net));
			PanelContent.addView(net_type); 
		addView(PanelContent, Params_channel);		
	}
	
	
	
	
	
	/**
	 * 
	 * @param rssi : int, choose the appropriate pin with your eye on the rssi value
	 */
	private Drawable icon_Choose_and_Aplly_Phone(int rssi){
		Drawable icon = null; 
		try{
			if(rssi <= 6){
				icon = this.getResources().getDrawable(R.drawable.signal_00);
			}
			else if(rssi > 6 && rssi <= 12 ){
				icon = this.getResources().getDrawable(R.drawable.signal_11);
			}
			else if(rssi > 12 && rssi <= 18){
				icon = this.getResources().getDrawable(R.drawable.signal_22);
			}
			else if(rssi > 18 && rssi <= 26){
				icon = this.getResources().getDrawable(R.drawable.signal_33);
			}else if(rssi > 26){
				icon = this.getResources().getDrawable(R.drawable.signal_44);
			}
			// Create the overlay object
		}catch(Exception e){
			icon = this.getResources().getDrawable(R.drawable.signal_00);
		}
		return icon;
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
}
