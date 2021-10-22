package opalib.opa;

import opalib.opa.impl.AppServer;
import opalib.opa.impl.DateConvJava8Ext;
import com.progress.open4gl.javaproxy.Connection;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Augustus
 */
public class OpaServer {

	private final AppServer appServer;

	static {
		DateConvJava8Ext.init();
	}

	public enum SessionModel {
		STATELESS (0),
		STATE_FREE (1);
		public final int progressId;
		SessionModel(int id) {
			this.progressId = id;
		}
	}

	/**
	 * additional info about call
	*/
	public interface RunResult {
		String returnValue();
		String requestId();
	}

	public OpaServer(String serverUrl, String userName, String password, SessionModel sessionModel) {
		appServer = new AppServer(serverUrl, userName, password, sessionModel);
	}

	public void shutdown() {
		appServer.shutdown();
	}

	/**
	 *  Run OpenEdge procedure on AppServer.
	 *  opp must describe OpenEdge procedure parameters using @OpaProc and @OpaParam annotations.
	 *  Procedure name will be in opp annotation, e.g. @OpaProc(proc="proc.p").
	 */
	public void runProc(Object opp) throws OpaException {
		appServer.runProc(opp, null, null);
	}

	public void runProc(Object opp, Supplier<String> requestIdProvider) throws OpaException {
		appServer.runProc(opp, null, requestIdProvider::get);
	}

	/**
	 *  Run OpenEdge procedure on AppServer.
	 *  opp must describe OpenEdge procedure parameters using @OpaProc and @OpaParam annotations
	 */
	public void runProc(Object opp, String procName) throws OpaException {
		appServer.runProc(opp, procName, null);
	}
	public void runProc(Object opp, String procName, Supplier<String> requestIdProvider) throws OpaException {
		appServer.runProc(opp, procName, requestIdProvider::get);
	}

	/** Set maximum pool size (maximum connections to OpenEdge AppServer). Default is 10 */
	public void setMaxPoolSize (int maxConnections) {
		appServer.setMaxPoolSize(maxConnections);
	}

	/** Set new connection create timeout in milliseconds. Default is 30 s. */
	public void setConnectionTimeout(long timeoutMs) {
		appServer.setConnectionTimeout(timeoutMs);
	}

	/** Set connection time-to-live in seconds. Default is 238s (3:58) */
	public void setConnectionTTLSec(int ttlInSec) {
		appServer.setConnectionTTLSec(ttlInSec);
	}

	/** Set certificate (psccerts.zip) path for ssl */
	public void setCertificateStore(String psccertsPath) {
		appServer.setCertificateStore(psccertsPath);
	}

	/** set flag to not to verify hosts for ssl */
	public void setNoHostVerify(Boolean value) {
		appServer.setNoHostVerify(value);
	}

	/** Set connection configuration provider - for manual configuration */
	public void setConnectionConfigurer(Consumer<Connection> connConfigurer) {
		appServer.setConnectionConfigurer(connConfigurer::accept);
	}

	public void setRequestIdGenerator(Supplier<String> requestIdGenerator) {
		appServer.setRequestIdGenerator(requestIdGenerator::get);
	}

}
