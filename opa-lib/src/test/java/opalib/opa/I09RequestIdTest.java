package opalib.opa;

import opalib.api.IoDir;
import opalib.api.OpaParam;
import opalib.api.OpaProc;

/**
 * Created by Augustus on 2019.11.19.
 */
public class I09RequestIdTest extends IntTestBase {

	@OpaProc(proc="tests/opalib/test_09_requestid.p")
	public static class T09RequestIdOpp {

		@OpaParam(io = IoDir.OUT)
		public String requestId;
		@OpaParam(io = IoDir.OUT)
		public String contextId;

	}
/*
	@Test
	public void testRequestId() throws OpaException {
		server.setRequestIdGenerator(null);

		T09RequestIdOpp opp = new T09RequestIdOpp();

		server.runProc(opp);

		log("RequestId:" + opp.requestId + " ContextId:" + opp.contextId);
		boolean isOrig = asList("<REQ|", "ROOT:").contains(opp.requestId.substring(0,5));
		assertTrue("Should use standard from lib", isOrig);

		server.runProc(opp, () -> "abra1");
		log("RequestId:" + opp.requestId + " ContextId:" + opp.contextId);
		assertEquals("Should use provided in run", "abra1", opp.contextId);

		// ! opp.requestId is same for Classic, but different for Pacific
	}


	@Test
	public void testRequestIdfFromGenerator() throws OpaException {
		OpaServer server2 = IntTestUtils.createOpaServer(TestParams.APP_SERVER);
		server2.setRequestIdGenerator(() -> "abra2");

		T09RequestIdOpp opp = new T09RequestIdOpp();

		server2.runProc(opp);

		server.setRequestIdGenerator(() -> "abra2");
		server.runProc(opp);
		assertEquals("Should use from configuration", "abra2", opp.contextId);

		server.runProc(opp, () -> "abra1");
		assertEquals("Should use provided in run", "abra1", opp.contextId);

	}*/


}
