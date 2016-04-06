package reproducer.netty_overrides;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;

/**
 * Created by v on 4/5/16.
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("handlerAdded");
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("chanel active");
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (ctx.channel().closeFuture().cause() != null)
            ctx.channel().closeFuture().cause().printStackTrace();
    }

    public void messageReceived(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {

            if (msg instanceof DefaultHttpResponse) {
                DefaultHttpResponse respDef = (DefaultHttpResponse) msg;
                DefaultFullHttpResponse resp = new DefaultFullHttpResponse(respDef.protocolVersion(), respDef.status());
                respDef.headers().forEach(h -> resp.headers().add(h.getKey(), h.getValue()));
                handshaker.finishHandshake(ch, resp);

            } else
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            try {
                ctx.pipeline().get(LightStreamHandler.class).userEventTriggered(ctx, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return;
        }
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

                try {
                    ctx.pipeline().get(LightStreamHandler.class).channelRead0(ctx, textFrame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
               // System.out.println(textFrame.text());
            } else if (frame instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame");

                //it is ok,
            } else if (frame instanceof CloseWebSocketFrame) {
                CloseWebSocketFrame clfr = (CloseWebSocketFrame) frame;
                System.out.println("CloseWebSocketFrame "+clfr.reasonText() );
                ch.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // logger.error("error!", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        messageReceived(ctx, msg);
    }
}