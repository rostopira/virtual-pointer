package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This task gets Strings, merge them into one with ' ' as splitter and send them to the IP address,
 * defined as constructor parameter;
 */
public class UDPSender extends AsyncTask<String, Void, Void> {
    InetAddress IP;

    public UDPSender(InetAddress ip) {
        IP = ip;
    }

    @Override
    public Void doInBackground(String... message) {
        if (message[0] == null)
            return null;
        String t = message[0];
        for (int i=1; i<message.length; i++)
            t = t+" "+message[i];
        byte[] bytes = t.getBytes();
        try { //TODO: make this to work as conveyor to reduce RAM usage and GC work
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, IP, S.port);
            socket.send(packet);
            Log.d("UDPSender", t);
            socket.close();
        } catch (Exception e) {
            Log.e("UDPSender", "FUUUUUUUUUCKing network"); //SO MANY EXCEPTIONS
        }
        return null;
    }

}