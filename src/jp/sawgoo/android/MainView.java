package jp.sawgoo.android;

import java.io.IOException;

import jp.sawgoo.android.common.Type;
import jp.sawgoo.android.event.LocationAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import twitter4j.GeoLocation;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainView extends Activity {

	private static final int DIALOG_CONFIRM = 1;
	private static final int DIALOG_PROGRESS = 2;
	private static final int DIALOG_COMPLETE  = 3;
	private static final int DIALOG_NONETWORK = 4;
	private static final int DIALOG_NOGPS = 5;
	private static final String URL = "http://sawgoo.jp/api/create_data.php";
	private LocationManager locationManager;
	private LocationAdapter locationAdapter;
	private Location location = null;
	private String type = null;

	/*
	 * (非 Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				processClick(v);
			}
		};
		View.OnTouchListener touchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setPressed(true);
					if (!v.isFocused()) v.requestFocus();
					break;
				case MotionEvent.ACTION_UP:
					v.setPressed(false);
					processClick(v);
					break;
				}
				return true;
			}
		};

		for (int id : Type.BUTTONS) {
			ImageButton btn = (ImageButton) findViewById(id);
			btn.setOnClickListener(clickListener);
			btn.setOnTouchListener(touchListener);
		}

		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationAdapter = new LocationAdapter();
	}

	/*
	 * (非 Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainView.this);
		switch (id) {
		// 送信確認ダイアログ
		case DIALOG_CONFIRM:
			builder = new AlertDialog.Builder(MainView.this);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(R.string.post_message);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CONFIRM);
					showDialog(DIALOG_PROGRESS);
					request();
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialoginterface) {
					Log.d(getString(R.string.app_name), "alert dialog was canceled");
					removeDialog(DIALOG_CONFIRM);
				}
			});
			return builder.create();
		// 送信中ダイアログ
		case DIALOG_PROGRESS:
			ProgressDialog progress = new ProgressDialog(MainView.this);
			progress.setTitle(getString(R.string.post_progress));
			progress.setButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialoginterface) {
					Log.d(getString(R.string.app_name), "progress dialog was dismissed");
					Log.d(getString(R.string.app_name), "sending request was canceled");
					removeDialog(DIALOG_PROGRESS);
				}
			});
			return progress;
		// 送信完了ダイアログ
		case DIALOG_COMPLETE:
			builder = new AlertDialog.Builder(MainView.this);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(R.string.post_complete);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_COMPLETE);
				}
			});
			return builder.create();
		// ネットワーク不通
		case DIALOG_NONETWORK:
			builder = new AlertDialog.Builder(MainView.this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.network_unavailble);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_NONETWORK);
				}
			});
			return builder.create();
		// GPS不能
		case DIALOG_NOGPS:
			builder = new AlertDialog.Builder(MainView.this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.gps_unavailble);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_NOGPS);
				}
			});
			return builder.create();
		default:
			return null;
		}
	}

	/*
	 * (非 Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return true;
	}

	/*
	 * (非 Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(getString(R.string.app_name), item.getTitle() + ", " + item.getItemId());
		switch (item.getItemId()) {
		case R.id.prefmode:
			startActivity(new Intent(this, PreferencesIntent.class));
			break;
		case R.id.mapmode:
			startActivity(new Intent(this, MapIntent.class));
			break;
		}
		return true;
	}

	private void processClick(View v) {
		// 圏外のチェック
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
		if (nwInfo == null || !nwInfo.isAvailable()) {
			Log.w(getString(R.string.app_name), "NetworkInfo : " + nwInfo);
			showDialog(DIALOG_NONETWORK);
			return;
		} else {
			Log.i(getString(R.string.app_name), "NetworkInfo : " + nwInfo.getTypeName());
		}
		// GPSステータスのチェック
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationAdapter);
		location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
		if (location == null) {
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		if (location == null) {
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (location == null) {
			showDialog(DIALOG_NOGPS);
			return;
		}

		setType(v);
		if (type == null) {
			return;
		}
		Log.d(getString(R.string.app_name), Integer.toHexString(v.getId()).toUpperCase() + ", " + type);
		showDialog(DIALOG_CONFIRM);
	}

	private void request() {
		DefaultHttpClient client = new DefaultHttpClient();
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true) {
			@Override
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				Log.d(getString(R.string.app_name), "Count - " + executionCount);
				Log.getStackTraceString(exception);
				return super.retryRequest(exception, executionCount, context);
			}
		});
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		StringBuffer buf = new StringBuffer();
		buf.append(URL);
		buf.append("?type=").append(type);
		buf.append("&lat=").append(location.getLatitude());
		buf.append("&lng=").append(location.getLongitude());
		buf.append("&name=").append(pref.getString("display_name", ""));
		String screenName = pref.getString("screen_name_0", "");
		if (!screenName.equals("")) {
			buf.append("&twtr=").append(screenName);
		}
		Log.d(MainView.class.getName(), buf.toString());
		HttpGet request = new HttpGet(buf.toString());
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 3000);
		HttpConnectionParams.setSoTimeout(params, 3000);
		//TODO キャリア・機種情報をユーザエージェントにセットする（要Permission）
		//SIMの情報はWi-Fi接続時に取得できないからやらない。
		String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.System.ANDROID_ID);
		String sdk = Build.VERSION.RELEASE;
		String device = Build.BRAND;
		params.setParameter("http.useragent", "Android " + sdk + "/" + device);
		try {
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				removeDialog(DIALOG_PROGRESS);
				showDialog(DIALOG_COMPLETE);
			} else {
				Log.d(getString(R.string.app_name), "Response Status - " + response.getStatusLine().getStatusCode());
				dismissDialog(DIALOG_PROGRESS);
			}
			client.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			// Unknown host,
			Log.e(getString(R.string.app_name), null, e);
		} catch (IOException e) {
			Log.e(getString(R.string.app_name), null, e);
		}
		// Update status to Twitter
		EditText text = (EditText) findViewById(R.id.EditText01);
		GeoLocation gl = new GeoLocation(location.getLatitude(), location.getLongitude());
		TwitterFactory factory = new TwitterFactory();
		int cnt = pref.getInt("account_size", 0);
		String consumerKey = getString(R.string.consumer_key);
		String consumerSecret = getString(R.string.consumer_secret);
		for (int i = 0; i < cnt; i++) {
			String token = pref.getString("token_" + i, "");
			String tokenSecret = pref.getString("token_secret_" + i, "");
			if (token.length() > 0 && tokenSecret.length() > 0) {
				AccessToken accessToken = new AccessToken(token, tokenSecret);
				Twitter twitter = factory.getOAuthAuthorizedInstance(consumerKey, consumerSecret, accessToken);
				try {
					twitter.updateStatus(text.getText().toString(), gl);
				} catch (TwitterException e) {
					int status = e.getStatusCode();
					Log.e("Error", status + ", " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	private void setType(View v) {
		int id = v.getId();
		for (int i = 0; i < Type.BUTTONS.length; i++) {
			if (Type.BUTTONS[i] == id) {
				type = Type.NAMES[i];
			}
		}
	}
}
