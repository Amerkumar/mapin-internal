package app.mapin.amerkumar.regionsdetection;

import android.hardware.SensorManager;

public class HeadingDirection {


    private static float[] mOrientationAngles = new float[3];
    private static double heading;


    public static void updateOrientationAngles(float[] accelerometerData, float[] magnetometerData) {
        float[] rotationMatrix = new float[9];
        boolean rotationOk = SensorManager.getRotationMatrix(rotationMatrix,
                null, accelerometerData, magnetometerData);
        if (rotationOk) {
            SensorManager.getOrientation(rotationMatrix, mOrientationAngles);
        }

    }

    public static double getHeadingInDegrees() {
        double azimuth = mOrientationAngles[0];
        return Math.toDegrees(azimuth + 180);
    }

    public static void setHeadingInDegrees(double magneticHeading) {
        heading = magneticHeading;
    }

    public static double getHeading(){
        return heading;
    }

    /**
     * Calculates {@code a mod b} in a way that respects negative values (for example,
     * {@code mod(-1, 5) == 4}, rather than {@code -1}).
     *
     * @param a the dividend
     * @param b the divisor
     * @return {@code a mod b}
     */
    public static float mod(float a, float b) {
        return (a % b + b) % b;
    }
}
