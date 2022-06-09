package com.ioigoume.eketamobilitytool;

import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class ConsoleMonitorActivity extends BaseClass implements TelephoneInterface, WifiInterface{
	
	public static final String TAG = "ConsoleMonitorActivity";
	String sampling_time;
	String user_string;
	Handler myhandler = null;
	MobilityToolApplication myapp = null;
	private ConsoleTextClassThread mythread = null;
	private TextView infoText = null;


	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console_monitor);

		// Create the buttons
		myhandler = new Handler();
		myapp = (MobilityToolApplication)ConsoleMonitorActivity.this.getApplication();
		infoText = (TextView) findViewById(R.id.textViewInfoscreen);
		infoText.setMovementMethod(new ScrollingMovementMethod());
		
		mythread = new ConsoleTextClassThread(TAG);
		mythread.start();
		
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try{
			mythread.interrupt();
			mythread = null;
		} catch(Exception e){}
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mythread == null){
			mythread = new  ConsoleTextClassThread(TAG);
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		try{
			mythread.interrupt();
			mythread = null;
		} catch(Exception e){}
	}
	
	/**
	 * 
	 * @param mTextView : TextView
	 * @param text		: String , add text to TextView
	 */
	private void appendTextAndScroll(TextView mTextView, String text)
	{
	    if(mTextView != null){
	        mTextView.append(text + "\n");
	        final Layout layout = mTextView.getLayout();
	        if(layout != null){
	            int scrollDelta = layout.getLineBottom(mTextView.getLineCount() - 1) 
	                - mTextView.getScrollY() - mTextView.getHeight();
	            if(scrollDelta > 0)
	                mTextView.scrollBy(0, scrollDelta);
	        }
	    }
	}

	private class ConsoleTextClassThread extends Thread {
		private boolean isRunning;


		ConsoleTextClassThread(String ThreadName) {
			super(ThreadName);
			isRunning = false;
		} // constructor

		@SuppressWarnings("unused")
		public boolean isRunning() {
			return isRunning;
		}// isRunning

		@SuppressWarnings("unused")
		private void setIsRunning(boolean value) {
			isRunning = value;
		}// setIsRunning

		@Override
		public void run() {
			super.run();
			while (isRunning) {
				runOnUiThread(new Runnable(){
					public void run() {
						//infoText.setText(myapp.getPreferencesObjNonXml().getLogText());
						appendTextAndScroll(infoText, myapp.getPreferencesObjNonXml().getLogText());
					}
					
				});
				
				synchronized (this) {
					try {
						wait(Integer.parseInt(myapp.getPreferenceObj().getSamplingTime()));
					} catch (NumberFormatException e) {
						//e.printStackTrace();
					} catch (InterruptedException e) {
						//e.printStackTrace();
					} // try
				} // synchronized
			}// while
		}// run
		
		@Override
		public void interrupt() {
			isRunning = false;
		}

		@Override
		public synchronized void start() {
			super.start();
			isRunning = true;
		}
	}// ConsoleTextClassThread
}
