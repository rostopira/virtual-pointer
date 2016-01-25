package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPSender extends AsyncTask<String, Void, Void> {
    DatagramSocket socket;
    public static final int port = 6969;

    public UDPSender() {
        try {
            socket = new DatagramSocket(port, Singleton.get().IP);
        } catch (Exception e) {
            Log.e("UDPSender", "Failed to open socket");
        }
    }

    @Override
    public Void doInBackground(String... message) {
        if ( (socket==null) | (message[0]==null) ) {
            Log.e("UDPSender", "Packet not sent. Null socket or message");
            return null;
        }
        String t = message[0];
        for (int i=1; i<message.length; i++)
            t = t+" "+message[i];
        byte[] bytes = t.getBytes();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        try {
            socket.send(packet);
            Log.d("UDPSender", "Packet sent");
        } catch (IOException e) {
            Log.e("UDPSender", "I/O Exception");
        }
        return null;
    }

    @Override
    public void onCancelled(Void result) {
        socket.close();
        Log.d("UDPSender", "Socket closed due errors");
    }

}