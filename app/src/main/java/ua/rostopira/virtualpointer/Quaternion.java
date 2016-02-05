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

    /**
     *
     *  MAGIC
     *  DO NOT TOUCH
     *
     */

    Vector px = new Vector(1,0,0), pz = new Vector(1,0,0);
    float hCos=1, hSin=0; // sin & cos used to rotate measured heading back by baseline amount
    float headingBaseline = 0; // baseline compass heading
    float inclinationBaseline = 0; // baseline inclination from horizontal
    private final float sensitivity = 0.5f;

    private void setVectors() {
        px.x = 2*x*x -1 + 2*y*y;
        px.y = 2*y*z -2*x*w;
        px.z = 2*y*w + 2*x*z;
        pz.x = 2*y*w - 2*x*z;
        pz.y = 2*z*w + 2*x*y;
        pz.z = 2*x*x - 1 + 2*w*w;
    }

    public String getXY() {
        setVectors();
        float xeff = hCos*px.x - hSin*px.y;
        float yeff = hSin*px.x + hCos*px.y;
        float headingDelta = (float) Math.atan2(yeff, xeff);
        float x = sensitivity * (float) Math.tan(Math.asin(pz.y) - inclinationBaseline);
        headingBaseline = (float) -Math.atan2(px.y,  px.x);
        hSin = (float) Math.sin(headingBaseline);
        hCos = (float) Math.cos(headingBaseline);
        inclinationBaseline= (float) Math.asin(pz.y);
        float y = sensitivity * (float) Math.tan(headingDelta);
        return Float.toString(x) + " " + Float.toString(y);
    }

    public void center() {
        setVectors();
        headingBaseline = (float) -Math.atan2(px.y,  px.x);
        hSin = (float) Math.sin(headingBaseline);
        hCos = (float) Math.cos(headingBaseline);
        inclinationBaseline= (float) Math.asin(pz.y);
    }

}
