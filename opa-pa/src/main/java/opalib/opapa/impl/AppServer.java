package opalib.opapa.impl;

import opalib.api.OpaProc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Augustus
 *
 * For internal usage only (is not part of api)
 *
 */
public class AppServer {
    private final static Logger logger = LoggerFactory.getLogger(AppServer.class);

    private final String baseUrl;
    private final IJsonConverter jsonConverter;
    private final IOppParamReader oppParamReader;
    private final IHttpCaller httpCaller;

    public AppServer(String baseUrl) {
        this.baseUrl = MiscUtils.trimLastSlash(baseUrl);
        httpCaller = new HttpCaller();
        jsonConverter = new JacksonConverter();
        oppParamReader = new OppParamReader();
    }


    public void runProc(Object opp, String proc) {
        if (proc == null) {
            OpaProc opaProc = opp.getClass().getAnnotation(OpaProc.class);
            if (opaProc == null)
                throw new IllegalStateException("Annotation @OpaProc is required for Opp class");
            if (opaProc.proc().isEmpty())
                throw new IllegalStateException("Procedure name should be provided in @OpaProc");
            proc = opaProc.proc();
        }

        String url = baseUrl + "/" + proc;

        String meta = oppParamReader.readOppParams(opp.getClass());

        String json = jsonConverter.getInputJson(opp);

        long start1 = System.currentTimeMillis();
        String resultJson = httpCaller.postUrl(url, json, meta);
        logger.info("Parse time={}ms", System.currentTimeMillis() - start1);

        jsonConverter.applyResponseJson(opp, resultJson);

    }

    public void addTypeExtension() {
    }


    public void shutdown() {
        httpCaller.shutdown();
        logger.debug("Finished opaServer shutdown");
    }

    public void setMaxPoolSize(int max) {
        httpCaller.setMaxPoolSize(max);
    }

}
