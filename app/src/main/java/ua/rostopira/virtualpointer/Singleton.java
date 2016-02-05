package ua.rostopira.virtualpointer;

import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

final public class Singleton {
    private static Singleton instance;
    private InetAddress IP;

    private Singleton() {
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
        send("M", q.getXY(), Long.toString(SystemClock.uptimeMillis()));
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
