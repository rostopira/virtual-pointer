package ua.rostopira.virtualpointer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * The orientation provider that delivers the absolute orientation from the Gyroscope and Android
 * Rotation Vector.
 * 
 * It mainly relies on the gyroscope, but corrects with the Android Rotation Vector which also
 * provides an absolute estimation of current orientation. The correction is a static weight.
 * 
 * @author Alexander Pacha
 */
public class SensorFusion implements SensorEventListener {
    private Sensor gyroscope, rotationVector;
    private SensorManager sensorManager;
    /**
     * Constant specifying the factor between a Nano-second and a second
     */
    private static final float NS2S = 1.0f / 1000000000.0f;

    /**
     * The quaternion that stores the difference that is obtained by the gyroscope.
     * Basically it contains a rotational difference encoded into a quaternion.
     * 
     * To obtain the absolute orientation one must add this into an initial position by
     * multiplying it with another quaternion
     */
    private final Quaternion deltaQuaternion = new Quaternion();

    /**
     * The Quaternions that contain the current rotation of the Gyroscope
     */
    private Quaternion quaternionGyroscope = new Quaternion();

    /**
     * The quaternion that contains the absolute orientation as obtained by the rotation vector.
     */
    private Quaternion quaternionRotationVector = new Quaternion();

    private long timestamp = 0;

    /**
     * This is a filter-threshold for discarding Gyroscope measurements that are below a certain
     * level and potentially are only noise and not real motion. Values from the gyroscope are
     * usually between 0-10, so 0.1 or smaller seems to be a reasonable threshold to filter noise.
     * Note that there is a chance of missing real motion, if the use is turning the
     * device really slowly. 0.1 seems to work fine for most applications.
     */
    private static final double EPSILON = 0.1f;

    /**
     * Value giving the total velocity of the gyroscope. This is usually a value between 0 and 10
     * for normal motion. Heavy shaking can increase it to about 25.
     */
    private double gyroscopeRotationVelocity = 0;

    /**
     * Flag indicating, whether the orientations were initialised from the rotation vector or not.
     * If false, the gyroscope can not be used (since it's only meaningful to calculate differences
     * from an initial state).
     */
    private boolean positionInitialised = false;

    /**
     * Counter that sums the number of consecutive frames, where the rotationVector and the
     * gyroscope were significantly different (and the dot-product was smaller than 0.7). This
     * event can either happen when the angles of the rotation vector explode (e.g. during fast
     * tilting) or when the device was shaken heavily and the gyroscope is now completely off.
     */
    private int panicCounter = 0;

    /**
     * This weight determines indirectly how much the rotation sensor will be used to correct. This
     * weight will be multiplied by the velocity to obtain the actual weight.
     * Must be a value between 0 and approx. 0.04 (if multiplied with a velocity of up to 25,
     * should be still less than 1, otherwise the SLERP will not correctly interpolate).
     */
    private static final float INDIRECT_INTERPOLATION_WEIGHT = 0.01f;

    /**
     * The threshold that indicates an outlier of the rotation vector. If the dot-product between
     * the two vectors falls below this threshold (ideally it should be 1, if they are the same)
     * the system falls back to the gyroscope values only and just ignores the rotation vector.
     * 
     * This value should be quite high (> 0.7) to filter even the slightest discrepancies.
     */
    private static final float OUTLIER_THRESHOLD = 0.85f;

    /**
     * The threshold that indicates a massive discrepancy between the rotation vector and the
     * gyroscope orientation. If the dot-product between the two vectors falls below this threshold,
     * the system will start increasing the panic counter (probably a gyroscope fail).
     * 
     * This value should be lower than OUTLIER_THRESHOLD (0.5 - 0.7) to only start increasing the
     * panic counter, when there is a huge discrepancy.
     */
    private static final float OUTLIER_PANIC_THRESHOLD = 0.75f;

    /**
     * The threshold that indicates that a chaos state has been established rather than just a
     * temporary peak in the rotation vector (caused by exploding angled during fast tilting).
     * 
     * If the panicCounter is bigger than this threshold, the current position will be reset to
     * whatever the rotation vector indicates.
     */
    private static final int PANIC_THRESHOLD = 60;

