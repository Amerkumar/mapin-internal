package app.mapin.amerkumar.regionsdetection;

public class LatLngLocation {



  private double mLatitude;
  private double mLongitude;
  private double mBearing;
  private double mDistance;
  private static final double EARTH_RADIUS = 6371;

  public static double getEarthRadius() {
    return EARTH_RADIUS;
  }

  public double getBearing() {
    return mBearing;
  }

  public void setBearing(double bearing) {
    mBearing = bearing;
  }

  public double getDistance() {
    return mDistance;
  }

  public void setDistance(double distance) {
    mDistance = distance;
  }

  public double getLatitude() {
    return mLatitude;
  }

  public void setLatitude(double latitude) {
    mLatitude = latitude;
  }


  public double getLongitude() {
    return mLongitude;
  }

  public void setLongitude(double longitude) {
    mLongitude = longitude;
  }
}
