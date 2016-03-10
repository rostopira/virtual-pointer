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
 */
public class UDPBroadcast extends AsyncTask<Void, Void, InetAddress> {
    private InetAddress broadcastAddress;

    /**
     * Detect broadcast address
     * Class B networks unsupported
     */
    @Override
    public void onPreExecute() {
        try {
            //Need to check all network interfaces, Set-Top-Boxes usually have Ethernet and Wi-Fi
            Enumeration networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
            do {
                NetworkInterface networkInterface = (NetworkInterface) networkInterfaceEnum.nextElement();
                Enumeration IPEnum = networkInterface.getInetAddresses();
                do {
                    InetAddress IP = (InetAddress) IPEnum.nextElement();
                    if (!IP.isLoopbackAddress() && IP instanceof Inet4Address) {
                        //Broadcast address = (ip & mask) | ~mask
                        //But for local networks (class C) subnet mask is always 255.255.255.0
                        byte [] ip = IP.getAddress();
                        //So we need just to replace last byte with 255 or -128
                        ip[3] = -128;
                        broadcastAddress = InetAddress.getByAddress(ip);
                        return;
                    }
                } while (IPEnum.hasMoreElements());
            } while (networkInterfaceEnum.hasMoreElements());
        } catch (UnknownHostException e) {
            Log.e("UDPBroadcast", "Unknown host exception");
        } catch (SocketException e) {
            Log.e("UDPBroadcast", "Socket exception");
        }
    }

    /**
     * Broadcast and listen to answer
     * @return InetAddress of server
     */
    @Override
    public InetAddress doInBackground(Void... voids) {
        try {
            //Send Broadcast
            DatagramSocket socket = new DatagramSocket(S.port);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(
                    "B".getBytes(),
                    "B".length(),
                    broadcastAddress,
                    S.port
            );
            socket.send(packet);

            //Receive answer
            byte[] buffer = new byte[11];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            socket.close(); //Always close sockets, be a good boy
            return packet.getAddress();
        } catch (SocketException e) {
            Log.e("UDPBroadcast", "Socket exception");
        } catch (IOException e) {
            Log.e("UDPBroadcast", "I/O exception");
        }
        return null;
    }

    /**
     * Save result
     */
    @Override
    public void onPostExecute(InetAddress IP) {
        S.get().IP = IP;
    }
}