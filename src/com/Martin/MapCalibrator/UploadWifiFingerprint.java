package com.Martin.MapCalibrator;

import java.util.HashMap;
import java.util.List;

import java.io.*;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

public class UploadWifiFingerprint {
	private static final String TAG = "UploadWifiFingerprint";
	// public static final String userID =
	// android.provider.Settings.Secure.ANDROID_ID;
	WifiManager wifi;
	String room;
	int coordinate;

	Context context;

	/** Called when the activity is first created. */

	public void scanAndUpload(Context context, String room, int coordinate) {
		this.context = context;
		this.room = room;
		this.coordinate = coordinate;

		// Setup WiFi
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		Thread scan = new Thread() {
			@Override
			public void run() {
				Scan();
			}
		};
		scan.run();
		return;
	}

	public void Scan() {
		if (!wifi.isWifiEnabled()) {
			Toast.makeText(context, "WiFi is not open on this device",
					Toast.LENGTH_LONG).show();
		} else {
			wifi.startScan();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			ArrayList<List<ScanResult>> ScanList = new ArrayList<List<ScanResult>>(
					20);
			for (int scancount = 0; scancount < 20; scancount++) {
				wifi.startScan();
				try {
					Thread.sleep(1300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<ScanResult> results = this.wifi.getScanResults();
				ScanList.add(results);
				ScanResult bestSignal = null;
				for (ScanResult result : results) {
					if (bestSignal == null
							|| WifiManager.compareSignalLevel(bestSignal.level,
									result.level) < 0)
						bestSignal = result;
				}

				String message = String
						.format("%d round scan: %s networks found. %s is the strongest.",
								scancount, results.size(), bestSignal.SSID);
				Log.d(TAG, "onReceive() message: " + message);

				for (int i = 0; i < results.size(); i++) {
					String message1 = String
							.format("AP num: %d\nessid: %s \nMAC: %s\nfrequency: %s\nSig: %d",
									i + 1, results.get(i).SSID,
									results.get(i).BSSID,
									results.get(i).frequency,
									results.get(i).level);
					Log.d(TAG, "scan result: " + message1);
				}
			}

			HashMap<String, Double> map_sig = new HashMap<String, Double>();
			HashMap<String, Double> map_num = new HashMap<String, Double>();
			// TODO Compare 20 scan results and Calculate an average value
			// List<ScanResult> average;
			for (int i = 0; i < 20; i++) {
				List<ScanResult> res = ScanList.get(i);
				for (int j = 0; j < res.size(); j++) {
					if (!map_sig.containsKey(res.get(j).BSSID)) {
						map_sig.put(res.get(j).BSSID,
								Double.valueOf(res.get(j).level));
						map_num.put(res.get(j).BSSID, Double.valueOf(1));
					} else {
						map_sig.put(res.get(j).BSSID,
								(Double.valueOf(map_sig.get(res.get(j).BSSID))
										.doubleValue() + res.get(j).level));
						map_num.put(res.get(j).BSSID,
								(Double.valueOf(map_num.get(res.get(j).BSSID))
										.doubleValue() + 1));
					}
				}
			}
			HashMap<String, Double> map_avg = new HashMap<String, Double>();
			for (String key : map_sig.keySet()) {
				if (Double.valueOf(map_num.get(key)) >= 3)
					map_avg.put(
							key,
							Double.valueOf(map_sig.get(key).doubleValue()
									/ map_num.get(key).doubleValue()));
				// textStatus.append(key + ":" + map_avg.get(key).doubleValue()
				// +
				// " "
				// + map_num.get(key).doubleValue() + "\n");
			}

			HashMap<String, Double> map_var = new HashMap<String, Double>(); // variance
			for (int i = 0; i < 20; i++) {
				List<ScanResult> res = ScanList.get(i);
				for (int j = 0; j < res.size(); j++) {
					if (Double.valueOf(map_num.get(res.get(j).BSSID)
							.doubleValue()) >= 3) {
						if (!map_var.containsKey(res.get(j).BSSID)) {
							map_var.put(
									res.get(j).BSSID,
									(Double.valueOf(res.get(j).level) - Double
											.valueOf(map_avg.get(
													res.get(j).BSSID)
													.doubleValue()))
											* (Double.valueOf(res.get(j).level) - Double
													.valueOf(map_avg.get(
															res.get(j).BSSID)
															.doubleValue())));
						} else {
							map_var.put(
									res.get(j).BSSID,
									(Double.valueOf(
											map_var.get(res.get(j).BSSID))
											.doubleValue() + (Double
											.valueOf(res.get(j).level) - Double
											.valueOf(map_avg.get(
													res.get(j).BSSID)
													.doubleValue()))
											* (Double.valueOf(res.get(j).level) - Double
													.valueOf(map_avg.get(
															res.get(j).BSSID)
															.doubleValue()))));
						}
					}
				}
			}

			for (String key : map_var.keySet()) {
				if (Double.valueOf(map_var.get(key)) >= 3)
					map_var.put(
							key,
							Double.valueOf(map_var.get(key).doubleValue()
									/ map_num.get(key).doubleValue()));
				// textStatus.append(key + ":" + map_avg.get(key).doubleValue()
				// +
				// " "
				// + map_num.get(key).doubleValue() + "\n");
			}

			// create a xml formatted string
			String xml;
			String header = "<?xml version='1.0'?>\n";
			String session = "<session>\n <number>" + map_avg.size()
					+ "</number>\n" + "<coordinates>" + coordinate
					+ "</coordinates>\n" + "<room>" + room.toString()
					+ "</room>\n";
			String content = " <content>\n";
			for (String key : map_avg.keySet()) {
				content += "  <";
				content += "item";
				content += ">\n";
				content += "   <variance>";
				content += map_var.get(key);
				content += "</variance>\n";
				content += "   <num_of_values>";
				content += map_num.get(key);
				content += "</num_of_values>\n";
				content += "   <MAC>";
				content += key;
				content += "</MAC>\n";
				content += "   <SIG>";
				content += map_avg.get(key);
				content += "</SIG>\n";
				content += "  </";
				content += "item";
				content += ">\n";
			}
			content += " </content>\n</session>\n";
			xml = header + session + content;
			System.out.println(xml);

			/* get IMEI */
			// TelephonyManager telephonyManager =
			// (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			// textStatus.append(telephonyManager.getDeviceId());

			/* get Android ID */
			// String Id = Settings.Secure.getString(getContentResolver(),
			// Settings.Secure.ANDROID_ID);
			// textStatus.append(Id);

			ConnectivityManager mag = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = mag.getActiveNetworkInfo();
			if (info == null || !info.isConnected())
				Toast.makeText(context,
						"Warning: No Internet connection, please check...",
						Toast.LENGTH_LONG).show();
			else {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(
							"http://inpoint.pdp.fi/wlan/measurement.php");

					// send xml through http post
					StringEntity se = new StringEntity(xml, HTTP.UTF_8);
					se.setContentType("text/xml");
					httppost.setHeader("Content-Type",
							"application/soap+xml;charset=UTF-8");
					httppost.setEntity(se);
					
					BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient
							.execute(httppost);

					// read echo from server
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(httpResponse.getEntity()
									.getContent(), "UTF-8"));
					String json = reader.readLine();

					System.out.println("returned from server:" + json);
					Toast.makeText(context,
							"WiFi figerprint collected",
							Toast.LENGTH_LONG).show();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}