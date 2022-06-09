package com.ioigoume.eketamobilitytool.customAdapterWifi;

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
import com.ioigoume.eketamobilitytool.wifiClass.AccessPointClass;

public class WifiListViewRowLayout extends LinearLayout {

	int white_color = getResources().getColor(R.color.white);
	int light_blue = getResources().getColor(R.color.lightBlue);
	int black_color = getResources().getColor(R.color.black);
	int red_color = getResources().getColor(R.color.red);
	
	public WifiListViewRowLayout(Context context) {
		super(context);
	}
	
	public WifiListViewRowLayout(Context context, AccessPointClass ap ) {
		this(context);

		// give the row layout an id
		setId(1);
		// Row layout
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(0, 6, 0, 6);
		
		
		
		// image panel, here there is an image and underneath a text
		//LinearLayout.LayoutParams Params_row = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		///LinearLayout Panel = new LinearLayout(context);
		//Panel.setOrientation(LinearLayout.VERTICAL);
		//Panel.setGravity(Gravity.CENTER);
		// Load the panel in the row
		//Params_row.setMargins(6, 0, 6, 0);
		//Panel.addView(Panel,Params_row);
		
		
		
		
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
			ivLogo.setPadding(5, 10, 5, 0);
			Drawable myWifiIcon = (Drawable)icon_Choose_and_Aplly_wifi(ap.getSignalstrength());
			ivLogo.setImageDrawable(myWifiIcon);
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
			String rssi_str =  "<font color=#4fa5d5>" + String.valueOf(ap.getSignalstrength()) + "</font>";
			String rssi_dbm =  "<font color=#FFFFFF>dbm</font>";
			rssi.setText( Html.fromHtml(rssi_str + " " + rssi_dbm), TextView.BufferType.SPANNABLE );
			PanelRSSI.addView(rssi);     
		addView(PanelRSSI,Params_rssi);
		

		/**
		 * CHANNEL FREQ, CHANNEL NUM, BSSID, SSID, 
		 */
		// vertical layer for frequency and channel num
		LinearLayout.LayoutParams Params_channel = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		Params_channel.weight = 0.45f;
		LinearLayout PanelContent = new LinearLayout(context);
		PanelContent.setOrientation(LinearLayout.VERTICAL);
		PanelContent.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		PanelContent.setPadding(10, 5, 5, 0);
		
			// SSID
			TextView ssid = new TextView( context );
			ssid.setTextSize(14);
			ssid.setPadding(0, 0, 0, 2);
			ssid.setTextColor(white_color);
			ssid.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			ssid.setText("ssid: " + ap.getSsid());
			PanelContent.addView(ssid);  
			
			// BSSID
			TextView bsid = new TextView( context );
			bsid.setTextSize(14);
			bsid.setPadding(0, 0, 0, 2);
			bsid.setTextColor(white_color);
			bsid.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			bsid.setText("bssid: " + ap.getBssid());
			PanelContent.addView(bsid);
			
			// CHANNEL FREQUENCY:CHANNEL NUM:ACCESSPOINTS NUM
			TextView channel_fr = new TextView( context );
			channel_fr.setTextSize(14);
			channel_fr.setPadding(0, 0, 0, 2);
			channel_fr.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			
			// Frequency
			String freq = String.valueOf(ap.getChannel_frequency());
			SpannableString sp_ch_fr = new SpannableString("ch: " + freq + "MHz - " );
			sp_ch_fr.setSpan(new ForegroundColorSpan(white_color), 0, sp_ch_fr.length(), 0);			
			// Number frequency
			String num = String.valueOf(ap.getChannel_num());
			SpannableString sp_ch_num = new SpannableString("#" + num);
			sp_ch_num.setSpan(new ForegroundColorSpan(light_blue), 0, sp_ch_num.length(), 0);
			// Text Number of access points
			SpannableString text_num_aps = new SpannableString(",#APs: " );
			text_num_aps.setSpan(new ForegroundColorSpan(white_color), 0, text_num_aps.length(), 0);
			// Number of access points
			String num_aps = String.valueOf(ap.getNum_accesspoints());
			SpannableString sp_aps_num = new SpannableString("#" + num_aps);
			sp_aps_num.setSpan(new ForegroundColorSpan(light_blue), 0, sp_aps_num.length(), 0);
									
			channel_fr.setText(TextUtils.concat(sp_ch_fr , sp_ch_num , text_num_aps, sp_aps_num));
			PanelContent.addView(channel_fr);
			
			// LINK SPEED
			TextView link_speed = new TextView( context );
			link_speed.setTextSize(14);
			link_speed.setPadding(0, 0, 0, 2);
			link_speed.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			
			// Link text
			SpannableString sp_link_txt = new SpannableString("link sp: ");
			sp_link_txt.setSpan(new ForegroundColorSpan(white_color), 0, sp_link_txt.length(), 0);
						
			String link = "";
			SpannableString sp_link_num = null;
			SpannableString sp_link_mbps = null;
			if(ap.getLink_speed() == -1){
				link = "Not Available ";
				// Link measure
				sp_link_num = new SpannableString(link + " ");
				sp_link_num.setSpan(new ForegroundColorSpan(red_color), 0, sp_link_num.length(), 0);
				// Link measure txt
				sp_link_mbps = new SpannableString("");
				sp_link_mbps.setSpan(new ForegroundColorSpan(white_color), 0, sp_link_mbps.length(), 0);
			}else{
				link = String.valueOf(ap.getLink_speed());
				// Link measure
				sp_link_num = new SpannableString(link + " ");
				sp_link_num.setSpan(new ForegroundColorSpan(light_blue), 0, sp_link_num.length(), 0);
				// Link measure txt
				sp_link_mbps = new SpannableString("Mbps");
				sp_link_mbps.setSpan(new ForegroundColorSpan(white_color), 0, sp_link_mbps.length(), 0);
			}
			
			
			
			link_speed.setText(TextUtils.concat(sp_link_txt, sp_link_num, sp_link_mbps));
			PanelContent.addView(link_speed);
		addView(PanelContent, Params_channel);
		
		
		/**
		 * CAPABILITIES ICON, TEXT
		 */
		LinearLayout.LayoutParams Params_cap = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		Params_cap.weight = 0.99f;
		LinearLayout PanelCAP = new LinearLayout(context);
		PanelCAP.setOrientation(LinearLayout.VERTICAL);
		PanelCAP.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		
			String security_cap = security_string(ap.getCapabilities());
			// LOCKER IMAGE
			ImageView un_locker_img = new ImageView(context);
			un_locker_img.setPadding(5, 10, 5, 0);
			if(security_cap.equalsIgnoreCase("none"))
				un_locker_img.setImageDrawable(context.getResources().getDrawable(R.drawable.unlock));
			else
				un_locker_img.setImageDrawable(context.getResources().getDrawable(R.drawable.lock));
			//image:add
			PanelCAP.addView(un_locker_img);
			
			// SECURITY PROTOCOL TEXT
			TextView capabilities = new TextView( context );
			LinearLayout.LayoutParams capabilities_params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			capabilities_params.gravity = Gravity.CENTER_HORIZONTAL;
			capabilities.setLayoutParams(capabilities_params);
			capabilities.setTextSize(12);
			capabilities.setPadding(10, 10, 0, 0);
			capabilities.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			String sec_prot =  "<font color=#4fa5d5>" + security_cap + "</font>";
			capabilities.setText( Html.fromHtml(sec_prot), TextView.BufferType.SPANNABLE );
			PanelCAP.addView(capabilities);     
		addView(PanelCAP,Params_cap);
	}
	
	
	/**
	 * @param capabilities, the string returned from the wifi scanner
	 * @return string with the security protocols
	 */
	public String security_string(String capabilities){
		StringBuilder security = new StringBuilder();
		
		if(capabilities.toLowerCase().contains("wpa2")){
			security.append("WPA2");
			return security.toString();
			//capabilities.replace("wpa2", "");
		}
		if(capabilities.toLowerCase().contains("wpa")){
			//if(security.toString().length() != 0){
			//	security.append("/");
			//}
			security.append("WPA");
			return security.toString();
			//capabilities.replace("wpa", "");
		}
		if(capabilities.toLowerCase().contains("wep")){
			//if(security.toString().length() != 0){
			//	security.append("/");
			//}
			security.append("WEP");
			return security.toString();
			//capabilities.replace("wep", "");
		}
		
		// There is no security
		if(security.toString().trim().length() == 0){
			security.append("none");
		}
		
		return security.toString();
	}
	
	
	/**
	 * 
	 * @param rssi : int, choose the appropriate pin with your eye on the rssi value
	 */
	private Drawable icon_Choose_and_Aplly_wifi(int rssi){
		Drawable icon = null; 
		try{
			if(rssi <= -95){
				icon = this.getResources().getDrawable(R.drawable.signal_00);
			}
			else if(rssi > -95 && rssi <= -75 ){
				icon = this.getResources().getDrawable(R.drawable.signal_11);
			}
			else if(rssi > -75 && rssi <= -55){
				icon = this.getResources().getDrawable(R.drawable.signal_22);
			}
			else if(rssi > -55 && rssi <= -35){
				icon = this.getResources().getDrawable(R.drawable.signal_33);
			}else if(rssi > -35){
				icon = this.getResources().getDrawable(R.drawable.signal_44);
			}
			// Create the overlay object
		}catch(Exception e){
			icon = this.getResources().getDrawable(R.drawable.signal_00);
		}
		return icon;
	}

	
}
