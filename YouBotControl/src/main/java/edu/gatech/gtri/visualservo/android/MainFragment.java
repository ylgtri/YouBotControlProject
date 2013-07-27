package edu.gatech.gtri.visualservo.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

// Finally discovered netty.io, let's see how this goes...

public class MainFragment extends Fragment {

    private static String host = "192.168.1.140"; // probably shouldn't be hardcoded
    private static int port = 9102; // probably shouldn't be hardcoded
    private static int serverport = 9013;
    public static ImageView mImageView;
    public static String end = "ENDOFJPEGFILE"; //Note: this doesn't actually need to be a JPEG file, as it is attached on the serverside
    public static Long currentFrame;
    public static LinkedBlockingQueue<Bitmap> queue = new LinkedBlockingQueue<Bitmap>(); // The image queue for the choreographer to work off of
    public static CopyOnWriteArrayList<Bitmap> pool = new CopyOnWriteArrayList<Bitmap>(); // Pooling for usage in onBitmap()
    public static Choreographer choreographer; // this allows us to push images without throttling
    public static volatile int currentIndex = 0; // basic lock for queue, inBitmap needs to be a mutable object
    public static View rightJoystick;


    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mImageView = (ImageView) view.findViewById(R.id.image); // Again, Canvas bitmap drawing isn't HW accel
        mImageView.setBackgroundColor(Color.BLACK); // default is white
        rightJoystick = view.findViewById(R.id.joystick2);
        sendMessage(host, serverport, "Connecting"); // initial handshake
        choreographer = Choreographer.getInstance();
        // register netty image channel handlers
        try {
            final byte[] ending = end.getBytes("US-ASCII");
            ServerBootstrap bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool())); // faster frame decoding, at the expense of memory
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    return Channels.pipeline(
                            new DelimiterBasedFrameDecoder(1000000, copiedBuffer(ending)), // max buffer size is 1MB, ending is trimmed
                            new ClientHandler()); // registers our handler
                }
            });
            bootstrap.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    public static void sendMessage(String host, int port, String message) {
        new Thread(new MessageSender(host, serverport, message)).start();
    }


}