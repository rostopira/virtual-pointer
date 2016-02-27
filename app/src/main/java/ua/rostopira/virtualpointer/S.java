package ua.rostopira.virtualpointer;

import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Singleton
 */
final public class S {
    private static S instance;
    private InetAddress IP;

    private S() {
        instance = this;
        setIP("192.168.1.69");
    }

    public static S get() {
        if (instance == null) {
            synchronized (S.class) {
                if (instance == null)
                    new S();
            }
        }
        return instance;
    }

    public boolean setIP(String ip) {
        InetAddress temp = IP;
        try {
            IP = InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            IP = temp;
            Log.e("S", "Unknown host exception");
            return false;
        }
    }

    public void send(String... strings) {
        UDPSender sender = new UDPSender(IP);
        sender.execute(strings);
    }
}
