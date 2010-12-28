package jp.sawgoo.android.event;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class LocationAdapter implements LocationListener {

	private boolean gpsEnabled = false;

	/*
	 * (非 Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.d(LocationAdapter.class.getName(), "LocationListener.onLocationChanged");
	}

	/*
	 * (非 Javadoc)
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		Log.d(LocationAdapter.class.getName(), "LocationListener.onProviderDisabled");
		gpsEnabled = false;
	}

	/*
	 * (非 Javadoc)
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(LocationAdapter.class.getName(), "LocationListener.onProviderEnabled");
		gpsEnabled = true;
	}

	/*
	 * (非 Javadoc)
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(LocationAdapter.class.getName(), "LocationListener.onStatusChanged");
		switch (status) {
		case LocationProvider.AVAILABLE:
			Log.d(LocationAdapter.class.getName(), "AVAILABLE");
			gpsEnabled = true;
			break;
		case LocationProvider.OUT_OF_SERVICE:
			Log.d(LocationAdapter.class.getName(), "OUT_OF_SERVICE");
			gpsEnabled = false;
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.d(LocationAdapter.class.getName(), "TEMPORARILY_UNAVAILABLE");
			gpsEnabled = false;
			break;
		}
	}

	public boolean isGpsEnabled() {
		return gpsEnabled;
	}
}
