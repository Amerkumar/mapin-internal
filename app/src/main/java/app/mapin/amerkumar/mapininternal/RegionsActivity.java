package app.mapin.amerkumar.mapininternal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class RegionsActivity extends AppCompatActivity implements OnMapReadyCallback,
        SensorEventListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener
{

    private static final String TAG = "ap.mapin.RegionActivity";
    private static final int REQUEST_CHECK_SETTINGS = 0;
    private static final int MAX_DIMENSION = 2048;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int LOCATION_PERMISSIONS_REQUEST = 0;

    private LocationCallback mLocationCallback;
    private GoogleMap mMap;
    private GroundOverlay mGroundOverlay;
    private Target mLoadTarget;
    private IARegion mOverlayFloorPlan;
    private IAResourceManager mResourceManager;
    private IATask<IAFloorPlan> mFetchFloorPlanTask;
    private String newId = "bf5292d6-d12a-44a7-b9e3-ee6de6f10ffc";

    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private Sensor mAccelerometerSensor;
    private Sensor mMagnetometerSensor;
    private Sensor mRotationVectorSensor;

    //    we want step detection as soon as possible
    private final int STEP_DETECTOR_SAMPLING_PERIOD = 0;
    // 20,000 micro seconds = 20 milli seconds = 50 hz
    private final int OTHER_SENSOR_SAMPLING_PERIOD = 20000;

    private final float STEP_LENGTH = 0.000762f;
    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[9];
    private Location mGPSLocation;
    private LatLng mCenter;
    private Snackbar mWaypointSnackbar;


    private Switch mSwitchInertialNavigation;
    private boolean mIsIntialLocationGiven = false;
    private LatLngLocation mIntialLatLng = new LatLngLocation();
    private BlueDotClass mBlueDotClass;
    private GeomagneticField mGeomagneticField;
    private ImageButton mAddWaypoint;
    private List<WaypointMarkers> mWayPointList = new ArrayList<>();



    /**
     * The sensors used by the compass are mounted in the movable arm on Glass. Depending on how
     * this arm is rotated, it may produce a displacement ranging anywhere from 0 to about 12
     * degrees. Since there is no way to know exactly how far the arm is rotated, we just split the
     * difference.
     */
    private static final int ARM_DISPLACEMENT_DEGREES = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_maps);

        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationPermissions();
        requestGPSPermissions();
        getLocationUpdates();
        mapsInit();
        mResourceManager = IAResourceManager.create(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



    }

    private void initWayPointMarkers() {
        mAddWaypoint = (ImageButton) findViewById(R.id.add_waypoint);
        final Bitmap bitmap = WaypointMarkers.getBitmapFromVectorDrawable(this, R.drawable.waypoint_shape);
        mAddWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(mCenter)
                                    .draggable(true)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .flat(true));

                mWayPointList.add(new WaypointMarkers(marker.getId(), marker));
            }
        });
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        fetchFloorPlan(newId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_maps, menu);
        MenuItem item = (MenuItem) menu.findItem(R.id.switch_item);
        item.setActionView(R.layout.switch_layout);
        mSwitchInertialNavigation = item
                .getActionView().findViewById(R.id.switch_inertial_nav);
        mSwitchInertialNavigation.setChecked(false);
        mSwitchInertialNavigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mIsIntialLocationGiven)
                        registerInertialNavigation();
                    else{
                        showInfo("Please Long Click on Map to select intial position.");
                        buttonView.setChecked(false);
                    }

                } else {
                    unregisterInertialNavigation();
                }
            }
        });
        return true;
    }


    private void toastMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void mapsInit() {
        Log.d(TAG, "Maps init");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    private void requestGPSPermissions() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        RegionsActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            }
        });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        ActivityCompat.requestPermissions(RegionsActivity.this,
                                LOCATION_PERMISSIONS,
                                LOCATION_PERMISSIONS_REQUEST);
                    }
                });
                builder.create().show();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    Log.d(TAG, "Permissions granted");

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void getLocationUpdates() {
        Log.d(TAG, "Get Location");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "Get Location");
                if (locationResult == null)
                    return;
                Log.d(TAG, "size " + locationResult.getLocations().size() + "");
                mGPSLocation = locationResult.getLocations().get(0);
            }
        };

    }

    private void registerInertialNavigation() {
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (mStepDetectorSensor != null) {
            mSensorManager.registerListener(this, mStepDetectorSensor,
                    STEP_DETECTOR_SAMPLING_PERIOD);
        }
//
//        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        if (mAccelerometerSensor != null) {
//            mSensorManager.registerListener(this, mAccelerometerSensor,
//                    OTHER_SENSOR_SAMPLING_PERIOD);
//        }
//
//        mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        if (mMagnetometerSensor != null) {
//            mSensorManager.registerListener(this, mMagnetometerSensor,
//                    OTHER_SENSOR_SAMPLING_PERIOD);
//        }
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (mRotationVectorSensor != null) {
            mSensorManager.registerListener(this, mRotationVectorSensor,
                    OTHER_SENSOR_SAMPLING_PERIOD);
        }

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(createLocationRequest(),
                mLocationCallback, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopLocationUpdates();
    }

    private void unregisterInertialNavigation() {
        mSensorManager.unregisterListener(this);
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "On Map ready callback");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mMap.setMyLocationEnabled(false);
        mBlueDotClass = BlueDotClass.get(this, mMap);
        fetchFloorPlan(newId);
//        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//             @Override
//             public void onMapLongClick(LatLng latLng) {
//                 if (mIsIntialLocationGiven) {
//                    toastMessage("Intial Location Already Given");
//                 }
//                 else {
//                     mIntialLatLng.setLatitude(latLng.latitude);
//                     mIntialLatLng.setLongitude(latLng.longitude);
//                     mBlueDotClass.showBlueDot(latLng, 0, 0);
//                     mIsIntialLocationGiven = true;
//                 }
//             }
//         });
        initWayPointMarkers();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mWaypointSnackbar.dismiss();
            }
        });
    }

    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap) {

        if (mGroundOverlay != null) {
            mGroundOverlay.remove();
        }

        if (mMap != null) {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            mCenter = center;
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .zIndex(0.0f)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());

            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);
            LatLng incubator = new LatLng(34.071714, 72.645690);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(incubator, 20), 2000, null);
        }
    }

    /**
     * Download floor plan using Picasso library.
     */
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan) {

        final String url = floorPlan.getUrl();

        if (mLoadTarget == null) {
            mLoadTarget = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x"
                            + bitmap.getHeight());
                    setupGroundOverlay(floorPlan, bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // N/A
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable placeHolderDraweble) {
                    showInfo("Failed to load bitmap");
                    mOverlayFloorPlan = null;
                }
            };
        }

        RequestCreator request = Picasso.get().load(url);

        final int bitmapWidth = floorPlan.getBitmapWidth();
        final int bitmapHeight = floorPlan.getBitmapHeight();

        if (bitmapHeight > MAX_DIMENSION) {
            request.resize(0, MAX_DIMENSION);
        } else if (bitmapWidth > MAX_DIMENSION) {
            request.resize(MAX_DIMENSION, 0);
        }

        request.into(mLoadTarget);
    }


    /**
     * Fetches floor plan data from IndoorAtlas server.
     */
    private void fetchFloorPlan(String id) {

        // if there is already running task, cancel it
        cancelPendingNetworkCalls();

        final IATask<IAFloorPlan> task = mResourceManager.fetchFloorPlanWithId(id);

        task.setCallback(new IAResultCallback<IAFloorPlan>() {

            @Override
            public void onResult(IAResult<IAFloorPlan> result) {

                if (result.isSuccess() && result.getResult() != null) {
                    // retrieve bitmap for this floor plan metadata
                    Log.d(TAG, "Successfully found floor");

                    fetchFloorPlanBitmap(result.getResult());
                } else {
                    // ignore errors if this task was already canceled
                    if (!task.isCancelled()) {
                        // do something with error
                        Log.d(TAG, result.getResult() + "");
                        showInfo("Loading floor plan failed: " + result.getError());
                        mOverlayFloorPlan = null;
                    }
                }
            }
        }, Looper.getMainLooper()); // deliver callbacks using main looper

        // keep reference to task so that it can be canceled if needed
        mFetchFloorPlanTask = task;

    }

    /**
     * Helper method to cancel current task if any.
     */
    private void cancelPendingNetworkCalls() {
        if (mFetchFloorPlanTask != null && !mFetchFloorPlanTask.isCancelled()) {
            mFetchFloorPlanTask.cancel();
        }
    }

    private void showInfo(String text) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.button_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_STEP_DETECTOR:
                stepModel();
                break;
