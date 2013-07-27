package edu.gatech.gtri.visualservo.android;


import android.util.Log;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class ClientHandler extends SimpleChannelHandler {

    // an instance is invoked every socket request
    // hence the discard

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        ChannelBuffer buf = (ChannelBuffer) e.getMessage();
        byte[] head = new byte[13]; // 13 is length of timestamp header
        for (int i = 0; i < 13; i++) {
            head[i] = buf.readByte(); // read the first 13 bytes of the incoming message
        }
        Long time = Long.parseLong(new String(head));
        if (MainFragment.currentFrame == null || time > MainFragment.currentFrame) { // discard frames which arrive late
            MainFragment.currentFrame = time;
            byte[] tmp = buf.array();
            // Log.d("RECEIVEDFROMSOCKET", tmp.length + " bytes");
            new Thread(new ImageDecoder(tmp, time)).run();
        } else {
            buf.discardReadBytes(); // kill the connection
            ctx.getChannel().close();
            Log.d("DISCARDED", "Discarded " + time);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

}
