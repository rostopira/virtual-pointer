package ua.rostopira.virtualpointer;

import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

final public class Singleton {
    private static Singleton instance;
    private Quaternion center, current;
    private InetAddress IP;

    private Singleton() {
        center = new Quaternion();
        instance = this;
        setIP("192.168.1.2");
    }

    public static Singleton get() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null)
                    new Singleton();
            }
        }
        return instance;
    }

    public void setOrientation(Quaternion q) {
        current = q.clone();
        Quaternion t = q.substract(center);
        send("M", t.EulerYaw(), t.EulerRoll(), Long.toString(SystemClock.uptimeMillis()));
    }

    public void setCenter() {
        center = current.clone();
    }

    public boolean setIP(String ip) {
        InetAddress temp = IP;
        try {
            IP = InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            IP = temp;
            Log.e("Singleton", "Unknown host exception");
            return false;
        }
    }

    public void send(String... strings) {
        UDPSender sender = new UDPSender(IP);
        sender.execute(strings);
    }
}
