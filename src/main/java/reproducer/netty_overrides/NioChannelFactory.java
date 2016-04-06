package reproducer.netty_overrides;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by v on 4/5/16.
 */
public class NioChannelFactory implements ChannelFactory<NioSocketChannel> {

    public NioSocketChannel newChannel() {
        try {

            SocketChannel ch = SocketChannel.open();
            ch.configureBlocking(false);
            NioSocketChannel res = new NioSocketChannel(ch);
            return res;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;}

    public Channel newChannel(EventLoop eventLoop) {
        return newChannel();
    }
}
