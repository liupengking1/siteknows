package com.Martin.MapCalibrator;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class webActivity extends Activity {

	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		// Initilization
		actionBar = getActionBar();
		// actionBar.setHomeButtonEnabled(false);
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		WebView myWebView = (WebView) findViewById(R.id.webview);
		myWebView.loadUrl("http://v3g742.axshare.com/weixin.html");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, Menu.FIRST, 0, "Calibrate");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			Intent i = new Intent(getApplicationContext(),
					com.Martin.MapCalibrator.MapCalibrator.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

}
