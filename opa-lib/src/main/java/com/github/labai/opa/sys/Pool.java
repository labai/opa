package com.github.labai.opa.sys;

import com.github.labai.opa.OpaServer.SessionModel;
import com.github.labai.opa.sys.AppServer.ConnectionConfigurer;
import com.github.labai.opa.sys.AppServer.RequestIdProvider;
import com.progress.open4gl.Open4GLException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Augustus
 *         created on 2015.10.13
 *
 * For internal usage only (is not part of api)
 *
 */
class Pool {
	private final static Logger logger = LoggerFactory.getLogger(Pool.class);

	private static final int DEFAULT_CONN_TTL_SEC = 238;  // 3:58
	private static final int EVICT_PERIOD_SEC = 5;
	private static final int CONN_RELEASE_TIMOUT_SEC = 30;

	static class ConnParams {
		final String urlString;
		final String userId;
		final String password;
		final SessionModel sessionModel;

		// config via setters
		int connectionTTLSec = DEFAULT_CONN_TTL_SEC;
		String sslCertificateStore;
		Boolean sslNoHostVerify;

		// manual connection configurer
		ConnectionConfigurer connectionConfigurer;
		// requestId provider (general for connection)
		RequestIdProvider requestIdGenerator;


		public ConnParams(String urlString, String userId, String password, SessionModel sessionModel) {
			this.password = password;
			this.userId = userId;
			this.urlString = urlString;
			this.sessionModel = sessionModel;
		}

		@Override
		public String toString() {
			return "Conn{url='" + urlString + "',userId=" + userId + ",password=*****,sessionModel=" + sessionModel + "}";
		}
	}

	/**
	 * connection pool
	 */
	public static class JpxConnPool extends GenericObjectPool<JavaProxyAgent> {

		public JpxConnPool(ConnParams connParams, GenericObjectPoolConfig poolConfig) {
			super(new JpxConnFactory(connParams), poolConfig);
			this.setTestOnBorrow(true);
			this.setTestWhileIdle(true);
			this.setTimeBetweenEvictionRunsMillis(EVICT_PERIOD_SEC * 1000L);
			this.setMinEvictableIdleTimeMillis(connParams.connectionTTLSec * 1000L);
		}

		public void setConnectionTTLSec(int connectionTTLSec) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.connectionTTLSec = connectionTTLSec;
			this.setMinEvictableIdleTimeMillis(connParams.connectionTTLSec * 1000L);
		}

		public void setCertificateStore(String psccertsPath) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.sslCertificateStore = psccertsPath;
		}

		public void setNoHostVerify(Boolean value) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.sslNoHostVerify = value;
		}

		public void setConnectionConfigurer(ConnectionConfigurer connConfigurer) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.connectionConfigurer = connConfigurer;
		}

		public void setRequestIdGenerator(RequestIdProvider requestIdGenerator) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.requestIdGenerator = requestIdGenerator;
		}

		public void shutdown() {
			long tillMs = System.currentTimeMillis() + this.getEvictorShutdownTimeoutMillis();
			this.close();
			ThreadPoolExecutor zque = ((JpxConnFactory)this.getFactory()).zombieQueue;
			zque.shutdown();
			shutdownWaitExecutorTerminate(zque, tillMs, "ZombieQueue");
			ThreadPoolExecutor zkil = ((JpxConnFactory)this.getFactory()).zombieKiller;
			zkil.shutdown();
			shutdownWaitExecutorTerminate(zkil, tillMs, "ZombieKiller");
		}

		private void shutdownWaitExecutorTerminate(ThreadPoolExecutor executor, long tillMs, String name) {
			try {
				if (!executor.awaitTermination(Math.max(tillMs - System.currentTimeMillis(), 200L), TimeUnit.MILLISECONDS)) {
					logger.warn("{} did not terminated successfully", name);
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				logger.warn("InterruptedException while waiting for " + name + " to _release connection", e);
				executor.shutdownNow();
				Thread.currentThread().interrupt(); // Preserve interrupt status (see ThreadPoolExecutor javadoc)
			}
		}
	}


	/**
	 * connection factory
	 */
	private static class JpxConnFactory extends BasePooledObjectFactory<JavaProxyAgent> {

		final ConnParams connParams;
		final ThreadPoolExecutor zombieQueue = createExecutor(2, 200, "OpaZQue-");
		final ThreadPoolExecutor zombieKiller = createExecutor(2, 20, "OpaZKil-");

		public JpxConnFactory(ConnParams connParams) {
			this.connParams = connParams;
		}

		@Override
		public JavaProxyAgent create() throws Exception {
			return new JavaProxyAgent(connParams);
		}

		@Override
		public PooledObject<JavaProxyAgent> wrap(JavaProxyAgent javaProxyAgent) {
			return new DefaultPooledObject<JavaProxyAgent>(javaProxyAgent);
		}

		@Override
		public void destroyObject(final PooledObject<JavaProxyAgent> pooledObj) {
			// Sometimes _release() causes connection timeout - may take 15+ min. and then get connection timeout.
			// For timeout for _release() - use zombieKiller.
			// But even with timeout, disconnecting should not stop main thread - use zombieQueue.
			zombieQueue.submit(() -> {
				long startTs = System.currentTimeMillis();
				int objectLogId = Math.abs(pooledObj.getObject().hashCode() % 100000);
				try {
					logger.trace("Before release(1) {}", objectLogId);
					Future<?> zk = zombieKiller.submit(() -> {
						try {
							logger.trace("Before release(2) {}", objectLogId);
							pooledObj.getObject()._release();
						} catch (Open4GLException e) {
							logger.info("Open4GLException while JavaProxyAgent (" + objectLogId + ") _release: " + e.getMessage());
							logger.debug("Exception", e);
							// ignore
						}
					});
					zk.get(CONN_RELEASE_TIMOUT_SEC, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.info("ZombieKiller InterruptedException (" + objectLogId + ")", e);
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					logger.info("ZombieKiller ExecutionException (" + objectLogId + ")", e);
				} catch (TimeoutException e) {
					logger.info("ZombieKiller TimeoutException ({}) {}sec", objectLogId, CONN_RELEASE_TIMOUT_SEC);
				} finally {
					if (System.currentTimeMillis() - startTs > 1000) {
						logger.info("JavaProxyAgent ({}) _release() time={}ms", objectLogId, System.currentTimeMillis() - startTs);
					} else {
						logger.debug("JavaProxyAgent ({}) _release() time={}ms", objectLogId, System.currentTimeMillis() - startTs);
					}
				}
			});
		}

		@Override
		public boolean validateObject(PooledObject<JavaProxyAgent> p) {
			// check connection timeToLive
			if (connParams.connectionTTLSec > 0) {
				if (System.currentTimeMillis() - p.getCreateTime() > connParams.connectionTTLSec * 1000L)
					return false;
			}
			return super.validateObject(p);
		}


	}

	private static ThreadPoolExecutor createExecutor(int workerCount, int queueSize, String prefix) {
		ThreadFactory threadFactory = new ThreadFactory() {
			private int counter = 0;
			@Override
			public Thread newThread(Runnable runnable) {
				return new Thread(runnable, prefix + ++counter);
			}
		};
		ThreadPoolExecutor executor = new ThreadPoolExecutor(workerCount, workerCount,
				0, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(queueSize), threadFactory);
		return executor;
	}

}
