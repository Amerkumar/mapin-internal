package app.mapin.amerkumar.regionsdetection;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BlueDotClass {

    private Circle mCircle;
    private Marker mMarker;
    private GoogleMap mMap;
    private static BlueDotClass sBlueDotClass;

    public static BlueDotClass get(Context context, GoogleMap map){
        if (sBlueDotClass == null) {
            sBlueDotClass = new BlueDotClass(context, map);
        }
        return sBlueDotClass;
    }


    private BlueDotClass(Context context, GoogleMap map) {
        mMap = map;
    }

    public void showBlueDot(LatLng center, double accuracyRadius, double bearing) {
        if (mCircle == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mCircle = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(accuracyRadius)
                        .fillColor(0x201681FB)
                        .strokeColor(0x500A78DD)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                        .anchor(0.5f, 0.5f)
                        .rotation((float)bearing)
                        .flat(true));
            }
        } else {
            // move existing markers position to received location
            mCircle.setCenter(center);
            mCircle.setRadius(accuracyRadius);
            mMarker.setPosition(center);
            mMarker.setRotation((float)bearing);
        }
    }
}