//            case Sensor.TYPE_ACCELEROMETER:
//                mAccelerometerData = event.values.clone();
//                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                mMagnetometerData = event.values.clone();
//                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                // Get the current heading from the sensor, then notify the listeners of the
                // change.
                mSensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                mSensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Y, mRotationMatrix);
                mSensorManager.getOrientation(mRotationMatrix, mOrientation);
                // Convert the heading (which is relative to magnetic north) to one that is
                // relative to true north, using the user's current location to compute this.
                float magneticHeading = (float) Math.toDegrees(mOrientation[0]);
                float heading = HeadingDirection.mod(computeTrueNorth(magneticHeading), 360.0f)
                        - ARM_DISPLACEMENT_DEGREES;
                HeadingDirection.setHeadingInDegrees(heading);
//                updateGeomagneticField();
                break;
        }
//        HeadingDirection.updateOrientationAngles(mAccelerometerData, mMagnetometerData);
    }

    private void stepModel() {
        int offset = 0;
        double heading = HeadingDirection.getHeading();
        LatLngLocation latLngLocation = new LatLngLocation();
        latLngLocation.setLatitude(mIntialLatLng.getLatitude());
        latLngLocation.setLongitude(mIntialLatLng.getLongitude());
        latLngLocation.setBearing(heading - offset);
        Log.d(TAG, heading + "");
        latLngLocation.setDistance(STEP_LENGTH);
        mIntialLatLng = HaversineFunctions.latLngBearingToFinalLatLng(latLngLocation);
        LatLng center = new LatLng(mIntialLatLng.getLatitude(), mIntialLatLng.getLongitude());
        mBlueDotClass.showBlueDot(center, 1,  heading - offset);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Use the magnetic field to compute true (geographic) north from the specified heading
     * relative to magnetic north.
     *
     * @param heading the heading (in degrees) relative to magnetic north
     * @return the heading (in degrees) relative to true north
     */
    private float computeTrueNorth(float heading) {
        if (mGeomagneticField != null) {
            return heading + mGeomagneticField.getDeclination();
        } else {
            return heading;
        }
    }

    /**
     * Updates the cached instance of the geomagnetic field after a location change.
     */
    private void updateGeomagneticField() {
        mGeomagneticField = new GeomagneticField((float) mGPSLocation.getLatitude(),
                (float) mGPSLocation.getLongitude(), (float) mGPSLocation.getAltitude(),
                mGPSLocation.getTime());
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        mIntialLatLng.setLatitude(marker.getPosition().latitude);
        mIntialLatLng.setLongitude(marker.getPosition().longitude);
        mBlueDotClass.showBlueDot(marker.getPosition(), 0, 0);
        mIsIntialLocationGiven = true;
        return mIsIntialLocationGiven;
    }

    @Override
    public void onMarkerDragStart(final Marker marker) {
        mWaypointSnackbar = Snackbar.make(findViewById(android.R.id.content), "Remove marker",
                Snackbar.LENGTH_INDEFINITE);
        mWaypointSnackbar.setAction(R.string.remove, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marker.remove();
            }
        });
        mWaypointSnackbar.show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        for (WaypointMarkers waypointMarkers : mWayPointList) {
            if (waypointMarkers.getMarker().equals(marker)) {
                Log.d(TAG, "updated marker position");
                waypointMarkers.setMarker(marker);
            }
        }
    }
}
