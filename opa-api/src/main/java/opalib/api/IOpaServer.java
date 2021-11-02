package opalib.api;

import java.util.function.Supplier;

/**
 * @author Augustus
 * created on 2021.06.17
 */
public interface IOpaServer {


	void runProc(Object opp);

	void runProc(Object opp, Supplier<String> requestIdProvider);

	void runProc(Object opp, String procName);

	void runProc(Object opp, String procName, Supplier<String> requestIdProvider);

    void shutdown();

	void setMaxPoolSize (int maxConnections);

	// void setConnectionTimeout(long timeoutMs);
	// void setConnectionTTLSec(int ttlInSec);
	// void setCertificateStore(String psccertsPath);
	// void setNoHostVerify(Boolean value);
	// void setConnectionConfigurer(Consumer<Connection> connConfigurer)
	// void setRequestIdGenerator(Supplier<String> requestIdGenerator);

}
