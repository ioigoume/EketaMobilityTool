package com.ioigoume.eketamobilitytool;

import android.app.Activity;
import android.os.Bundle;


// The staff in this class will be viewed from every class that inherits the baseclass, targeting activity
public class BaseClass extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/**
		 * 	HERE PUT ANY DATA YOU WANT TO SHARE BETWEEN ACTIVITY CLASSES
		 */
	}

}
