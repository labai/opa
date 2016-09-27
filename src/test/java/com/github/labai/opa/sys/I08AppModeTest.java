package com.github.labai.opa.sys;

import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.OpaServer;
import com.github.labai.opa.OpaServer.SessionModel;
import com.github.labai.opa.TestParams;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Augustus Mickus
 *         created on 2016.08.31
 *
 * test, can exists few appServers with different sessionModel
 */
//@Ignore
public class I08AppModeTest {

	@Before
	public void init() {
		Assume.assumeTrue("Is integrations test enabled?", TestParams.INT_TESTS_ENABLED);
	}


	@OpaProc
	public static class HelloWorld2Opp {
		@OpaParam
		private String name;
		@OpaParam(io = IoDir.OUT)
		private String answer;
	}
	@Test
	public void testDifferentServerModes() throws Exception {
		OpaServer asStatefree = new OpaServer(TestParams.APP_SERVER, TestParams.APP_USER, TestParams.APP_PASSWORD, SessionModel.STATE_FREE);
		OpaServer asStateless = new OpaServer(TestParams.APP_SERVER_STATELESS, TestParams.APP_USER, TestParams.APP_PASSWORD, SessionModel.STATELESS);

		HelloWorld2Opp opp;
		opp = new HelloWorld2Opp();
		opp.name = "Augustus";
		asStatefree.runProc(opp, "jpx/test/opa/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);

		opp = new HelloWorld2Opp();
		opp.name = "Augustus";
		asStateless.runProc(opp, "jpx/test/opa/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);

	}
}
