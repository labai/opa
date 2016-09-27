package com.github.labai.opa;

import com.github.labai.opa.sys.AppServer;

/**
 * @author Augustus
 */
public class OpaServer {

	private AppServer appServer;

	public enum SessionModel {
		STATELESS (0),
		STATE_FREE (1);
		public final int progressId;
		private SessionModel(int id) {
			this.progressId = id;
		}
	}

	public OpaServer(String serverUrl, String userName, String password, SessionModel sessionModel) {
		appServer = new AppServer(serverUrl, userName, password, sessionModel);
	}

	/** Run OpenEdge procedure on AppServer.
	 *  opp must describe OpenEdge procedure parameters using @OpaProc and @OpaParam annotations.
	 *  Procedure name will be in opp annotation, e.g. @OpaProc(proc="proc.p").
	 */
	public void runProc(Object opp) throws OpaException {
		appServer.runProc(opp, null);
	}

	/** Run OpenEdge procedure on AppServer.
	 *  opp must describe OpenEdge procedure parameters using @OpaProc and @OpaParam annotations
	 */
	public void runProc(Object opp, String procName) throws OpaException {
		appServer.runProc(opp, procName);
	}

	/** Set maximum pool size (maximum connections to OpenEdge AppServer). Default is 10 */
	public void setMaxPoolSize (int maxConnections) {
		appServer.setMaxPoolSize(maxConnections);
	}

	/** Set connection timeout in milliseconds. Default is 30 s */
	public void setConnectionTimeout(long timeoutMs) {
		appServer.setConnectionTimeout(timeoutMs);
	}

}
