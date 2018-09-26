package app.mapin.amerkumar.regionsdetection;

public class HaversineFunctions {

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

        LatLngLocation result = new LatLngLocation();
        result.setLatitude(Math.asin(Math.sin(latLngLocation.getLatitude() * Math.cos(latLngLocation.getDistance() / LatLngLocation.getEarthRadius())))
                        + Math.cos(latLngLocation.getLatitude()) * Math.sin(latLngLocation.getDistance() / LatLngLocation.getEarthRadius()) * Math.cos(latLngLocation.getBearing()));
        result.setLongitude(latLngLocation.getLongitude() + Math.atan2(Math.sin(latLngLocation.getBearing()) * Math.sin(latLngLocation.getDistance() / LatLngLocation.getEarthRadius())
                            * Math.cos(latLngLocation.getLatitude()), Math.cos(latLngLocation.getDistance()) - Math.sin(latLngLocation.getLatitude()) * Math.sin(result.getLatitude())));

        result.setLongitude((result.getLongitude() + 540) % 360 - 180);
        result.setBearing(latLngLocation.getBearing());
        result.setDistance(0);
        return latLngLocation;
    }
}
