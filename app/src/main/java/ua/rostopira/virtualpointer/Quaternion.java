package ua.rostopira.virtualpointer;

/**
 * The Quaternion class. A Quaternion is a four-dimensional vector that is used to represent rotations of a rigid body
 * in the 3D space. It is very similar to a rotation vector; it contains an angle, encoded into the w component
 * and three components to describe the rotation-axis (encoded into x, y, z).
 */

public class Quaternion {
    public float x,y,z,w;

    public Quaternion () {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }
    public Quaternion (float X, float Y, float Z, float W) {
        x = X;
        y = Y;
        z = Z;
        w = W;
    }

    @Override
    public Quaternion clone() {
        return new Quaternion(x,y,z,w);
    }
    public Quaternion minus() {
        return new Quaternion(-x,-y,-z,-w);
    }

    public Quaternion multiply(Quaternion i) {
        Quaternion o = new Quaternion();
        o.w = (w * i.w) - (x * i.x) - (y * i.y) - (z * i.z);
        o.x = (w * i.x) + (x * i.w) + (y * i.z) - (z * i.y);
        o.y = (w * i.y) + (y * i.w) + (z * i.x) - (x * i.z);
        o.z = (w * i.z) + (z * i.w) + (x * i.y) - (y * i.x);
        return o;
    }

    public Quaternion substract(Quaternion i) {
        float X,Y,Z,W;
        X = x - i.x;
        Y = y - i.y;
        Z = z - i.z;
        W = w - i.w;
        return new Quaternion(X,Y,Z,W);
    }

    public float dotProduct(Quaternion i) {
        return ( x*i.x + y*i.y + z*i.z + w*i.w );
    }

    /**
     * Get a linear interpolation between this quaternion and the input quaternion, storing the result in the output
     * quaternion.
     * 
     * @param input The quaternion to be slerped with this quaternion.
     * @param t The ratio between the two quaternions where 0 <= t <= 1.0 . Increase value of t will bring rotation
     *            closer to the input quaternion.
     */
    public Quaternion slerp(Quaternion input, float t) {
        // Calculate angle between them.
        float cosHalftheta = this.dotProduct(input);

        if (Math.abs(cosHalftheta) >= 1.0)
            return this.clone();

        Quaternion bufferQuat;
        if (cosHalftheta < 0) {
            cosHalftheta = -cosHalftheta;
            bufferQuat = input.minus();
        } else {
            bufferQuat = input.clone();
        }

        double sinHalfTheta = Math.sqrt(1.0 - cosHalftheta * cosHalftheta);
        double halfTheta = Math.acos(cosHalftheta);

        double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
        double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

        float X,Y,Z,W;
        W = ((float) (w * ratioA + bufferQuat.w * ratioB));
        X = ((float) (x * ratioA + bufferQuat.x * ratioB));
        Y = ((float) (y * ratioA + bufferQuat.y * ratioB));
        Z = ((float) (z * ratioA + bufferQuat.z * ratioB));
        return new Quaternion(X,Y,Z,W);
    }

    public String EulerYaw() {
        return Float.toString(
                (float) Math.atan2(2 * (y * w - x * z), 1 - 2 * (y * y - z * z)) );
    }

    public String EulerPitch() {
        return Float.toString(
                (float) Math.asin(2 * (x * y + z * w)) );
    }

    public String EulerRoll() {
        return Float.toString(
                (float) Math.atan2( 2*(x*w - y*z),    1 - 2*(x*x - z*z) ) );
    }

}
