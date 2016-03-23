package ua.rostopira.virtualpointer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Broadcasts in local network, finds server, saves his IP
 * @params Broadcast address (like "192.168.0.255")
 */
public class UDPBroadcast extends AsyncTask<InetAddress, Void, InetAddress> {
    /**
     * Broadcast and listen to answer
     * @return InetAddress of server
     */
    @Override
    public InetAddress doInBackground(InetAddress... broadcastAddress) {
        InetAddress temp = null;
        try {
            //Send Broadcast
            DatagramSocket socket = new DatagramSocket(S.port);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(
                    "B".getBytes(),
                    "B".length(),
                    broadcastAddress[0],
                    S.port
            );
            socket.send(packet);

            //Receive answer
            byte[] buffer = new byte[11];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet); //Own broadcast. Nobody can't hide from broadcast :D
            socket.receive(packet); //Response from server. Hopefully
            temp = packet.getAddress();
            socket.close();
        } catch (SocketException e) {
            Log.e("UDPBroadcast", "Socket exception");
        } catch (IOException e) {
            Log.e("UDPBroadcast", "I/O exception");
        }
        return temp;
    }

    @Override
    public void onPostExecute(InetAddress IP) {
        S.get().IP = IP;
    }
}