package com.github.labai.opa.sys;

import com.github.labai.opa.OpaServer.SessionModel;
import com.progress.open4gl.*;
import com.progress.open4gl.javaproxy.Connection;
import com.github.labai.opa.sys.Exceptions.OpaSessionTimeoutException;
import com.github.labai.opa.sys.Exceptions.OpaStructureException;

import java.sql.SQLException;

/**
 * For internal usage only (is not part of api)
 * 
 * Will be created in connection pool
 *
 * @author Augustus
 */
public class JavaProxyAgent implements SDOFactory {
	public JavaProxyImpl impl;

	public JavaProxyAgent(String urlString, String userId, String password, String appServerInfo, SessionModel sessionModel) throws Open4GLException{
		// we must do this here before we attempt to create the appobject
		//if (RunTimeProperties.getDynamicApiVersion() != PROXY_VER)
		//	throw new Open4GLException(m_wrongProxyVer, null);

		Connection connection = new Connection(urlString, userId, password, appServerInfo);
		if (sessionModel != null)
			connection.setIntProperty(AppServer.PROGRESS_PROPS_KEY_SESSION_MODEL, sessionModel.progressId);
		impl = new JavaProxyImpl("OpaJavaProxy", connection, RunTimeProperties.tracer);
		connection.releaseConnection();
	}

	public void _release() throws Open4GLException, SystemErrorException {
		impl._release();
	}

	//@Override
	public SDOInterface _createSDOProcObject(String s) throws Open4GLException {
		return impl._createSDOProcObject(s);
	}

	String runProc(Object opp, String procName) throws Open4GLException, SQLException, OpaStructureException, OpaSessionTimeoutException {
		return impl.runProc(opp, procName);
	}
}

