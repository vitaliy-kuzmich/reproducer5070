package reproducer.netty_overrides;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reproducer.mine.ProtocolLightStreamContext;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Created by v on 4/5/16.
 */
public class WSInitializer extends ChannelInitializer<Channel> {

    private ProtocolLightStreamContext protoContext;
    private LightStreamHandler handler;
    private String prox;


    public WSInitializer(LightStreamHandler handler, String prox

    ) {
        this.prox = prox;
        this.protoContext = handler.getProtoContext();
        this.handler = handler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {


        SslContext sslContext = SslContextBuilder.forClient()

                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();


        DefaultHttpHeaders headers = new DefaultHttpHeaders();

        WebSocketClientHandshaker13Fixed hld = new WebSocketClientHandshaker13Fixed(URI.create(protoContext.getHost()), WebSocketVersion.V13, protoContext.getSubProtocol(), true, headers);
        hld.setContext(protoContext);

        WebSocketClientHandler wsHandler = new WebSocketClientHandler(hld);

        ChannelPipeline p = ch.pipeline();

        String pr[] = prox.split(":");
        p.addLast(new HttpProxyHandler(new InetSocketAddress(pr[0], Integer.parseInt(pr[1]))));

        if (protoContext.getUrl().startsWith("wss"))
            p.addLast(new SslHandler(sslContext.newEngine(ByteBufAllocator.DEFAULT, protoContext.getHost(), protoContext.getPort())));


        p.addLast(
                new HttpClientCodec(),
                wsHandler,
                handler
        );

    }


}