package com.ioigoume.eketamobilitytool;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class UmtsInfo extends Activity {

	TextView umtsInfo = null;
	StringBuilder netInfo = new StringBuilder();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_umts_info);
        
        umtsInfo = (TextView)findViewById(R.id.umts_info_view);
        
        netInfo.append("<(value):rssi in dBm>\n");
        netInfo.append("(0):-113dBm or less\n");
        netInfo.append("(1):-111dBm\n");
        netInfo.append("(2...30):-109... -53dBm\n");
        netInfo.append("(99):not known or not detectable\n");
        netInfo.append("---------------------------------------\n");
        netInfo.append("<(RXQUAL value): range of actual BER(%)>\n");
        netInfo.append("(0 RXQUAL)Less than 0,1%\n");
        netInfo.append("(1 RXQUAL)0,26% to 0,30%\n");
        netInfo.append("(2 RXQUAL)0,51% to 0,64%\n");
        netInfo.append("(3 RXQUAL)1,0% to 1,3%\n");
        netInfo.append("(4 RXQUAL)1,9% to 2,7%\n");
        netInfo.append("(5 RXQUAL)3,8% to 5,4%\n");
        netInfo.append("(6 RXQUAL)7,6% to 11,0%\n");
        netInfo.append("(7 RXQUAL)greater than 15,0%\n");
        
        umtsInfo.setText(netInfo.toString());
        
    }    
}
