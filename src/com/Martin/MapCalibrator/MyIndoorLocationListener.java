package com.Martin.MapCalibrator;

import android.content.Context;

//get WiFi location from server

public class MyIndoorLocationListener implements IndoorLocationListener {
	private MyDrawableImageView view;
	private int lastLocation;
	UpdateIndoorLocation updateindoorlocation;

	protected MyIndoorLocationListener(MyDrawableImageView view, Context context) {
		this.view = view;
		updateindoorlocation=new UpdateIndoorLocation();
		updateindoorlocation.setTheListener(this, context);
	}

	protected void startListening() {
		System.out.println("indoorlocation updater: start listening");
		if (updateindoorlocation != null)
			updateindoorlocation.startScanningThread();
	}

	protected void stopListening() {
		System.out.println("indoorlocation updater: stop listening");
//		
//		if (updateindoorlocation != null)
//			updateindoorlocation.stopScanningThread();
	}

	protected int getLastLocation() {
		return lastLocation;
	}

	@Override
	public void locationupdated(int position) {
		lastLocation = position;
		// view.makeUseOfNewLocation(location);
		view.makeUseOfNewLocation(position);
//		view.makeUseOfNewLocation(new Location("new location"));
	}
}
