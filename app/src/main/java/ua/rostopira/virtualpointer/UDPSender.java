package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This task gets Strings, merge them into one with ' ' as splitter and send to server
 */
public class UDPSender extends AsyncTask<String, Void, Void> {
    @Override
    public Void doInBackground(String... message) {
        if (S.get().IP == null)
            return null;
        String msg = message[0];
        for (int i=1; i<message.length; i++)
            msg = msg+" "+message[i];
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.send(
                    new DatagramPacket(
                            msg.getBytes(),
                            msg.length(),
                            S.get().IP,
                            S.port
                    )
            );
            Log.d("UDPSender", msg);
            socket.close();
        } catch (Exception e) {
            Log.e("UDPSender", "FUUUUUUUUUCKing network"); //SO MANY EXCEPTIONS
        }
        return null;
    }
}