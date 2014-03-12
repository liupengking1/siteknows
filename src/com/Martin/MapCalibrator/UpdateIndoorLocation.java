package com.Martin.MapCalibrator;

import java.util.HashMap;
import java.util.List;

import java.io.*;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import java.util.ArrayList;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

public class UpdateIndoorLocation {

	private IndoorLocationListener mTheListener;
	Context context;
	private static final int SCAN_ROUND = 5;
	// public static final String userID =
	// android.provider.Settings.Secure.ANDROID_ID;
	private static WifiManager wifi;
	private static NetworkInfo info;

	public static boolean should_scan=false;

	public void setTheListener(IndoorLocationListener listen, Context context) {
		mTheListener = listen;
		this.context = context;

		// Setup WiFi
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager mag = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		info = mag.getActiveNetworkInfo();

		CreateUpdateThread();
	}

	private void reportPositionChanged(int value) {
		if (mTheListener != null) {
			mTheListener.locationupdated(value);
		}
	}
	
	private void CreateUpdateThread() {
		should_scan = true;
		
		Thread scan_thread = new Thread() {
			@Override
			public void run() {
				int count=1;
				while (should_scan) {
					System.out.println("scanning in background");
//					wifi.startScan();
					try {
						Thread.sleep(700);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (true) {
						// for debugging
						System.out.println("wifi scanning debugging, count:"+count);
						reportPositionChanged(count);
						count++;
					} else {

						ArrayList<List<ScanResult>> ScanList = new ArrayList<List<ScanResult>>(
								SCAN_ROUND);
						for (int scancount = 0; scancount < SCAN_ROUND; scancount++) {
							wifi.startScan();
							try {
								Thread.sleep(700);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							List<ScanResult> results = wifi.getScanResults();
							ScanList.add(results);
						}

						HashMap<String, Double> map_sig = new HashMap<String, Double>();
						HashMap<String, Double> map_num = new HashMap<String, Double>();
						// TODO Compare 5 scan results and Calculate an
						// average value
						// List<ScanResult> average;
						for (int i = 0; i < SCAN_ROUND; i++) {
							List<ScanResult> res = ScanList.get(i);
							for (int j = 0; j < res.size(); j++) {
								if (!map_sig.containsKey(res.get(j).BSSID)) {
									map_sig.put(res.get(j).BSSID,
											Double.valueOf(res.get(j).level));
									map_num.put(res.get(j).BSSID,
											Double.valueOf(1));
								} else {
									map_sig.put(
											res.get(j).BSSID,
											(Double.valueOf(
													map_sig.get(res.get(j).BSSID))
													.doubleValue() + res.get(j).level));
									map_num.put(
											res.get(j).BSSID,
											(Double.valueOf(
													map_num.get(res.get(j).BSSID))
													.doubleValue() + 1));
								}
							}
						}
						HashMap<String, Double> map_avg = new HashMap<String, Double>();
						for (String key : map_sig.keySet()) {
							// if more than half scan times can the AP be seen
							if (Double.valueOf(map_num.get(key)) >= SCAN_ROUND * 0.5)
								map_avg.put(key, Double.valueOf(map_sig
										.get(key).doubleValue()
										/ map_num.get(key).doubleValue()));
							// textStatus.append(key + ":" +
							// map_avg.get(key).doubleValue()
							// +
							// " "
							// + map_num.get(key).doubleValue() + "\n");
						}

						// obtain device mac address

						String address = wifi.getConnectionInfo()
								.getMacAddress();

						// create a xml formatted string
						String xml;
						String header = "<?xml version='1.0'?>\n";
						String session = "<session>\n <number>"
								+ map_avg.size() + "</number>\n";
						String deviceMac = " <own_mac>" + address
								+ "</own_mac>\n";
						String content = " <content>\n";
						for (String key : map_avg.keySet()) {
							content += "  <";
							content += "item";
							content += ">\n";
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
						xml = header + session + deviceMac + content;

						// send msg to main thread to update UI
						// Message msg = mainHandler.obtainMessage();
						// msg.obj = xml;
						// mainHandler.sendMessage(msg);

						if (checkNetworkCondition()) {
							try {
								HttpClient httpclient = new DefaultHttpClient();
								HttpPost httppost = new HttpPost(
										"http://inpoint.pdp.fi/wlan/wlan_relative.php");

								// send xml through http post
								StringEntity se = new StringEntity(xml,
										HTTP.UTF_8);
								se.setContentType("text/xml");
								httppost.setHeader("Content-Type",
										"application/soap+xml;charset=UTF-8");
								httppost.setEntity(se);

								BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient
										.execute(httppost);

								// read echo from server
								BufferedReader reader = new BufferedReader(
										new InputStreamReader(httpResponse
												.getEntity().getContent(),
												"UTF-8"));
								String json = reader.readLine();
								System.out.println("Response from server:"
										+ json);

								reportPositionChanged(readPositionFromServer(json));

							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}
				}

			}
		};
		scan_thread.start();
	}

	boolean checkNetworkCondition() {
		if (!wifi.isWifiEnabled()) {
			System.out
					.println("WiFi is not open on this device, Please enable it and restart the app!");
			return false;
		} else if (info == null || !info.isConnected()) {
			System.out
					.println("No internet connection on this device, please connect via 3G or open WiFi AP and restart the app");
			return false;
		}
		return true;

	}

	public void stopScanningThread() {
		System.out.println("stopScanning Thread");
//		should_scan = false;
	}

	public void startScanningThread() {
		System.out.println("updateIndoorlocation: start scanning thread");
//		if (should_scan = false)
			CreateUpdateThread();
	}

	public int readPositionFromServer(String src) {
		if (src == null)
			return 0;
		String temp[] = src.split(",|\\_|\\ ");

		try {
			return Integer.parseInt(temp[0]);
		} catch (NumberFormatException e) {
			e.toString();
		}

		return 0;

	}

}