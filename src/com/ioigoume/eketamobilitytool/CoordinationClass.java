package com.ioigoume.eketamobilitytool;

import android.util.Log;

public class CoordinationClass extends BaseClass{

	private int longtitude;
	private int latitude;
	private int altitude;
	
	public CoordinationClass(){
		latitude = -1;
		longtitude = -1;
		altitude = -1;
	}
	
	public synchronized int getLongtitude() {
		return longtitude;
	}

	public synchronized void setLongtitude(int longtitude) {
		this.longtitude = longtitude;
	}
	
	public synchronized int getLatitunde() {
		return latitude;
	}

	public synchronized void setLatitunde(int latitude) {
		this.latitude = latitude;
	}

	public int getAltitude() {
		return altitude;
	}

	
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}
	
	public double Latitunde_normal(){
		return (double)this.latitude / 1E6;
	}

	public double Longtitude_normal(){
		return (double)this.longtitude / 1E6;
	}
	
	public double Altitude_normal(){
		return (double)this.altitude / 1E6;
	}
	
	public synchronized void print(){
		Log.d("CoordinationClass - print","Longtitude = " + String.valueOf(longtitude));
		Log.d("CoordinationClass - print","Latitunde = " + String.valueOf(latitude));
		Log.d("CoordinationClass - print","Altitude = " + String.valueOf(altitude));
		
	}

}
