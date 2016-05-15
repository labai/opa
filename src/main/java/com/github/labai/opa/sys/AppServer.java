package com.github.labai.opa.sys;

import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.RunTime4GLException;
import com.github.labai.opa.Opa;
import com.github.labai.opa.OpaException;
import com.github.labai.opa.sys.Exceptions.OpaSessionTimeoutException;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;
import com.github.labai.opa.sys.Pool.ConnParams;
import com.github.labai.opa.sys.Pool.JpxConnPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * For internal usage only (is not part of api)
 *
 * @author Augustas Mickus
 */
public class AppServer {
	private final static Logger logger = LoggerFactory.getLogger(Opa.class);

	private static final String PROGRESS_PROPS_KEY_SESSION_MODEL = "PROGRESS.Session.sessionModel";

	private static final long DEFAULT_WAIT_TIMEOUT_MILIS = 30000L; // 30 s
	private static final int DEFAULT_MAX_CONN = 10;

	private JpxConnPool pool;

	public AppServer(String serverUrl, String userName, String password) {
		super();
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxWaitMillis(DEFAULT_WAIT_TIMEOUT_MILIS);
		poolConfig.setMaxTotal(DEFAULT_MAX_CONN);
		ConnParams connParams = new ConnParams(serverUrl, userName, password);
		pool = new JpxConnPool(connParams, poolConfig);
	}

	// Remarks:
	// - procName can be null (then proc name must be set in @OpaProc))
	//
	public void runProc(Object opp, String procName) throws OpaException {
		JavaProxyAgent jpx;
		JavaProxyAgent jpx2 = null;
		try {
			jpx = (JavaProxyAgent) pool.borrowObject();
		} catch (Exception ex) {
			throw new OpaException("Cannot get connection from pool", ex);
		}

		try {
			try {
				jpx.runProc(opp, procName);
				return;
			} catch (OpaStructureException e) {
				throw e;
			} catch (RunTime4GLException e) {
				throw new OpaException("4GL runtime error: " + e.getMessage(), e);
			} catch (SQLException e) {
				throw new OpaException("4GL ResultSet error: " + e.getMessage(), e);
			} catch (Open4GLException e) {
				// can be connection problems - retry
			} catch (OpaSessionTimeoutException e) {
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
				jpx2 = (JavaProxyAgent) pool.borrowObject();
			} catch (Exception e) {
				throw new OpaException("Cannot restart connection in pool", e);
			}

			try {
				jpx2.runProc(opp, procName);
				return;
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

	// warning: static
	public static void setSessionModel(int sessionModel) {
		com.progress.open4gl.RunTimeProperties.setIntProperty(PROGRESS_PROPS_KEY_SESSION_MODEL, sessionModel);
	}

	public void setMaxPoolSize(int maxConnections) {
		pool.setMaxTotal(maxConnections);
	}

	// 0 = no timeout
	public void setConnectionTimeout(long timeoutMilliseconds) {
		if (timeoutMilliseconds == 0)
			timeoutMilliseconds = Long.MAX_VALUE;
		pool.setMaxWaitMillis(timeoutMilliseconds);
	}

	/** package scoped. For testing only */
	JpxConnPool getPool() {
		return pool;
	}


}