    /**
     * Initialises a new ImprovedOrientationSensor2Provider
     * 
     * @param sensorManager The android sensor manager
     */
    public SensorFusion(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    private static final int SENSOR_DELAY = 30000; //in miliseconds

    public void start() {
        sensorManager.registerListener(this, gyroscope, SENSOR_DELAY);
        sensorManager.registerListener(this, rotationVector, SENSOR_DELAY);
    }

    public void stop() {
        sensorManager.unregisterListener(this, gyroscope);
        sensorManager.unregisterListener(this, rotationVector);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // Process rotation vector (just save it)
            float[] q = new float[4];
            // Calculate angle.
            // Starting with API_18, Android will provide this value as event.values[3]
            SensorManager.getQuaternionFromVector(q, event.values);
            // Store in quaternion
            quaternionRotationVector= new Quaternion(q[1], q[2], q[3], -q[0]);
            if (!positionInitialised) {
                quaternionGyroscope = quaternionRotationVector.clone();
                positionInitialised = true;
            }
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Process Gyroscope and perform fusion
            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            //String result = null;
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float X = event.values[0];
                float Y = event.values[1];
                float Z = event.values[2];

                // Calculate the angular speed of the sample
                gyroscopeRotationVelocity = Math.sqrt(X * X + Y * Y + Z * Z);

                // Normalize the rotation vector if it's big enough to get the axis
                if (gyroscopeRotationVelocity > EPSILON) {
                    X /= gyroscopeRotationVelocity;
                    Y /= gyroscopeRotationVelocity;
                    Z /= gyroscopeRotationVelocity;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                double thetaOverTwo = gyroscopeRotationVelocity * dT / 2.0f;
                double sinThetaOverTwo = Math.sin(thetaOverTwo);
                deltaQuaternion.x = (float) (sinThetaOverTwo * X);
                deltaQuaternion.y = (float) (sinThetaOverTwo * Y);
                deltaQuaternion.z = (float) (sinThetaOverTwo * Z);
                deltaQuaternion.w = -(float) Math.cos(thetaOverTwo);

                // Move current gyro orientation
                quaternionGyroscope = deltaQuaternion.multiply(quaternionGyroscope);

                // Calculate dot-product to calculate whether the orientation sensors have diverged,
                // it should be close to 1 if both are the same.
                float dotProd = quaternionGyroscope.dotProduct(quaternionRotationVector);

                // If they have diverged, rely on gyroscope only
                // (this happens on some devices when the rotation vector "jumps").
                if (Math.abs(dotProd) < OUTLIER_THRESHOLD) {
                    // Increase panic counter
                    if (Math.abs(dotProd) < OUTLIER_PANIC_THRESHOLD) {
                        panicCounter++;
                    }
                    // Directly use Gyro
                    //result = quaternionGyroscope.getXY();
                } else {
                    // Both are nearly saying the same. Perform normal fusion.
                    // Interpolate with a fixed weight between the two absolute quaternions obtained
                    // from gyro and rotation vector sensors. The weight should be quite low, so the
                    // rotation vector corrects the gyro only slowly, and the output keeps responsive.
                    quaternionGyroscope = quaternionGyroscope.slerp(quaternionRotationVector,
                            (float) (INDIRECT_INTERPOLATION_WEIGHT * gyroscopeRotationVelocity));
                    //result = quaternionGyroscope.getXY();
                    // Reset the panic counter because both sensors are saying the same again
                    panicCounter = 0;
                }

                if (panicCounter > PANIC_THRESHOLD) {
                    Log.d("Rotation Vector", "Panic counter is bigger than threshold - Gyroscope failure.");
                    if (gyroscopeRotationVelocity < 3) {
                        Log.d("Rotation Vector", "Panic reset");
                        // Manually set position to whatever rotation vector says.
                        //result = quaternionRotationVector.getXY();
                        // Override current gyroscope-orientation with corrected value
                        quaternionGyroscope = quaternionRotationVector.clone();
                        panicCounter = 0;
                    }
                }
            }
            timestamp = event.timestamp;
            S.get().send("M",deltaQuaternion.getXY());
        }
    }
}
