package ua.rostopira.virtualpointer;

/**
 * The Quaternion class. A Quaternion is a four-dimensional vector that is used to represent
 * rotations of a rigid body in the 3D space. It is very similar to a rotation vector; it contains
 * an angle, encoded into the w component and three components to describe the rotation-axis
 * (encoded into x, y, z).
 */

public class Quaternion {
    public float x,y,z,w;
    static final float pi4 = (float) (Math.PI/4);

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

    public float dotProduct(Quaternion i) {
        return ( x*i.x + y*i.y + z*i.z + w*i.w );
    }

    /**
     * Get a linear interpolation between this quaternion and the input quaternion.
     * 
     * @param input The quaternion to be slerped with this quaternion.
     * @param t The ratio between the two quaternions where 0 <= t <= 1.0 . Increase value of t
     *          will bring rotation closer to the input quaternion.
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

        float sinHalfTheta = (float) Math.sqrt(1.0 - cosHalftheta * cosHalftheta);
        float halfTheta = (float) Math.acos(cosHalftheta);

        float ratioA = (float) Math.sin((1 - t) * halfTheta) / sinHalfTheta;
        float ratioB = (float) Math.sin(t * halfTheta) / sinHalfTheta;

        float X,Y,Z,W;
        W = w * ratioA + bufferQuat.w * ratioB;
        X = x * ratioA + bufferQuat.x * ratioB;
        Y = y * ratioA + bufferQuat.y * ratioB;
        Z = z * ratioA + bufferQuat.z * ratioB;
        return new Quaternion(X,Y,Z,W);
    }

    public float yaw() {
        return (float) Math.asin(2*x*y + 2*z*w);
    }

    public float pitch() {
        return (float) Math.atan2(2*x*w -2*y*z, 1 -2*x*x -2*z*z);
    }

}
