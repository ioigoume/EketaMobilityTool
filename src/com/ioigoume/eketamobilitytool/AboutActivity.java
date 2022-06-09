package com.ioigoume.eketamobilitytool;

import android.os.Bundle;
import android.widget.TextView;


public class AboutActivity extends BaseClass {

	TextView mytext;
	String abouttext = "\n\nHi,\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
						"\n\nNITLAB researchers developed the application " +
						"\nyou are holding on your hands." +
						"\nPress start to begin taking data, rssi, " +
						"\nsignal strength, handoffs,... and then " +
						"\nuse the view map utility to project your " +
						"\nposition and the data for its one.The data " +
						"\nare going to be stored in a local database. " +
						"\nUse the view content button to see the " +
						"\ndata in a gridview. " +
						"\nIf you wish you can delete them." +
						"\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tEnjoy!";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        mytext = (TextView) findViewById(R.id.about_text_view);
        mytext.setText(abouttext);
    }
    
}
