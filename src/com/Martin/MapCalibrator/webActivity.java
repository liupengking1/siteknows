package com.Martin.MapCalibrator;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class webActivity extends Activity {

	private ActionBar actionBar;
	private WebView myWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		// Initilization
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		// actionBar.setHomeAsUpIndicator(R.drawable.btn_back);

		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.rgb(0, 193, 164)));
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.WHITE));
		actionBar.setSplitBackgroundDrawable(new ColorDrawable(Color.rgb(0,
				193, 164)));
		int titleId = Resources.getSystem().getIdentifier("action_bar_title",
				"id", "android");
		TextView yourTextView = (TextView) findViewById(titleId);
		yourTextView.setTextColor(Color.WHITE);

		myWebView = (WebView) findViewById(R.id.webview);
		myWebView.setWebChromeClient(new WebChromeClient());
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		myWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		myWebView.loadUrl("http://streambels.com");

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (myWebView.canGoBack()) {
					myWebView.goBack();
				} else {
					finish();
				}
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	//
	// menu.add(0, Menu.FIRST, 0, "Calibrate");
	// return super.onCreateOptionsMenu(menu);
	// }
	//
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// if (item.getItemId() == Menu.FIRST) {
		// Intent i = new Intent(getApplicationContext(),
		// com.Martin.MapCalibrator.MapCalibrator.class);
		// startActivity(i);
		// }
		if (item.getItemId() == android.R.id.home) {
			// app icon in action bar clicked; goto parent activity.
			this.finish();
		}

		return super.onOptionsItemSelected(item);
	}

}
