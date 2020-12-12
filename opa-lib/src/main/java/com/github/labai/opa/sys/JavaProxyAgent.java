package com.github.labai.opa.sys;

import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.RunTimeProperties;
import com.progress.open4gl.javaproxy.Connection;
import com.github.labai.opa.sys.Exceptions.OpaSessionTimeoutException;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;
import com.github.labai.opa.sys.Pool.ConnParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * @author Augustus
 *
 * For internal usage only (is not part of api)
 *
 * Will be created in connection pool
 *
 */
class JavaProxyAgent {
	private final static Logger logger = LoggerFactory.getLogger(JavaProxyAgent.class);
	private JavaProxyImpl impl;

	JavaProxyAgent(ConnParams connParams) throws Open4GLException {
		// we must do this here before we attempt to create the appobject
		//if (RunTimeProperties.getDynamicApiVersion() != PROXY_VER)
		//	throw new Open4GLException(m_wrongProxyVer, null);
		Connection connection = new Connection(connParams.urlString, connParams.userId, connParams.password, "" /*appServerInfo*/);

		if (connParams.sessionModel != null) {
			connection.setIntProperty(AppServer.PROGRESS_PROPS_KEY_SESSION_MODEL, connParams.sessionModel.progressId);
		}

		if (connParams.sslCertificateStore != null && !"".equals(connParams.sslCertificateStore)) {
			connection.setCertificateStore(connParams.sslCertificateStore);
		}

		if (connParams.sslNoHostVerify != null) {
			connection.setNoHostVerify(connParams.sslNoHostVerify);
		}

		if (connParams.connectionConfigurer != null) {
			try {
				connParams.connectionConfigurer.accept(connection);
			} catch (Throwable e) {
				logger.warn("Exception while calling manual connection configuration", e);
			}
		}
		impl = new JavaProxyImpl("OpaJavaProxy", connection, RunTimeProperties.tracer);
		connection.releaseConnection();

		if (connParams.requestIdGenerator != null) {
			impl.setRequestIdGenerator(connParams.requestIdGenerator);
		}

	}

	void _release() throws Open4GLException {
		impl._release();
	}

	String runProc(Object opp, String procName, Supplier<String> requestIdProvider) throws Open4GLException, SQLException, OpaStructureException, OpaSessionTimeoutException {
		return impl.runProc(opp, procName, requestIdProvider);
	}
}

