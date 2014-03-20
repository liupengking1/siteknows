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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OnSiteFragment extends Fragment {

	public static String MAP_FILE_PATH = "MAP_FILE_PATH";

	private SaveData m_SavableData;
	EditText locationIndex;

	private static final int DIALOG_INFORMATION_ID = 1;
	private static final int ACTIVITY_REQUEST_CODE_SELECT_PICTURE = 2;
	private MyIndoorLocationListener locationListener;

	private DBAdapter database;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Bundle bundle = this.getArguments();
		savedInstanceState = getArguments();
		View rootView = inflater.inflate(R.layout.main, container, false);
		initView(rootView, savedInstanceState);
		return rootView;
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

			System.out.println("debuging......" + lastUsedMap);
			if (lastUsedMap.length() != 0) {
				File temp = new File(lastUsedMap);
				if (temp.exists()) {
					m_SavableData.mapFile = new File(lastUsedMap);
					m_SavableData.m_iMapKey = settings.getLong(
							"lastUsedMapKey", 0);
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

	private void newFromFile() {
		// showDialog(DIALOG_LOAD_FILE);
		Intent fileIntent = new Intent();
		fileIntent.setType("image/*");
		fileIntent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
				Intent.createChooser(fileIntent, "Select a Map"),
				ACTIVITY_REQUEST_CODE_SELECT_PICTURE);
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

	private void loadFileList() {
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			// Log.e(TAG, "unable to write on the sd card " + e.toString());
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					// File sel = new File(dir, filename);
					return filename.endsWith(".png")
							|| filename.endsWith(".jpg")
							|| filename.endsWith(".jpeg");// ||
															// sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
		}
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// if (requestCode == ACTIVITY_REQUEST_CODE_TAKE_PICTURE
	// && resultCode == Activity.RESULT_OK) {
	// resetForNewMap();
	// } else if (requestCode == ACTIVITY_REQUEST_CODE_SELECT_PICTURE
	// && resultCode == Activity.RESULT_OK) {
	// Uri selectedImage = data.getData();
	//
	// // Uri:s should be of type "content://" according to the
	// // documentation for ACTION_GET_CONTENT
	// // The Astro file manager returns an Uri of Type "file://" so we
	// // must handle that as well.
	// if (selectedImage.toString().toLowerCase().startsWith("content://")) {
	// String[] filePathColumn = { MediaStore.Images.Media.DATA };
	// Cursor cursor = getActivity().getContentResolver().query(
	// selectedImage, filePathColumn, null, null, null);
	// if (cursor != null) {
	// cursor.moveToFirst();
	//
	// int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	// String filePath = cursor.getString(columnIndex); // file
	// // path
	// // of
	// // selected
	// // image
	// cursor.close();
	//
	// m_SavableData.mapFile = new File(filePath);
	//
	// resetForNewMap();
	// }
	// } else if (selectedImage.toString().toLowerCase()
	// .startsWith("file://")) {
	// // Handle stuff from the ASTRO file manager
	// String filePath = selectedImage.getPath();
	// m_SavableData.mapFile = new File(filePath);
	// resetForNewMap();
	// } else {
	// // TODO: Well, what do we do?
	// // Show a dialog perhaps?
	// }
	// } else if (requestCode == ACTIVITY_REQUEST_CODE_SELECT_MAP
	// && resultCode == Activity.RESULT_OK) {
	// String filePath = data.getExtras().getString(MAP_FILE_PATH);
	// m_SavableData.mapFile = new File(filePath);
	// resetForNewMap();
	// }
	// }
	// }

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
