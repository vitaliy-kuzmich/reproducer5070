package reproducer.netty_overrides;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import reproducer.mine.ProtocolLightStreamContext;

import java.net.URI;

/**
 * Created by v on 4/5/16.
 */
public class WebSocketClientHandshaker13Fixed extends WebSocketClientHandshaker13 {
    ProtocolLightStreamContext context;

    public ProtocolLightStreamContext getContext() {
        return context;
    }

    public void setContext(ProtocolLightStreamContext context) {
        this.context = context;
    }

    public WebSocketClientHandshaker13Fixed(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders) {
        super(webSocketURL, version, subprotocol, allowExtensions, customHeaders, 65536);
    }

    @Override
    protected FullHttpRequest newHandshakeRequest() {
        FullHttpRequest req = super.newHandshakeRequest();
        req.headers().set(HttpHeaderNames.SEC_WEBSOCKET_ORIGIN, getContext().getOrigin());

        FullHttpRequest fixed = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, context.getUrl());

        req.headers().forEach(h -> fixed.headers().add(h.getKey(), h.getValue()));
        fixed.headers().set(HttpHeaderNames.HOST, context.getHost());
        fixed.headers().set(HttpHeaderNames.PRAGMA, "no-cache");
        fixed.headers().set(HttpHeaderNames.ORIGIN, context.getOrigin());
        fixed.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");

        return fixed;
    }

}
