package ua.rostopira.virtualpointer;

import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Singleton {

    private static Singleton instance;
    private Quaternion center, current;
    public UDPSender sender;
    InetAddress IP;

    private Singleton() {
        center = new Quaternion();
        sender = new UDPSender();
        try { IP = InetAddress.getByName("192.168.1.4");
        } catch (UnknownHostException e) { }
    }

    public static Singleton get() {
        if (instance==null) {
            instance = new Singleton();
        }
        return instance;
    }

    public void setOrientation(Quaternion q) {
        current = q.clone();
        Quaternion t = q.substract(center);
        sender.execute("M",t.EulerYaw(),t.EulerPitch(),Long.toString(SystemClock.uptimeMillis()));
    }

    public void setCenter() {
        center = current.clone();
    }

    /*
    public String X() {
        return Float.toString(currentOrientation.EulerYaw());
    }

    public String Y() {
        return Float.toString(currentOrientation.EulerPitch());
    }
    */

    public boolean setIP(String ip) {
        InetAddress temp = IP;
        try {
            IP = InetAddress.getByName(ip);
            sender.cancel(false);
            sender = new UDPSender();
            return true;
        } catch (UnknownHostException e) {
            IP = temp;
            Log.e("Singleton", "Unknown host exception");
            return false;
        }
    }
}
