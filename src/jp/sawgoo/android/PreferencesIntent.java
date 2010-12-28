package jp.sawgoo.android;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PreferencesIntent extends Activity {

	private SharedPreferences sharedPref;
	private Twitter twitter = null;
	private RequestToken requestToken = null;
	private List<OAuthData> dataList;
	private AccountListAdapter adapter;
	private Button btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		dataList = new ArrayList<OAuthData>();
		int accounts = sharedPref.getInt("account_size", 0);
		for (int i = 0; i < accounts; i++) {
			String token = sharedPref.getString("token_" + i, "");
			String tokenSecret = sharedPref.getString("token_secret_" + i, "");
			int userId = sharedPref.getInt("user_id_" + i, 0);
			String screenName = sharedPref.getString("screen_name_" + i, "");
			dataList.add(new OAuthData(token, tokenSecret, userId, screenName));
		}

		btn = (Button) findViewById(R.id.twitterAuth);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					TwitterFactory factory = new TwitterFactory();
					twitter = factory.getInstance();
					twitter.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));
					requestToken = twitter.getOAuthRequestToken("oob");
					String authUrl = requestToken.getAuthorizationURL();
					Intent twitterOAuthIntent = new Intent(PreferencesIntent.this, TwitterOAuthIntent.class);
					twitterOAuthIntent.putExtra("authorizeUrl", authUrl);
					startActivityForResult(twitterOAuthIntent, 1);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
		});
		if (accounts == 0) {
			btn.setVisibility(View.VISIBLE);
		} else {
			btn.setVisibility(View.GONE);
		}

		adapter = new AccountListAdapter(this, R.layout.prefrow, dataList);
		ListView listView = (ListView)this.findViewById(R.id.list);
		listView.setAdapter(adapter);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				try {
					AccessToken accessToken = null;
					accessToken = twitter.getOAuthAccessToken(requestToken, data.getStringExtra("oauthPin"));
					Log.d("AccessToken", accessToken.getToken());
					
					String _token = accessToken.getToken();
					String _tokenSecret = accessToken.getTokenSecret();
					int _userId = accessToken.getUserId();
					String _screenName = accessToken.getScreenName();
					
					int index = -1;
					for (int i = 0; i < dataList.size(); i++) {
						OAuthData tmpData = dataList.get(i);
						Log.d("debug", String.valueOf(tmpData.getUserId()));
						if (tmpData.getUserId() == _userId) {
							Log.d("debug", "match");
							index = i;
							break;
						}
					}
					Editor editor = sharedPref.edit();
					if (index == -1) {
						int newidx = dataList.size();
						dataList.add(new OAuthData(_token, _tokenSecret, _userId, _screenName));
						editor.putString("token_" + newidx, _token);
						editor.putString("token_secret_" + newidx, _tokenSecret);
						editor.putInt("user_id_" + newidx, _userId);
						editor.putString("screen_name_" + newidx, _screenName);
						editor.putInt("account_size", dataList.size());
					} else {
						dataList.set(index, new OAuthData(_token, _tokenSecret, _userId, _screenName));
						editor.putString("token_" + index, _token);
						editor.putString("token_secret_" + index, _tokenSecret);
						editor.putInt("user_id_" + index, _userId);
						editor.putString("screen_name_" + index, _screenName);
					}
					editor.commit();
					adapter.notifyDataSetChanged();
					btn.setVisibility(View.GONE);
				} catch (TwitterException e) {
					if (401 == e.getStatusCode()) {
						System.out.println("Unable to get the access token.");
					}else{
						e.printStackTrace();
					}
				}
			} else {
				// Cancel
			}
		}
	}
	
	class AccountListAdapter extends ArrayAdapter<OAuthData>{
		private List<OAuthData> items;
		private LayoutInflater inflater;
		
		public AccountListAdapter(Context context, int resourceId, List<OAuthData> items) {
			super(context, resourceId, items);
			this.items = items;
			this.inflater = getLayoutInflater();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.prefrow, null);
			}
			OAuthData tmpData = (OAuthData)items.get(position);
			final OAuthData data = tmpData;
			TextView userId = (TextView)v.findViewById(R.id.user_id);
			userId.setText("@" + String.valueOf(tmpData.getScreenName()));
			
			Button delButton = (Button) v.findViewById(R.id.del_button);
			delButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					items.remove(data);
					
					Set<String> keySet = sharedPref.getAll().keySet();
					Iterator<String> it = keySet.iterator();
					Editor editor = sharedPref.edit();
					while (it.hasNext()) {
						String key = it.next();
						if (key.startsWith("token_") || key.startsWith("user_id_") || key.startsWith("screen_name_")) {
							editor.remove(key);
						}
					}
					editor.commit();
					int size = items.size();
					for (int i = 0; i < size; i++) {
						OAuthData data = items.get(i);
						editor.putString("token_" + i, data.getToken());
						editor.putString("token_secret_" + i, data.getTokenSecret());
						editor.putInt("user_id_" + i, data.getUserId());
						editor.putString("screen_name_" + i, data.getScreenName());
					}
					editor.putInt("account_size", size);
					editor.commit();
					adapter.notifyDataSetChanged();
					if (dataList.size() == 0) {
						btn.setVisibility(View.VISIBLE);
					}
				}
			});
			return v;
		}
	}
	
	final class OAuthData implements Serializable {
		private static final long serialVersionUID = -7267814615880475181L;
		private String token;
		private String tokenSecret;
		private int userId;
		private String screenName;
		public OAuthData(String token, String tokenSecret, int userId, String screenName) {
			this.token = token;
			this.tokenSecret = tokenSecret;
			this.userId = userId;
			this.screenName = screenName;
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public String getTokenSecret() {
			return tokenSecret;
		}
		public void setTokenSecret(String tokenSecret) {
			this.tokenSecret = tokenSecret;
		}
		public int getUserId() {
			return userId;
		}
		public void setUserId(int userId) {
			this.userId = userId;
		}
		public String getScreenName() {
			return screenName;
		}
		public void setScreenName(String screenName) {
			this.screenName = screenName;
		}
	}
}
