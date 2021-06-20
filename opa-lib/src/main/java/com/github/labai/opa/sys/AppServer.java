package com.github.labai.opa.sys;

import com.github.labai.opa.Opa;
import com.github.labai.opa.OpaException;
import com.github.labai.opa.OpaServer.RunResult;
import com.github.labai.opa.OpaServer.SessionModel;
import com.github.labai.opa.sys.Exceptions.OpaSessionTimeoutException;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;
import com.github.labai.opa.sys.Pool.ConnParams;
import com.github.labai.opa.sys.Pool.JpxConnPool;
import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.RunTime4GLException;
import com.progress.open4gl.javaproxy.Connection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * @author Augustus
 *
 * For internal usage only (is not part of api)
 *
 */
public class AppServer {
	private final static Logger logger = LoggerFactory.getLogger(Opa.class);

	static final String PROGRESS_PROPS_KEY_SESSION_MODEL = "PROGRESS.Session.sessionModel";
	static final String PROGRESS_PROPS_KEY_CONTEXT_ID = "PROGRESS.Session.ClientContextID";

	private static final long DEFAULT_WAIT_TIMEOUT_MILIS = 30000L; // 30 s
	private static final int DEFAULT_MAX_CONN = 10;

	private final JpxConnPool pool;

	public interface RequestIdProvider {
		String get();
	}

	public interface ConnectionConfigurer {
		void accept(Connection conn);
	}

	public AppServer(String serverUrl, String userName, String password, SessionModel sessionModel) {
		super();
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxWaitMillis(DEFAULT_WAIT_TIMEOUT_MILIS);
		poolConfig.setMaxTotal(DEFAULT_MAX_CONN);
		ConnParams connParams = new ConnParams(serverUrl, userName, password, sessionModel);
		pool = new JpxConnPool(connParams, poolConfig);
	}

	public void shutdown() {
		pool.shutdown();
		logger.debug("Finished opaServer shutdown");
	}

	// Remarks:
	// - procName can be null (then proc name must be set in @OpaProc))
	//
	public RunResult runProc(Object opp, String procName, RequestIdProvider requestIdProvider) throws OpaException {
		JavaProxyAgent jpx;
		JavaProxyAgent jpx2 = null;
		try {
			jpx = pool.borrowObject();
		} catch (Exception ex) {
			throw new OpaException("Cannot get connection from pool", ex);
		}

		try {
			try {
				return jpx.runProc(opp, procName, requestIdProvider);
			} catch (OpaStructureException e) {
				throw e;
			} catch (RunTime4GLException e) {
				throw new OpaException("4GL runtime error: " + e.getMessage(), e);
			} catch (SQLException e) {
				throw new OpaException("4GL ResultSet error: " + e.getMessage(), e);
			} catch (Open4GLException e) {
				// can happen due to network problems
				throw new OpaException("Open4GL error: " + e.getMessage(), e);
			} catch (OpaSessionTimeoutException e) {
				logger.debug("runProc(1) OpaSessionTimeoutException, will try again: {}", e.getMessage());
				// session timeout - retry
			}

			try {
				pool.invalidateObject(jpx);
				jpx = null;
			} catch (Exception e) {
				logger.warn("Cannot invalidate connection in pool: " + e.getMessage(), e);
			}

			// retry
			try {
				jpx2 = pool.borrowObject();
			} catch (Exception e) {
				throw new OpaException("Cannot restart connection in pool", e);
			}

			try {
				return jpx2.runProc(opp, procName, requestIdProvider);
			} catch (RunTime4GLException e) {
				throw new OpaException("4GL runtime error: " + e.getMessage(), e);
			} catch (SQLException e) {
				throw new OpaException("4GL ResultSet error: " + e.getMessage(), e);
			} catch (Open4GLException e) {
				throw new OpaException("Recurrent 4GL execution error: " + e.getMessage(), e);
			}

		}
		finally {
			if (jpx != null) {
				try {
					pool.returnObject(jpx);
				} catch (Exception e) {
					logger.error("Cannot return connection to pool: " + e.getMessage(), e);
				}
			}
			if (jpx2 != null) {
				try {
					pool.returnObject(jpx2);
				} catch (Exception e) {
					logger.error("Cannot return connection(2) to pool: " + e.getMessage(), e);
				}
			}
		}
	}


	public void setMaxPoolSize(int maxConnections) {
		pool.setMaxTotal(maxConnections);
	}

	public void setConnectionTTLSec(int ttlInSec) {
		pool.setConnectionTTLSec(ttlInSec);
	}

	public void setCertificateStore(String psccertsPath) {
		pool.setCertificateStore(psccertsPath);
	}

	public void setNoHostVerify(Boolean value) {
		pool.setNoHostVerify(value);
	}


	// 0 = no timeout
	public void setConnectionTimeout(long timeoutMilliseconds) {
		if (timeoutMilliseconds == 0)
			timeoutMilliseconds = Long.MAX_VALUE;
		pool.setMaxWaitMillis(timeoutMilliseconds);
	}

	// manual connection configuration (function)
	public void setConnectionConfigurer(ConnectionConfigurer connConfigurer) {
		pool.setConnectionConfigurer(connConfigurer);
	}

	public void setRequestIdGenerator(RequestIdProvider requestIdGenerator) {
		pool.setRequestIdGenerator(requestIdGenerator);
	}

	/** package scoped. For testing only */
	JpxConnPool getPool() {
		return pool;
	}


}
