package com.github.labai.opa.sys;

import com.github.labai.opa.OpaServer.SessionModel;
import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.javaproxy.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Augustus
 *         created on 2015.10.13
 *
 * For internal usage only (is not part of api)
 *
 */
class Pool {
	private final static Logger logger = LoggerFactory.getLogger(Pool.class);

	static class ConnParams {
		final String urlString;
		final String userId;
		final String password;
		final SessionModel sessionModel;

		// config via setters
		int connectionTTLSec = 298; // 4:58
		String sslCertificateStore;
		Boolean sslNoHostVerify;

		// manual connection configurer
		Consumer<Connection> connectionConfigurer;
		// requestId provider (general for connection)
		Supplier<String> requestIdGenerator;


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
		}

		public void setConnectionTTLSec(int connectionTTLSec) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.connectionTTLSec = connectionTTLSec;
		}

		public void setCertificateStore(String psccertsPath) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.sslCertificateStore = psccertsPath;
		}

		public void setNoHostVerify(Boolean value) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.sslNoHostVerify = value;
		}

		public void setConnectionConfigurer(Consumer<Connection> connConfigurer) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.connectionConfigurer = connConfigurer;
		}

		public void setRequestIdGenerator(Supplier<String> requestIdGenerator) {
			ConnParams connParams = ((JpxConnFactory)super.getFactory()).connParams;
			connParams.requestIdGenerator = requestIdGenerator;
		}
	}

	/**
	 * connection factory
	 */
	private static class JpxConnFactory extends BasePooledObjectFactory<JavaProxyAgent> {

		final ConnParams connParams;
		private final ThreadPoolExecutor zombieKiller = createExecutor(2, 200);

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
		public void destroyObject(final PooledObject<JavaProxyAgent> pooledObj) throws Exception {
			zombieKiller.submit(new Runnable() {
				@Override
				public void run() {
					long startTs = System.currentTimeMillis();
					try {
						pooledObj.getObject()._release();
					} catch (Open4GLException e) {
						logger.info("Open4GLException while JavaProxyAgent _release:" + e.getMessage());
						logger.debug("Exception", e);
						// ignore
					} finally {
						if (System.currentTimeMillis() - startTs > 1000) {
							logger.info("JavaProxyAgent _release() took {}ms", System.currentTimeMillis() - startTs);
						}
					}


				}
			});
		}

		@Override
		public boolean validateObject(PooledObject<JavaProxyAgent> p) {
			// check connection timeToLive
			if (connParams.connectionTTLSec > 0) {
				if (System.currentTimeMillis() - p.getCreateTime() > connParams.connectionTTLSec * 1000)
					return false;
			}
			return super.validateObject(p);
		}


	}

	private static ThreadPoolExecutor createExecutor(int workerCount, int queueSize) {
		ThreadFactory threadFactory = new ThreadFactory() {
			private int counter = 0;
			@Override
			public Thread newThread(Runnable runnable) {
				return new Thread(runnable, "OpaZombie-" + ++counter);
			}
		};
		ThreadPoolExecutor executor = new ThreadPoolExecutor(workerCount, workerCount,
				0, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(queueSize), threadFactory);
		return executor;
	}

}
