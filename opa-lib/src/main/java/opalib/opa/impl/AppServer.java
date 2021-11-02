package opalib.opa.impl;

import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.RunTime4GLException;
import com.progress.open4gl.javaproxy.Connection;
import opalib.api.OpaException;
import opalib.opa.OpaServer.RunResult;
import opalib.opa.OpaServer.SessionModel;
import opalib.opa.impl.Exceptions.OpaSessionTimeoutException;
import opalib.opa.impl.Pool.ConnParams;
import opalib.opa.impl.Pool.JpxConnPool;
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
	private final static Logger logger = LoggerFactory.getLogger(ProMap.class);

	static final String PROGRESS_PROPS_KEY_SESSION_MODEL = "PROGRESS.Session.sessionModel";
	static final String PROGRESS_PROPS_KEY_CONTEXT_ID = "PROGRESS.Session.ClientContextID";

	private static final long DEFAULT_WAIT_TIMEOUT_MILIS = 30000L; // 30 s
	private static final int DEFAULT_MAX_CONN = 10;

	private JpxConnPool pool;

	public interface RequestIdProvider {
		String get();
	}

	public interface ConnectionConfigurer {
		void accept(Connection conn);
	}

	public AppServer(String serverUrl, String userName, String password, SessionModel sessionModel) {
		super();
		GenericObjectPoolConfig<JavaProxyAgent> poolConfig = new GenericObjectPoolConfig<>();
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
	// - procName can be null (then proc name must be set in @OpaProc)
	//
	public RunResult runProc(Object opp, String procName, RequestIdProvider requestIdProvider) throws OpaException {
		RunResult result = null;
		boolean sessionExpired = false;
		try {
			result = runJpx(opp, procName, requestIdProvider);
		} catch (OpaSessionTimeoutException e) {
			// session timeout - retry
			sessionExpired = true;
			logger.debug("runProc OpaSessionTimeoutException, will try again: {}", e.getMessage());
		}
		if (sessionExpired)
			result = runJpx(opp, procName, requestIdProvider);
		return result;

	}

	private RunResult runJpx(Object opp, String procName, RequestIdProvider requestIdProvider) {
		JavaProxyAgent jpx;
		Throwable raised = null;
		RunResult result = null;
		boolean destroyAgent = false;
		try {
			jpx = pool.borrowObject();
		} catch (Exception ex) {
			throw new OpaException("Cannot get connection from pool", ex);
		}

		try {
			result = jpx.runProc(opp, procName, requestIdProvider);
		} catch (RunTime4GLException e) {
			destroyAgent = true;
			raised = new OpaException("4GL runtime error: " + e.getMessage(), e);
		} catch (SQLException e) {
			destroyAgent = true;
			raised = new OpaException("4GL ResultSet error: " + e.getMessage(), e);
		} catch (Open4GLException e) {
			// can happen due to network problems
			destroyAgent = true;
			raised = new OpaException("Open4GL error: " + e.getMessage(), e);
		} catch (Throwable e) {
			destroyAgent = true;
			raised = e;
		}

		if (destroyAgent) {
			try {
				pool.invalidateObject(jpx);
			} catch (Exception e) {
				logger.warn("Cannot invalidate connection in pool: " + e.getMessage(), e);
			}
		} else {
			try {
				pool.returnObject(jpx);
			} catch (Exception e) {
				logger.error("Cannot return connection to pool: " + e.getMessage(), e);
			}
		}

		if (raised != null) {
			if (raised instanceof RuntimeException)
				throw (RuntimeException) raised;
			else
				throw new RuntimeException(raised);
		}

		return result;
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
	JpxConnPool getPool() { return pool; }
	void setPool(JpxConnPool pool) { this.pool = pool; }

}
