package jp.sawgoo.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterOAuthIntent extends Activity {
	
	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter);
		WebView webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		String authUrl = getIntent().getStringExtra("authorizeUrl");
		webView.loadUrl(authUrl);
		webView.addJavascriptInterface(new JsObj(), "Android");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				Log.d("WebKit", url);
				if (url.equals("http://api.twitter.com/oauth/authorize")) {
					view.loadUrl("javascript:Android.hoge(document.getElementById('oauth_pin').innerHTML);");
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d("WebKit", url);
				view.loadUrl(url);
				return true;
			}
		});
	}
	
	final class JsObj {
		public void hoge(String message) {
			Log.d("WebKit", message);
			if (message != null && message.length() > 0) {
				final String oauthPin = message;
				AlertDialog.Builder builder = new AlertDialog.Builder(TwitterOAuthIntent.this);
				builder.setIcon(android.R.drawable.ic_dialog_info);
				builder.setTitle("認証に成功しました。");
				builder.setMessage("認証キー" + message + "を保存して設定画面に戻ります。よろしいですか？");
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent preferencesIntent = new Intent(TwitterOAuthIntent.this, PreferencesIntent.class);
						preferencesIntent.putExtra("oauthPin", oauthPin);
						setResult(RESULT_OK, preferencesIntent);
						finish();
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
						setResult(RESULT_CANCELED);
						finish();
					}
				});
				builder.create().show();
			}
		}
	}
}
