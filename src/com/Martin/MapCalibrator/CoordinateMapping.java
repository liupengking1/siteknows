package com.Martin.MapCalibrator;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

public class CoordinateMapping implements Parcelable{
	public PointF mapCoordinate;
	public int WifiCoordinateIndex; // This is the final gps coordinate after possible change and verification by the user
	
	public int describeContents() {
        return 0;
    }

	public void writeToParcel(Parcel out, int flags) {
        out.writeFloatArray(new float[]{mapCoordinate.x, mapCoordinate.y});
//        out.writeFloatArray(new float[]{gpsCoordinate.x, gpsCoordinate.x});
        out.writeInt(WifiCoordinateIndex);
    }
	
	public static final Parcelable.Creator<CoordinateMapping> CREATOR
		= new Parcelable.Creator<CoordinateMapping>() {
		public CoordinateMapping createFromParcel(Parcel in) {
			return new CoordinateMapping(in);
		}

		public CoordinateMapping[] newArray(int size) {
			return new CoordinateMapping[size];
		}
	};

	private CoordinateMapping(Parcel in) {
		float[] fTemp = new float[2];
		in.readFloatArray(fTemp);
		this.mapCoordinate = new PointF(fTemp[0], fTemp[1]);
		
		
		this.WifiCoordinateIndex = in.readInt();
	}

	
	public CoordinateMapping(PointF mapCoordinate, int location) {
		this.mapCoordinate = mapCoordinate;		
		this.WifiCoordinateIndex = location;
	}	
}