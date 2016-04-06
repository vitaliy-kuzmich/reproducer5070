package reproducer.netty_overrides;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import reproducer.mine.ProtocolLightStreamContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by v on 4/5/16.
 */
@ChannelHandler.Sharable
public class LightStreamHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    ProtocolLightStreamContext protoContext;
    AtomicBoolean wasBind = new AtomicBoolean();
    AtomicBoolean wasSubscribed = new AtomicBoolean();
    private ChannelHandlerContext ctx;

    public LightStreamHandler(ProtocolLightStreamContext protoContext) {
        this.protoContext = protoContext;

    }

    public ProtocolLightStreamContext getProtoContext() {
        return protoContext;
    }

    public void setProtoContext(ProtocolLightStreamContext protoContext) {
        this.protoContext = protoContext;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println("channelRegistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
        System.out.println("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("channelInactive");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        System.out.println("channelUnregistered");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!wasBind.get() && evt instanceof DefaultHttpResponse) {

            if (((DefaultHttpResponse) evt).status().code() == 101) {
                wasBind.set(true);
                ctx.channel().writeAndFlush(new TextWebSocketFrame("hello world"));
            } else System.err.println("server return wrong status code!");

        }

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        if (wasBind.get() && !wasSubscribed.get()) {
            wasSubscribed.set(true);

            ctx.channel().writeAndFlush(new TextWebSocketFrame("hello world"));
        }

        protoContext.resp(msg.text());

    }

    public void close() {
        if (ctx != null)
            ctx.disconnect();

    }
}
