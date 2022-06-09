package com.ioigoume.eketamobilitytool.opengl;


import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GLView extends SurfaceView implements SurfaceHolder.Callback{

	private GLThread glThread = null;
	
	public GLView(Context context) {
		super(context);
		// Get notified when the underlying surface is created or destroyed
		getHolder().addCallback(this);
		//
		getHolder().lockCanvas();
		// User hardware acceleration if available
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
		// Make sure we get key events
		setFocusable(true);
		
	}
	
	
	public GLView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// Get notified when the underlying surface is created or destroyed
		getHolder().addCallback(this);
		// User hardware acceleration if available
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
		// Make sure we get key events
		setFocusable(true);
		
	}

	
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder arg0) {
		// The surface has been created so start our drawing thread
		glThread = new GLThread(this);
		glThread.start();
		
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		// Stop our drawing thread
		glThread.requestExitAndWait();
		glThread = null;
		
	}

}
