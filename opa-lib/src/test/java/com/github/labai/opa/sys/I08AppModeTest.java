package com.github.labai.opa.sys;

import com.github.labai.opa.IntTestUtils;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.OpaServer;
import com.github.labai.opa.OpaServer.SessionModel;
import com.github.labai.opa.TestParams;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Augustus
 *         created on 2016.08.31
 *
 * test, can exists few appServers with different sessionModel
 */
public class I08AppModeTest {

	@Before
	public void init() {
		Assume.assumeTrue("Is integrations test enabled?", TestParams.INT_TESTS_ENABLED);
	}

	@OpaProc
	public static class HelloWorldOpp {
		@OpaParam
		private String name;
		@OpaParam(io= IoDir.OUT)
		private String answer;
	}

	@Test
	@Ignore // need STATELESS server..
	public void testDifferentServerModes() throws Exception {
		OpaServer asStatefree = IntTestUtils.createOpaServer(TestParams.APP_SERVER, SessionModel.STATE_FREE);
		OpaServer asStateless = IntTestUtils.createOpaServer(TestParams.APP_SERVER_STATELESS, SessionModel.STATELESS);

		HelloWorldOpp opp;
		opp = new HelloWorldOpp();
		opp.name = "Augustus";
		asStatefree.runProc(opp, "tests/opalib/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);

		opp = new HelloWorldOpp();
		opp.name = "Augustus";
		asStateless.runProc(opp, "tests/opalib/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);

	}



}
