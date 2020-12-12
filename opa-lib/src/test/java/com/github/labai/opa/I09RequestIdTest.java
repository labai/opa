package com.github.labai.opa;

import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.OpaServer.SessionModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Augustus on 2019.11.19.
 */
public class I09RequestIdTest extends IntTestBase {

	@OpaProc(proc="jpx/test/opa/test_09_requestid.p")
	public static class T09RequestIdOpp {

		@OpaParam(io = IoDir.OUT)
		public String requestId;

	}

	@Test
	public void testRequestId() throws OpaException {
		server.setRequestIdGenerator(null);

		T09RequestIdOpp opp = new T09RequestIdOpp();

		server.runProc(opp);

		assertEquals("Should use standard from lib", "<REQ|", opp.requestId.substring(0,5));
		log("RequestId:" + opp.requestId);

		server.runProc(opp, () -> "abra1");
		assertEquals("Should use provided in run", "abra1", opp.requestId);

	}


	@Test
	public void testRequestIdfFromGenerator() throws OpaException {
		OpaServer server2 = new OpaServer(TestParams.APP_SERVER, TestParams.APP_USER, TestParams.APP_USER, SessionModel.STATE_FREE);
		server2.setRequestIdGenerator(() -> "abra2");

		T09RequestIdOpp opp = new T09RequestIdOpp();

		server2.runProc(opp);

		server.setRequestIdGenerator(() -> "abra2");
		server.runProc(opp);
		assertEquals("Should use from configuration", "abra2", opp.requestId);

		server.runProc(opp, () -> "abra1");
		assertEquals("Should use provided in run", "abra1", opp.requestId);

	}


}
