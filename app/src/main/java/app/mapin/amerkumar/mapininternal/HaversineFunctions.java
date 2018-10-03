package app.mapin.amerkumar.mapininternal;

import android.util.Log;

public class HaversineFunctions {

  private static final String TAG = HaversineFunctions.class.getSimpleName();
//
//    Formula:	φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
//    λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )
//    where	φ is latitude, λ is longitude, θ is the bearing (clockwise from north), δ is the angular distance d/R; d being the distance travelled, R the earth’s radius
//    JavaScript:
//            (all angles
//    in radians)
//    var φ2 = Math.asin( Math.sin(φ1)*Math.cos(d/R) +
//            Math.cos(φ1)*Math.sin(d/R)*Math.cos(brng) );
//    var λ2 = λ1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(φ1),
//            Math.cos(d/R)-Math.sin(φ1)*Math.sin(φ2));
// The longitude can be normalised to −180…+180 using (lon+540)%360-180

  //    Inputs : LatLng , effectively final Bearing (in radians) and distance
//    Output : New LatLng
  public static LatLngLocation latLngBearingToFinalLatLng(LatLngLocation latLngLocation) {

    double distance = latLngLocation.getDistance() / LatLngLocation.getEarthRadius();
    double bearing = Math.toRadians(latLngLocation.getBearing());
    double latitude = Math.toRadians(latLngLocation.getLatitude());
    double longitude = Math.toRadians(latLngLocation.getLongitude());

    LatLngLocation result = new LatLngLocation();

    double sinLatitude1 = Math.sin(latitude);
    double cosLatitude1 = Math.cos(latitude);
    double sinDistance = Math.sin(distance);
    double cosDistance = Math.cos(distance);
    double sinBearing = Math.sin(bearing);
    double cosBearing = Math.cos(bearing);

    double sinLatitude2 = sinLatitude1 * cosDistance + cosLatitude1 * sinDistance * cosBearing;
    double latitude2 = Math.asin(sinLatitude2);
    double y = sinBearing * sinDistance * cosLatitude1;
    double x = cosDistance - sinLatitude1 * sinLatitude2;

    double longitude2 = longitude + Math.atan2(y, x);

    result.setLatitude((Math.toDegrees(latitude2) + 540) % 360 - 180);
    result.setLongitude(Math.toDegrees(longitude2));
    Log.d(TAG, result.getLongitude() + "");
    Log.d(TAG, result.getLatitude() + "" );

    return result;
  }
}
