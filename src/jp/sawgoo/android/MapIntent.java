package jp.sawgoo.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.sawgoo.android.common.Type;
import jp.sawgoo.android.event.LocationAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapIntent extends MapActivity {

	private static final String URL = "http://sawgoo.jp/api/get_data.php";
	private static Drawable[] drawables = new Drawable[Type.ICONS.length];
	private MapView mapView;
	private MapController mapController;
	private LocationManager locationManager;
	private LocationAdapter locationAdapter;
	private MarkersOverlay overlay;

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Debug.isDebuggerConnected()) {
			Debug.startMethodTracing("findout");
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		/*
		 * GUIの処理
		 */
		mapView = (MapView) findViewById(R.id.map);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(15);

		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationAdapter = new LocationAdapter();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationAdapter);
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		GeoPoint gp = new GeoPoint(35658144, 139701412);
		if (location != null) {
			gp = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
		}
		mapController.animateTo(gp);

		for (int i = 0; i < Type.ICONS.length; i++) {
			drawables[i] = getResources().getDrawable(Type.ICONS[i]);
			drawables[i].setBounds(0, 0, drawables[i].getIntrinsicWidth(), drawables[i].getIntrinsicHeight());
		}

		overlay = new MarkersOverlay(null);
		mapView.getOverlays().add(overlay);

		/*
		 * レスポンスデータのXML解析処理
		 * 別スレッドで処理したHTTP通信から取得したXMLデータを、ItemizedOverlayにマップする。
		 */
		final XmlPullParser parser = Xml.newPullParser();
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj == null) return;
				int count = 0;
				int latitude = 0;
				int longtitude = 0;
				String name = null;
				String twitter = null;
				Drawable icon = null;
				try {
					parser.setInput((InputStream)msg.obj, "UTF-8");
					for(int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT; e = parser.next()){
						if (e == XmlPullParser.START_TAG) {
//							Log.d(getString(R.string.app_name), parser.getName());
							if (parser.getName().equals("type")) {
								String type = parser.nextText();
								icon = findIcon(type);
							}
							else if (parser.getName().equals("latitude")) {
								latitude = (int)(Double.parseDouble(parser.nextText()) * 1E6);
							}
							else if (parser.getName().equals("longtitude")) {
								longtitude = (int)(Double.parseDouble(parser.nextText()) * 1E6);
							}
							else if (parser.getName().equals("name")) {
								name = parser.nextText();
							}
							else if (parser.getName().equals("twitter")) {
								twitter = parser.nextText();
							}
						}
						else if (e == XmlPullParser.END_TAG && parser.getName().equals("item")) {
							overlay.addNewItem(new GeoPoint(latitude, longtitude), name, name + "/@" + twitter, icon);
							count++;
						}
					}
				} catch (FileNotFoundException e) {
					Log.getStackTraceString(e);
					Toast t = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
					t.show();
				} catch (IOException e) {
					Log.getStackTraceString(e);
					Toast t = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
					t.show();
				} catch (XmlPullParserException e) {
					Log.getStackTraceString(e);
					Toast t = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
					t.show();
				}
			}
		};
		/*
		 * HTTP通信は別スレッドで処理する。1秒毎にループを回し、レスポンスが取得できたらループを抜ける。
		 * MAX30回でタイムアウトとする。レスポンスデータはHandlerにコールバック。
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						int loopCnt = 0;
						InputStream content = null;
						DefaultHttpClient client = new DefaultHttpClient();
						client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true) {
							@Override
							public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
								Log.d(getString(R.string.app_name), "Count - " + executionCount);
								Log.getStackTraceString(exception);
								return super.retryRequest(exception, executionCount, context);
							}
						});
						HttpGet request = new HttpGet(URL);
						HttpParams params = client.getParams();
						HttpConnectionParams.setConnectionTimeout(params, 3000);
						HttpConnectionParams.setSoTimeout(params, 3000);
						params.setParameter("http.useragent", "android-sdk-7");
						try {
							HttpResponse response = client.execute(request);
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								HttpEntity entity = response.getEntity();
								content = entity.getContent();
							}
							client.getConnectionManager().shutdown();
						} catch (ClientProtocolException e) {
							Log.e(getString(R.string.app_name), null, e);
						} catch (IOException e) {
							Log.e(getString(R.string.app_name), null, e);
						}
						while (loopCnt < 30) {
							try {
								loopCnt++;
								Thread.sleep(1000);
								Log.d(getString(R.string.app_name), "Loading...");
							} catch (InterruptedException e) {
								Log.e(getString(R.string.app_name), null, e);
							}
							if (content != null) {
								Log.d(getString(R.string.app_name), "Load Complete!!!");
								break;
							}
						}
						Message msg = new Message();
						msg.obj = content;
						handler.sendMessage(msg);
					}
				});
			}
		}).start();
		mapView.invalidate();
	}

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		Log.d(getString(R.string.app_name), "MapActivity.isRouteDisplayed");
		return false;
	}

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		Log.d(getString(R.string.app_name), "MapActivity.onPause");
		if (locationManager != null) {
			locationManager.removeUpdates(locationAdapter);
		}
		super.onPause();
	}

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.d(getString(R.string.app_name), "MapActivity.onResume");
		if (locationManager != null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationAdapter);
		}
		super.onResume();
	}

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(getString(R.string.app_name), "MapActivity.onDestroy");
		if (overlay != null) {
			mapView.getOverlays().remove(overlay);
			overlay.clear();
		}
		super.onDestroy();
		if (Debug.isDebuggerConnected()) {
			Debug.stopMethodTracing();
		}
	}

	private Drawable findIcon(String type) {
		for (int i = 0; i < Type.NAMES.length; i++) {
			if (Type.NAMES[i].equals(type)) {
				return drawables[i];
			}
		}
		return null;
	}
	
	/**
	 * Overlay
	 *
	 */
	class MarkersOverlay extends ItemizedOverlay<OverlayItem> {
		private static final int NOT_GEOPOINT = -1;
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

		/**
		 * 指定したアイコンでコンストラクタを構築する。
		 * @param defaultMarker
		 */
		public MarkersOverlay(Drawable defaultMarker) {
			super(defaultMarker);
			populate();
		}

		/*
		 * (非 Javadoc)
		 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
		 */
		@Override
		protected OverlayItem createItem(int index) {
			return items.get(index);
		}

		/*
		 * (非 Javadoc)
		 * @see com.google.android.maps.ItemizedOverlay#size()
		 */
		@Override
		public int size() {
			return items.size();
		}

		/*
		 * (非 Javadoc)
		 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
		 */
		@Override
		protected boolean onTap(int index) {
			// マップの中心座標を、タップされたアイテムに合わせる
			// mapControlerは、パッケージスコープで宣言
			GeoPoint gp = this.getItem(index).getPoint();
			mapController.animateTo(gp);
			Toast t = Toast.makeText(getApplicationContext(), this.getItem(index).getTitle(), Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER_HORIZONTAL, 0, 40);
			t.show();
			return true;
		}

		/**
		 * 新しい位置を追加する。但し、同じ位置がリストに存在したら追加しない。
		 * @param point 位置
		 * @param markerText マーカーに付随する文字列
		 * @param snippet 断片文字列
		 */
		public void addNewItem(GeoPoint point, String markerText, String snippet, Drawable marker) {
			if (getIndexGeoPoint(point) == NOT_GEOPOINT && marker != null) {
				boundCenterBottom(marker);
				OverlayItem item = new OverlayItem(point, markerText, snippet);
				item.setMarker(marker);
				items.add(item);
				populate();
			}
		}

		/**
		 * 指定されたインデックスのマーカーを削除する。
		 * @param index マーカー数より大きいインデックスが指定された場合は何もしない。
		 */
		public void removeItem(int index) {
			if (index < size()) {
				items.remove(index);
				populate();
			}
		}

		/**
		 * 指定された位置のマーカーを削除する。
		 * @param point 同じ位置が存在すれば削除。
		 */
		public void removeGeoPoint(GeoPoint point) {
			int idx = getIndexGeoPoint(point);
			if (idx != NOT_GEOPOINT) {
				removeItem(idx);
			}
		}

		/**
		 * マーカーを全てクリアする。
		 */
		public void clear() {
			items.clear();
			populate();
		}

		// 位置が一致するか (一致しない場合は NOT_GEOPOINTを返却)
		private int getIndexGeoPoint(GeoPoint newPoint) {
			int result = NOT_GEOPOINT;
			int size = items.size();
			GeoPoint point;
			for (int i = 0; i < size; i++) {
				point = items.get(i).getPoint();
				if (point.equals(newPoint)) {
					result = i;
					break;
				}
			}
			return result;
		}
	}
}