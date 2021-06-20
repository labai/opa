package opalib.opapa.impl;

import opalib.api.OpaException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Augustus
 * created on 2021.06.19
 */
public class HttpCaller implements IHttpCaller {
    private final static Logger logger = LoggerFactory.getLogger(HttpCaller.class);
    private final static Logger loggerDetails = LoggerFactory.getLogger("opalib.opapa.errorDetails");

    private final PoolingHttpClientConnectionManager poolingConnManager;
    private final ScheduledExecutorService scheduler;

    public HttpCaller() {
        this.poolingConnManager = new PoolingHttpClientConnectionManager();
        // poolingConnManager.setValidateAfterInactivity(200_000);
        scheduler = createSchedulerExecutor("OpaCleaner");
        scheduler.scheduleAtFixedRate(() -> {
            try {
                poolingConnManager.closeExpiredConnections();
                poolingConnManager.closeIdleConnections(200, TimeUnit.SECONDS);
            } catch (Throwable e) {
                logger.error("Error while executing clean tasks", e);
            }
        }, 60, 20, TimeUnit.SECONDS);
    }

    @Override
    public String postUrl(String url, String json, String meta) {

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        httpPost.setHeader("opa-opp-meta-v1", meta);

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(poolingConnManager).build();

        logger.info("url: " + url);
        logger.info("meta: " + meta);
        logger.info("in json: " + json);
        String jsonRes;
        try {
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            long start1 = System.currentTimeMillis();
            HttpResponse response = client.execute(httpPost);
            logger.info("Call time={}ms", System.currentTimeMillis() - start1);
            if (response.getStatusLine().getStatusCode() >= 400) {
                String errJson = new String(EntityUtils.toByteArray(response.getEntity()));
                // errJson = errJson.replace("\\n", "\n");;
                Map<String, String> errs = JsonUtils.fromJson(errJson, Map.class);
                String msg = errs.get("errorMessage");
                String det = errs.get("errorDetailsText");
                loggerDetails.info("Got response status {}, errorCode: {} message: {}{}", response.getStatusLine().getStatusCode(), errs.get("errorCode"), msg, (MiscUtils.isNotEmpty(det) ? ",\nError details: " + det : ""));
                throw new OpaException("Got response " + response.getStatusLine().getStatusCode() + " from server (" + url + "): " + (MiscUtils.isNotEmpty(msg) ? msg : errJson));
            }
            start1 = System.currentTimeMillis();
            jsonRes = new String(EntityUtils.toByteArray(response.getEntity()));
            logger.info("Read time={}ms", System.currentTimeMillis() - start1);
//            logger.info("out json: " + jsonRes);

        } catch (IOException e) {
            throw new OpaException("Can't execute procedure (" + url + ")", e);
        }

        return jsonRes;
    }

    @Override
    public void shutdown() {
        long tillTs = System.currentTimeMillis() + 1000;
        poolingConnManager.shutdown();
        scheduler.shutdown();
        shutdownWaitExecutorTerminate(scheduler, tillTs, "OpaCleaner");
    }

    @Override
    public void setMaxPoolSize(int max) {
        poolingConnManager.setMaxTotal(max);
    }



    /**
     * create scheduler ThreadPoolExecutor
     */
    ScheduledExecutorService createSchedulerExecutor(final String prefix) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, prefix + ++counter);
            }
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        return executor;
    }


    private void shutdownWaitExecutorTerminate(ExecutorService executor, long tillMs, String name) {
        try {
            if (!executor.awaitTermination(Math.max(tillMs - System.currentTimeMillis(), 200L), TimeUnit.MILLISECONDS)) {
                logger.warn("{} did not terminated successfully", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("InterruptedException while waiting for " + name + " to terminate", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status (see ThreadPoolExecutor javadoc)
        }
    }

}
