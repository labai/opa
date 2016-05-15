package com.github.labai.opa.sys;

import com.progress.open4gl.Open4GLException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/*
 * For internal usage only (is not part of api)
 *
 * @author Augustus Mickus
 *         created on 2015.10.13
 */
class Pool {

	static class ConnParams {
		final String urlString;
		final String userId;
		final String password;

		public ConnParams(String urlString, String userId, String password) {
			this.password = password;
			this.userId = userId;
			this.urlString = urlString;
		}

		@Override
		public String toString() {
			return "Conn{url='" + urlString + "',userId=" + userId + ",password=*****}";
		}
	}

	/**
	 * connection pool
	 */
	public static class JpxConnPool extends GenericObjectPool<JavaProxyAgent> {
		public JpxConnPool(ConnParams connParams, GenericObjectPoolConfig poolConfig) {
			super(new JpxConnFactory(connParams), poolConfig);
		}
	}

	/**
	 * connection factory
	 */
	private static class JpxConnFactory extends BasePooledObjectFactory<JavaProxyAgent> {

		ConnParams connParams;

		public JpxConnFactory(ConnParams connParams) {
			this.connParams = connParams;
		}

		@Override
		public JavaProxyAgent create() throws Exception {
			return new JavaProxyAgent(connParams.urlString, connParams.userId, connParams.password, "");
		}

		@Override
		public PooledObject<JavaProxyAgent> wrap(JavaProxyAgent javaProxyAgent) {
			return new DefaultPooledObject<JavaProxyAgent>(javaProxyAgent);
		}

		@Override
		public void destroyObject(PooledObject<JavaProxyAgent> p) throws Exception {
			try {
				p.getObject()._release();
			} catch (Open4GLException e) {
				// ignore
			}
		}
	}



}
