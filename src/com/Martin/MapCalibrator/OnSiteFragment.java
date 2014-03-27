package com.Martin.MapCalibrator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.Martin.MapCalibrator.CoordinateList;
import com.Martin.MapCalibrator.CoordinateMapping;
import com.Martin.MapCalibrator.MapActivity;
import com.Martin.MapCalibrator.MyDrawableImageView;
import com.Martin.MapCalibrator.MyIndoorLocationListener;
import com.Martin.MapCalibrator.MyPreferencesActivity;
import com.Martin.MapCalibrator.R;
import com.Martin.MapCalibrator.UploadWifiFingerprint;
import com.Martin.MapCalibrator.misc.DBAdapter;
import com.Martin.MapCalibrator.mapSaveData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class OnSiteFragment extends Fragment {

	public static String MAP_FILE_PATH = "MAP_FILE_PATH";

	private SaveData m_SavableData;
	EditText locationIndex;

	private static final int DIALOG_INFORMATION_ID = 1;
	private static final int ACTIVITY_REQUEST_CODE_SELECT_PICTURE = 2;
	private MyIndoorLocationListener locationListener;

	private Button btn_position;

	private Button btn1;
	private Button btn2;
	private Button btn3;
	private Button btn4;
	private Button btn5;

	private DBAdapter database;
	private DBAdapter mDbHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Bundle bundle = this.getArguments();
		savedInstanceState = getArguments();
		View rootView = inflater.inflate(R.layout.main, container, false);

		initButton(rootView);
		initView(rootView, savedInstanceState);
		return rootView;
	}

	private void fillNewMap(int floor, View rootView) {
		mDbHelper = new DBAdapter(getActivity());
		mDbHelper.open();
		Cursor c = mDbHelper.getAllMaps();
		String lastUsedMap = null;
		long lastUsedMapKey = 0;
		int i = 0;
		if (c != null) {
			if (c.moveToFirst()) {
				// Now create an array adapter and set it to display using our
				// row
				do {
					if (i == floor) {
						lastUsedMap = c.getString(2);
						lastUsedMapKey = c.getLong(0);
						System.out.println(c.getString(2));
						System.out.println(c.getLong(0));
						break;
					}
					i++;
				} while (c.moveToNext());
			}
		} else {
			System.out.println("cursor is null");
		}

		mDbHelper.close();

		m_SavableData = new SaveData();
		// findViewById is only visible in Activity and View, therefore I
		// can not create it in my object
		m_SavableData.mapView = (MyDrawableImageView) rootView
				.findViewById(R.id.imageView);

		if (lastUsedMap.length() != 0) {
			File temp = new File(lastUsedMap);
			if (temp.exists()) {
				m_SavableData.mapFile = new File(lastUsedMap);
				m_SavableData.m_iMapKey = lastUsedMapKey;
				System.out.println("debuging......" + lastUsedMap);
				System.out.println("debuging......" + lastUsedMapKey);
				resetForNewMap();
			}
		}
	}

	private void popUpWindow() {
		Intent i = new Intent(getActivity(), PopupWindow.class);
		startActivity(i);
	}

	private void initButton(final View rootView) {
		btn_position = (Button) rootView.findViewById(R.id.buttonpos);
		btn1 = (Button) rootView.findViewById(R.id.button1);
		btn2 = (Button) rootView.findViewById(R.id.button2);
		btn3 = (Button) rootView.findViewById(R.id.button3);
		btn4 = (Button) rootView.findViewById(R.id.button4);
		btn5 = (Button) rootView.findViewById(R.id.button5);
		btn_position.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("position selected");
				popUpWindow();
			}
		});

		btn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("floor 1 selected");
				btn1.setBackgroundResource(R.drawable.btn_1_select);
				btn2.setBackgroundResource(R.drawable.btn_2_unselect);
				btn3.setBackgroundResource(R.drawable.btn_3_unselect);
				btn4.setBackgroundResource(R.drawable.btn_4_unselect);
				btn5.setBackgroundResource(R.drawable.btn_5_unselect);
				fillNewMap(0, rootView);
			}
		});
		btn2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("floor 2 selected");
				btn1.setBackgroundResource(R.drawable.btn_1_unselect);
				btn2.setBackgroundResource(R.drawable.btn_2_select);
				btn3.setBackgroundResource(R.drawable.btn_3_unselect);
				btn4.setBackgroundResource(R.drawable.btn_4_unselect);
				btn5.setBackgroundResource(R.drawable.btn_5_unselect);
				fillNewMap(1, rootView);
			}
		});
		btn3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("floor 3 selected");
				btn1.setBackgroundResource(R.drawable.btn_1_unselect);
				btn2.setBackgroundResource(R.drawable.btn_2_unselect);
				btn3.setBackgroundResource(R.drawable.btn_3_select);
				btn4.setBackgroundResource(R.drawable.btn_4_unselect);
				btn5.setBackgroundResource(R.drawable.btn_5_unselect);
				fillNewMap(2, rootView);
			}
		});
		btn4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("floor 4 selected");
				btn1.setBackgroundResource(R.drawable.btn_1_unselect);
				btn2.setBackgroundResource(R.drawable.btn_2_unselect);
				btn3.setBackgroundResource(R.drawable.btn_3_unselect);
				btn4.setBackgroundResource(R.drawable.btn_4_select);
				btn5.setBackgroundResource(R.drawable.btn_5_unselect);
				fillNewMap(3, rootView);
			}
		});
		btn5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("floor 5 selected");
				btn1.setBackgroundResource(R.drawable.btn_1_unselect);
				btn2.setBackgroundResource(R.drawable.btn_2_unselect);
				btn3.setBackgroundResource(R.drawable.btn_3_unselect);
				btn4.setBackgroundResource(R.drawable.btn_4_unselect);
				btn5.setBackgroundResource(R.drawable.btn_5_select);
				fillNewMap(4, rootView);
			}
		});

	}

	private void initView(View rootView, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		database = new DBAdapter(getActivity());
		// Create our folder if it is not already created
		if (!mPath.exists()) {
			try {
				mPath.mkdirs();
			} catch (SecurityException e) {
				// Log.e(TAG, "unable to write on the sd card " + e.toString());
			}
		}
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {

			m_SavableData = (SaveData) savedInstanceState
					.getParcelable("laststate");
			m_SavableData.mapView = (MyDrawableImageView) rootView
					.findViewById(R.id.imageView);
			mapSaveData mapSaveDataTemp = (mapSaveData) savedInstanceState
					.getParcelable("laststate_mapView");
			m_SavableData.mapView.setSaveableData(mapSaveDataTemp);
			// m_SavableData.mapView.setSaveableData(m_SavableData.mapViewSaveData);

			if (m_SavableData.m_bMapIsLoaded == true) {
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				Boolean supportLargeMaps = preferences.getBoolean(
						"supportLargeMaps", false);
				m_SavableData.mapView.setSupportLargeMaps(supportLargeMaps); // Must
																				// be
																				// called
																				// before
																				// setMap(
				m_SavableData.mapView.setMap(m_SavableData.mapFile);
			}

			if (m_SavableData.m_bIsTrackingPosition == true) {
				locationListener = new MyIndoorLocationListener(
						m_SavableData.mapView, getActivity());
				locationListener.startListening();
			}
		} else // Let's start from scratch
		{
			m_SavableData = new SaveData();
			// findViewById is only visible in Activity and View, therefore I
			// can not create it in my object
			m_SavableData.mapView = (MyDrawableImageView) rootView
					.findViewById(R.id.imageView);

			// Restore preferences
			SharedPreferences settings = (SharedPreferences) getActivity()
					.getSharedPreferences("MapCalibrator",
							getActivity().MODE_PRIVATE);
			int oldVersion = settings.getInt("version", 0);
			int currentVersion = 0;

			PackageInfo pInfo = null;
			try {
				pInfo = getActivity().getPackageManager().getPackageInfo(
						"com.Martin.MapCalibrator",
						PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
			}
			if (pInfo != null)
				currentVersion = pInfo.versionCode;

			if (currentVersion > oldVersion) {
				getActivity().showDialog(DIALOG_INFORMATION_ID);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("version", currentVersion);
				editor.commit();
			} else {
				// Toast.makeText(getActivity().getApplicationContext(),
				// "Use the menu button to see all available options.",
				// Toast.LENGTH_LONG).show();
			}

			// Get the last used map (if any)
			String lastUsedMap = settings.getString("lastUsedMap", "");

			if (lastUsedMap.length() != 0) {
				File temp = new File(lastUsedMap);
				if (temp.exists()) {
					m_SavableData.mapFile = new File(lastUsedMap);
					m_SavableData.m_iMapKey = settings.getLong(
							"lastUsedMapKey", 0);
					System.out.println("debuging......" + lastUsedMap);
					System.out.println("debuging......"
							+ settings.getLong("lastUsedMapKey", 0));
					resetForNewMap();
				} else {
					Toast.makeText(
							getActivity(),
							"Tried to load the previously used map, but it looks as if it does not exist anymore.",
							Toast.LENGTH_LONG).show();
				}
			} else {
				// Send them directly to the map list.
				// Intent intent = new Intent(this, MapActivity.class);
				// startActivityForResult(intent,
				// ACTIVITY_REQUEST_CODE_SELECT_MAP);
			}
		}

		// resetForNewMap();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//
		if (locationListener != null)
			locationListener.stopListening();
	}

	private void resetForNewMap() {
		System.out.println("reset for new map");
		m_SavableData.coordinateMappingList.clear();
		m_SavableData.m_bMapIsLoaded = true;
		m_SavableData.m_bIsCalibrating = false;

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		Boolean supportLargeMaps = preferences.getBoolean("supportLargeMaps",
				false);
		if (supportLargeMaps) {
			// Check so that it can be safely used.
			if (android.os.Build.VERSION.SDK_INT < 10)
				supportLargeMaps = false;
		}

		database.open();
		m_SavableData.m_iMapKey = database.getMapKey(m_SavableData.mapFile
				.getAbsolutePath());
		if (m_SavableData.m_iMapKey != -1) {
			if (database.mapHasReferencePoints(m_SavableData.m_iMapKey)) {
				database.close();
				loadAndUseOldReferencePoints();
			}
		} else {
			m_SavableData.m_iMapKey = database.insertMap(
					m_SavableData.mapFile.getName(),
					m_SavableData.mapFile.getAbsolutePath());
			database.close();
		}

		m_SavableData.mapView.setSupportLargeMaps(supportLargeMaps); // Must be
																		// called
																		// before
																		// setMap(
		m_SavableData.mapView.getSaveableData().m_iMapKey = m_SavableData.m_iMapKey;

		m_SavableData.mapView.setMap(m_SavableData.mapFile);

		// Save the last used map so that we can auto load it upon next run
		SharedPreferences settings = (SharedPreferences) getActivity()
				.getPreferences(getActivity().MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lastUsedMap", m_SavableData.mapFile.getAbsolutePath());
		editor.putLong("lastUsedMapKey", m_SavableData.m_iMapKey);
		editor.commit();

		if (locationListener == null) {
			m_SavableData.m_bIsTrackingPosition = true; // TODO:We really don't
			locationListener = new MyIndoorLocationListener(
					m_SavableData.mapView, getActivity());
		}
		locationListener.startListening();
	}

	private void loadAndUseOldReferencePoints() {
		if (m_SavableData.m_iMapKey != -1) {
			database.open();
			m_SavableData.coordinateMappingList = database
					.getAllReferencePointsForMap(m_SavableData.m_iMapKey);
			database.close();
			// tryToCalibrateMap();
		}
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// // Handle item selection
	// switch (item.getItemId()) {
	// case R.id.menu_maps: {
	// Intent intent = new Intent(getActivity(), MapActivity.class);
	// startActivityForResult(intent, ACTIVITY_REQUEST_CODE_SELECT_MAP);
	// return true;
	// }
	// }
	// }

	private String[] mFileList;
	private File mPath = new File(Environment.getExternalStorageDirectory()
			+ "//MapCalibrator//");

	class SaveData implements Parcelable {
		public SaveData() {
		}

		MyDrawableImageView mapView; // Always != null created in the Activity
		mapSaveData mapViewSaveData; // Used to restore the mapView after
										// pausing
		ArrayList<CoordinateMapping> coordinateMappingList = new ArrayList<CoordinateMapping>();
		File mapFile;
		boolean m_bMapIsLoaded = false;
		boolean m_bIsCalibrating = false;
		boolean m_bIsTrackingPosition = true;
		long m_iMapKey = -1; // Key in the database for the current map.

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			CoordinateMapping[] temp = new CoordinateMapping[coordinateMappingList
					.size()];
			temp = coordinateMappingList.toArray(temp);
			out.writeParcelableArray(temp, flags);

			out.writeString(mapFile == null ? null : mapFile.toString());
			out.writeBooleanArray(new boolean[] { m_bMapIsLoaded,
					m_bIsCalibrating, m_bIsTrackingPosition });

			out.writeLong(m_iMapKey);
			mapView.getSaveableData().m_iMapKey = m_iMapKey;
			out.writeParcelable(mapView.getSaveableData(), flags);
		}

		public final Parcelable.Creator<SaveData> CREATOR = new Parcelable.Creator<SaveData>() {
			public SaveData createFromParcel(Parcel in) {
				return new SaveData(in);
			}

			public SaveData[] newArray(int size) {
				return new SaveData[size];
			}
		};

		private SaveData(Parcel in) {
			// In Java it is illegal to cast an array of a supertype into a
			// subtype.
			// That will throw an
			// java.lang.ClassCastException
			Parcelable[] temp;
			temp = in.readParcelableArray(CoordinateMapping.class
					.getClassLoader());
			for (int i = 0; i < temp.length; i++)
				this.coordinateMappingList.add((CoordinateMapping) temp[i]);

			String tempString = in.readString();
			if (tempString != null) {
				this.mapFile = new File(tempString);
			}

			boolean bTemp[] = new boolean[3];
			in.readBooleanArray(bTemp);
			this.m_bMapIsLoaded = bTemp[0];
			this.m_bIsCalibrating = bTemp[1];
			this.m_bIsTrackingPosition = bTemp[2];

			this.m_iMapKey = in.readLong();

			mapViewSaveData = in.readParcelable(mapSaveData.class
					.getClassLoader());
		}
	}
}
