package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender extends AsyncTask<String, Void, Void> {
    public static final int port = 6969;
    InetAddress IP;

    public UDPSender(InetAddress ip) {
        IP = ip;
    }

    @Override
    public Void doInBackground(String... message) {
        if (message[0] == null) {
            Log.e("UDPSender", "Null message");
            return null;
        }
        String t = message[0];
        for (int i=1; i<message.length; i++)
            t = t+" "+message[i];
        byte[] bytes = t.getBytes();
        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, IP, port);
            socket.send(packet);
            Log.d("UDPSender", t);
            socket.close();
        } catch (Exception e) {
            Log.e("UDPSender", "FUUUUUUUUUCKing network");
        }
        return null;
    }

}