package ua.rostopira.virtualpointer;

import android.os.Handler;
import java.net.*;

public class UDPSender {
    public static String IP = "192.168.1.6";
    static int PORT = 6969;
    public void Send(String MESSAGE) {

        String msg = MESSAGE;
        if(msg == null) return;
        byte[] msgBytes = msg.getBytes();

        final byte[] buf = msgBytes;

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress serverAddress = InetAddress.getByName(IP);
                    DatagramSocket socket = new DatagramSocket();
                    if (!socket.getBroadcast()) socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length,
                            serverAddress, PORT);
                    socket.send(packet);
                    socket.close();
                } catch (Exception e) {}
            }
        });
        t.start();
    }
}