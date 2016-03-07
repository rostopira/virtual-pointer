package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Listens to server answer and returns his IP
 */
public class UDPScanner extends AsyncTask<Void, Void, InetAddress> {
    @Override
    public InetAddress doInBackground(Void... v) {
        try {
            DatagramSocket dsocket = new DatagramSocket(S.port);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            String msg;
            do {
                dsocket.receive(packet);
                msg = new String(buffer, 0, packet.getLength());
            } while (!msg.equals("VPS here!")); //Prevent discover own broadcast and other apps
            return packet.getAddress();
        } catch (Exception e) {
            Log.e("UDPScanner", "Scan failed");
            return null;
        }
    }
}
