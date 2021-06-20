package opalib.opapa;

import opalib.api.IOpaServer;
import opalib.api.OpaException;
import opalib.opapa.impl.AppServer;

import java.util.function.Supplier;

/**
 * @author Augustus
 */
public class OpaServer implements IOpaServer {
	private final AppServer appServer;

	public OpaServer(String baseUrl) {
		appServer = new AppServer(baseUrl);
	}

	@Override
	public void runProc(Object opp) {
		appServer.runProc(opp, null);
	}

	@Override
	public void runProc(Object opp, Supplier<String> requestIdProvider) {

	}

	public void runProc(Object opp, String proc) throws OpaException {
		appServer.runProc(opp, proc);

	}

	@Override
	public void runProc(Object opp, String procName, Supplier<String> requestIdProvider) {

	}

	@Override
	public void shutdown() {
		appServer.shutdown();
	}

	@Override
	public void setMaxPoolSize(int maxConnections) {
		appServer.setMaxPoolSize(maxConnections);
	}

}
