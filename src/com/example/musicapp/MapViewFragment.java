package com.example.musicapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MapViewFragment extends Fragment {
	private MapView mMapView;
	private GoogleMap mMap;
	private double longitude;
	private double latitude;
	private Bundle mBundle;
	private String userName = "";
	private String pictureURL = "";
	final ParseObject gameScore = new ParseObject("songs");
	private String userID = "";
	private String graph = "http://graph.facebook.com/";
	LocationListener locationListener = new MyLocationListener();
	GPSTracker gps;

	private final class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location locFromGps) {
			// called when the listener is notified with a location update from
			// the GPS
			gps.longitude = locFromGps.getLongitude();
			gps.latitude = locFromGps.getLatitude();

		}

		@Override
		public void onProviderDisabled(String provider) {
			// called when the GPS provider is turned off (user turning off the
			// GPS on the phone)
		}

		@Override
		public void onProviderEnabled(String provider) {
			// called when the GPS provider is turned on (user turning on the
			// GPS on the phone)
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// called when the status of the GPS provider changes
		}
	}

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);

		mBundle = savedInstanceState;

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ParseQuery query = new ParseQuery("songs");
		query.addDescendingOrder("createdAt");
		query.setLimit(10);
		ParseGeoPoint a = new ParseGeoPoint();
		a.setLatitude(latitude);
		a.setLongitude(longitude);

		query.whereWithinKilometers("location", a, 1);
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				// TODO Auto-generated method stub
				System.out.println("I'm here");
				for (int i = 0; i < objects.size(); i++) {
					System.out.println("yo");
					ParseGeoPoint a = null;
					if (objects.get(i).get("location") != null) {
						a = (ParseGeoPoint) objects.get(i).get("location");
						mMap.addMarker(new MarkerOptions()
								.position(
										new LatLng(a.getLatitude(), a
												.getLongitude()))
								.title((String) objects.get(i).get("artist"))
								.snippet(objects.get(i).get("song").toString())
								.icon(BitmapDescriptorFactory
										.fromBitmap(getResizedBitmap(
												getBitmapFromURL(objects.get(i)
														.get("profilePic")
														.toString()), 48, 48))));

					}
				}
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();

		mMapView.onResume();

	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");
			Log.d("mIntentReceiver.onReceive ", action + " / " + cmd);
			String artist = intent.getStringExtra("artist");
			String album = intent.getStringExtra("album");
			String track = intent.getStringExtra("track");
			Log.d("Music", artist + ":" + album + ":" + track);
			Toast.makeText(getActivity(),
					"Music: " + artist + ":" + album + ":" + track,
					Toast.LENGTH_LONG).show();

			gameScore.put("artist", artist);

			gameScore.put("album", album);
			gameScore.put("song", track);
			gameScore.put("location", new ParseGeoPoint(latitude, longitude));

			try {
				gameScore.save();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshListView();

		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View inflatedView = inflater
				.inflate(R.layout.mapview, container, false);

		try {
			MapsInitializer.initialize(getActivity());
		} catch (GooglePlayServicesNotAvailableException e) {
			// TODO handle this situation
		}

		IntentFilter iF = new IntentFilter();
		iF.addAction("com.android.music.metachanged");

		iF.addAction("com.htc.music.metachanged");

		iF.addAction("fm.last.android.metachanged");
		iF.addAction("com.sec.android.app.music.metachanged");
		iF.addAction("com.nullsoft.winamp.metachanged");
		iF.addAction("com.amazon.mp3.metachanged");
		iF.addAction("com.miui.player.metachanged");
		iF.addAction("com.real.IMP.metachanged");
		iF.addAction("com.sonyericsson.music.metachanged");
		iF.addAction("com.rdio.android.metachanged");
		iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
		iF.addAction("com.andrew.apollo.metachanged");

		getActivity().registerReceiver(mReceiver, iF);

		mMapView = (MapView) inflatedView.findViewById(R.id.map);
		mMapView.onCreate(mBundle);
		// create class object
		gps = new GPSTracker(getActivity());
		setUpMapIfNeeded(inflatedView);

		// check if GPS enabled
		if (gps.canGetLocation()) {

			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			gps.locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

			gps.locationManager
					.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							1000, 1, locationListener);
		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsAlert();

		}
		return inflatedView;

	}

	private void setUpMapIfNeeded(View inflatedView) {
		if (mMap == null) {
			mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {

		longitude = gps.getLongitude();
		latitude = gps.getLatitude();
		LatLng a = new LatLng(latitude, longitude);
		mMap.addCircle(new CircleOptions().center(a).radius(300).strokeWidth(2));
		gps.locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 35000, 1, locationListener);

		gps.locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 35000, 1, locationListener);

		if (!mMap.isMyLocationEnabled()) {

			mMap.setMyLocationEnabled(true);
		}
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
				new LatLng(latitude, longitude), 15);

		mMap.moveCamera(cameraUpdate);

	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {

			makeMeRequest(session);

		}
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {

							if (user != null) {
								userID = user.getId();
								userName = user.getName();
								pictureURL = graph + userID + "/picture";
								gameScore.put("username", userName);
								gameScore.put("image", pictureURL);

							}
						}
					}
				});
		request.executeAsync();

	}

	private void refreshListView() {
		/* clear the existing list in the adapter */
		mMap.clear();
		setUpMap();
		ParseQuery query = new ParseQuery("songs");
		ParseGeoPoint a = new ParseGeoPoint();
		a.setLatitude(latitude);
		a.setLongitude(longitude);

		query.whereWithinKilometers("location", a, 1);

		query.addDescendingOrder("createdAt");
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				// TODO Auto-generated method stub
				System.out.println("I'm here");
				for (int i = 0; i < objects.size(); i++) {
					ParseGeoPoint b = null;
					if (objects.get(i).get("location") != null) {
						b = (ParseGeoPoint) objects.get(i).get("location");

						mMap.addMarker(new MarkerOptions()
								.position(
										new LatLng(b.getLatitude(), b
												.getLongitude()))
								.title((String) objects.get(i).get("artist"))
								.snippet(objects.get(i).get("song").toString())
								.icon(BitmapDescriptorFactory
										.fromBitmap(getResizedBitmap(
												getBitmapFromURL(objects.get(i)
														.get("profilePic")
														.toString()), 48, 48))));
					}

				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();

		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

}
