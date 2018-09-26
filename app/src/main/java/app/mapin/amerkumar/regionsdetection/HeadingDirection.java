package app.mapin.amerkumar.regionsdetection;

import android.hardware.SensorManager;

public class HeadingDirection {


    private static float[] mOrientationAngles = new float[3];


    public static void updateOrientationAngles(float[] accelerometerData, float[] magnetometerData) {
        float[] rotationMatrix = new float[9];
        boolean rotationOk = SensorManager.getRotationMatrix(rotationMatrix,
                null, accelerometerData, magnetometerData);
        if (rotationOk) {
            SensorManager.getOrientation(rotationMatrix, mOrientationAngles);
        }
    }

    public static float getHeadingInDegrees() {
        double azimuth = mOrientationAngles[0];
//        convert range from pi to -pi to 2pi
        azimuth = azimuth + Math.PI;
//        now convert azimuth in degrees
        return 0;
    }
}
