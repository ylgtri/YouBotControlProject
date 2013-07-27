package edu.gatech.gtri.visualservo.android;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.Charset;

public class MessageSender implements Runnable {

    private String host;
    private int port;
    private String message;

    public MessageSender(String host, int port, String message) {
        this.host = host;
        this.port = port;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            Socket skt = new Socket(host, port); // get OutputStream and write to it
            OutputStream out = skt.getOutputStream();
            out.write(encode(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encode(String s) { // not sure why ASCII doesn't work, but we need this for our JSON anyways
        String hex = String.format("%x", new BigInteger(1, s.getBytes(Charset.forName("UTF-8"))));
        return hexStringToByteArray(hex);
    }

    public static byte[] hexStringToByteArray(String s) { // Sadly, Android doesn't provide libs for these
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
