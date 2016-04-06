package reproducer.mine;

/**
 * Created by v on 4/5/16.
 */
public interface ProtocolLightStreamContext {

    boolean resp(String quote);

    String getSubProtocol();

    String getOrigin();

    String getHost();

    int getPort();

    String getUrl();

}
