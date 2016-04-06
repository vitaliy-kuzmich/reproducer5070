package reproducer.mine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import reproducer.netty_overrides.LightStreamHandler;
import reproducer.netty_overrides.NioChannelFactory;
import reproducer.netty_overrides.WSInitializer;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by v on 4/5/16.
 */

public class WSClient {
    private NioEventLoopGroup group;
    private LightStreamHandler handler;
    private ExecutorService eventLoopNettyPool;

    public NioEventLoopGroup getGroup() {
        return group;
    }

    public void setGroup(NioEventLoopGroup group) {
        this.group = group;
    }


    public WSClient() {
        eventLoopNettyPool = Executors.newFixedThreadPool(4, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("eventLoopNettyPool ");
            return thread;
        });
        group = new NioEventLoopGroup(4, eventLoopNettyPool);
    }


    public WSClient bind(ProtocolLightStreamContext protoContext,String prox) throws MalformedURLException {
        handler = new LightStreamHandler(protoContext);
        Bootstrap bootstrapSsl = new Bootstrap();
        bootstrapSsl.group(group);
        bootstrapSsl.handler(new WSInitializer(handler,prox));
        bootstrapSsl.option(ChannelOption.TCP_NODELAY, true);
        bootstrapSsl.option(ChannelOption.SO_REUSEADDR, false);
        // bootstrapSsl.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        bootstrapSsl.channelFactory(new NioChannelFactory());

        ChannelFuture fut = bootstrapSsl.connect(protoContext.getUrl().split("//")[1].split("/")[0], protoContext.getPort());
        fut.addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });


        return this;
    }

    public void shutdown() {
        if (handler != null)
            handler.close();


        if (group != null) {
            group.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
            if (!group.isTerminated()) {
                group.shutdownNow();
            }
        }

        eventLoopNettyPool.shutdownNow();


    }

}
