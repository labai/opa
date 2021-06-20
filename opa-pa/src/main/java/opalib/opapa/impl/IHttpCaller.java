package opalib.opapa.impl;

/**
 * @author Augustus
 * created on 2021.06.19
 */
public interface IHttpCaller {

    String postUrl(String url, String json, String meta);

    void shutdown();

    void setMaxPoolSize(int max);

}
