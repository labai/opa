package com.github.labai.opa.sys;

import com.github.labai.opa.sys.Exceptions.OpaSessionTimeoutException;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;
import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.RunTimeProperties;
import com.progress.open4gl.javaproxy.Connection;
import com.github.labai.opa.sys.Pool.ConnParams;

import java.sql.SQLException;

/**
 * For internal usage only (is not part of api)
 *
 * Will be created in connection pool
 *
 * @author Augustus
 */
class JavaProxyAgent {
	private JavaProxyImpl impl;

	JavaProxyAgent(ConnParams connParams) throws Open4GLException {
		// we must do this here before we attempt to create the appobject
		//if (RunTimeProperties.getDynamicApiVersion() != PROXY_VER)
		//	throw new Open4GLException(m_wrongProxyVer, null);
		Connection connection = new Connection(connParams.urlString, connParams.userId, connParams.password, "" /*appServerInfo*/);
		if (connParams.sessionModel != null)
			connection.setIntProperty(AppServer.PROGRESS_PROPS_KEY_SESSION_MODEL, connParams.sessionModel.progressId);
		impl = new JavaProxyImpl("OpaJavaProxy", connection, RunTimeProperties.tracer);
		connection.releaseConnection();

	}

	void _release() throws Open4GLException {
		impl._release();
	}

	String runProc(Object opp, String procName) throws Open4GLException, SQLException, OpaStructureException, OpaSessionTimeoutException {
		return impl.runProc(opp, procName);
	}
}

