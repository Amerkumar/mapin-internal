package app.mapin.amerkumar.regionsdetection;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceTransitionsIntentService extends IntentService {


    private static final String TAG = "GeofenceTransService";
    private static final String CHANNEL_ID = "GeoFenceNotification";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotifyManager;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
//                String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                        geofencingEvent.getErrorCode());
                Log.e(TAG, geofencingEvent.getErrorCode() + "");
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
// Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );
                Log.d(TAG, intent.getClass().getSimpleName());
                // Send notification and log the transition details.
//                sendNotification(geofenceTransitionDetails);
                Log.d(TAG, geofenceTransitionDetails);
                sendNotification("Transition Enter", "Entered in geofence -- here" +
                        "floor change function should be called");

            } else if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                sendNotification("Transition Exit", "Exit in geofence -- here" +
                        "floor should be removed.");

            }
            else {
                // Log the error.
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        String.valueOf(geofenceTransition)));

            }
        }
    }
    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Mascot Notification", NotificationManager
                    .IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private void sendNotification(String title, String content){
        Intent notificationIntent = new Intent(this, RegionsActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.android_icon)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

    }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Masoct Notification",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.enableVibration(true);
//            notificationChannel.setDescription("Notification from Mascot");
//            mNotificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                                                .setSmallIcon(R.drawable.android_icon)
//                                                .setContentTitle(title)
//                                                .setContentText(content);
//
//        // Create an explicit intent for an Activity in your app
//        Intent contentIntent = new Intent(this, RegionsActivity.class);
//        PendingIntent pendingContentIntent = PendingIntent.getService(this, 0,
//                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//// Set the intent that will fire when the user taps the notification
//        mBuilder.setContentIntent(pendingContentIntent);
//        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setDefaults(NotificationCompat.PRIORITY_HIGH);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//    }

    private String getGeofenceTransitionDetails(GeofenceTransitionsIntentService geofenceTransitionsIntentService,
                                                int geofenceTransition, List<Geofence> triggeringGeofences) {
            return triggeringGeofences.get(0).getRequestId();
    }


}
