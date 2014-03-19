package com.Martin.MapCalibrator;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class mapSaveData implements Parcelable {
	int mode = 0;
	boolean isTrackingPosition; // true if we are marking the GPS on the map
	boolean isCalibrating; // true if we are calibrating now

	RectF m_PositionMapPosition; // position on the map/image

	Rect m_CalibrationPositionOnDisplay; // position on the screen. Used for
											// finger press detection
	RectF m_calibrationPositionOnGlobalMap; // position on the map/image

	int lastLocation; // To be able to draw out position if we don't get a new
						// location after the map is set.
	public long m_iMapKey;
	// These matrices will be used to move and zoom the current part of the
	// image
	Matrix currentMapTileToDisplayMatrix = new Matrix();
	Matrix savedMapTileToDisplayMatrix = new Matrix();

	// These matrices are used to map the complete map to the display (as
	// opposed to the ones above)
	Matrix currentGlobalMapToDisplayMatrix = new Matrix();
	Matrix savedGlobalMapToDisplayMatrix = new Matrix();

	// Used for converting gps coordinates to map coordinates
	// Matrix gpsToMapMapMatrix = new Matrix(); //Needs to be something when we
	// parcel it.

	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<mapSaveData> CREATOR = new Parcelable.Creator<mapSaveData>() {
		public mapSaveData createFromParcel(Parcel in) {
			return new mapSaveData(in);
		}

		public mapSaveData[] newArray(int size) {
			return new mapSaveData[size];
		}
	};

	// Anropa den här metoden från MapCalibrator istället för att göra det
	// här objektet Parcelable
	// Detta eftersom jag inte vet hur jag skulle skapa det innifrån sig
	// själv. Konstruktorn kräver parametrar.
	// Måste lägga till bilden också. Eller en referens till den, filnamnet
	// kanske.
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mode);
		out.writeBooleanArray(new boolean[] { isTrackingPosition, isCalibrating });
		if (isTrackingPosition)
			out.writeParcelable(m_PositionMapPosition, flags);
		if (isCalibrating) {
			out.writeParcelable(m_CalibrationPositionOnDisplay, flags);
			out.writeParcelable(m_calibrationPositionOnGlobalMap, flags);
		}
		out.writeInt(lastLocation);
		out.writeLong(m_iMapKey);

		float[] temp = new float[9];
		currentMapTileToDisplayMatrix.getValues(temp);
		out.writeFloatArray(temp);

		temp = new float[9];
		savedMapTileToDisplayMatrix.getValues(temp);
		out.writeFloatArray(temp);

		temp = new float[9];
		currentGlobalMapToDisplayMatrix.getValues(temp);
		out.writeFloatArray(temp);

		temp = new float[9];
		savedGlobalMapToDisplayMatrix.getValues(temp);
		out.writeFloatArray(temp);

	}

	private mapSaveData(Parcel in) {
		this.mode = in.readInt();
		boolean bTemp[] = new boolean[2];
		in.readBooleanArray(bTemp);
		this.isTrackingPosition = bTemp[0];
		this.isCalibrating = bTemp[1];

		if (isTrackingPosition)
			m_PositionMapPosition = in.readParcelable(Drawable.class
					.getClassLoader());
		if (isCalibrating) {
			m_CalibrationPositionOnDisplay = in.readParcelable(Rect.class
					.getClassLoader());
			m_calibrationPositionOnGlobalMap = in.readParcelable(RectF.class
					.getClassLoader());
		}

		lastLocation = in.readInt();
		m_iMapKey = in.readLong();

		float[] temp = new float[9];
		in.readFloatArray(temp);
		currentMapTileToDisplayMatrix.setValues(temp);

		in.readFloatArray(temp);
		savedMapTileToDisplayMatrix.setValues(temp);

		in.readFloatArray(temp);
		currentGlobalMapToDisplayMatrix.setValues(temp);

		in.readFloatArray(temp);
		savedGlobalMapToDisplayMatrix.setValues(temp);

	}

	public mapSaveData() {
	}

}