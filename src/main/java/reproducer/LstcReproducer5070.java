package reproducer;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import reproducer.mine.ProtocolLightStreamContext;
import reproducer.mine.WSClient;

/**
 * Created by v on 4/5/16.
 */
public class LstcReproducer5070 {
    public static void main(String... args) throws Exception {
        HttpProxyServer server =
                DefaultHttpProxyServer.bootstrap()
                        .withPort(8080)
                        .start();

        String prox = server.getListenAddress().toString().replace("/", "");



        WSClient client = new WSClient();

        client.bind(new ProtocolLightStreamContext() {

            @Override
            public boolean resp(String quote) {
                System.out.println(quote);

                return true;
            }

            @Override
            public String getSubProtocol() {
                return "js.lightstreamer.com";
            }

            @Override
            public String getOrigin() {
                return "https://www.ls-tc.de";
            }

            @Override
            public String getHost() {
                return "push.ls-tc.de";
            }

            @Override
            public int getPort() {
                return 443;
            }

            @Override
            public String getUrl() {
                return "wss://push.ls-tc.de/lightstreamer";
            }

        }, prox);



        synchronized (client) {
            client.wait(60_000);
        }

        client.shutdown();

    }
}
