package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class UDPBroadcast extends AsyncTask<Void, Void, Void> {
    private byte[] localIP;

    @Override
    public Void doInBackground(Void... v) {
        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket("B".getBytes(),"B".getBytes().length);
            packet.setPort(S.port);
            for (byte i = 0; i<256; i++) {
                if (isCancelled())
                    return null;
                localIP[3] = i;
                packet.setAddress(InetAddress.getByAddress(localIP));
                socket.send(packet);
            }
        } catch (Exception e) {}
        return null;
    }

    public UDPBroadcast() {
        try { //get local IP, using some magic
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        localIP = inetAddress.getAddress();
                        return;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Socket exception", ex.toString());
        }
    }
}
