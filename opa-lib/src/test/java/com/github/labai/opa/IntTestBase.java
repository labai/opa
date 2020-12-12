package com.github.labai.opa;

import com.github.labai.opa.OpaServer.SessionModel;
import org.junit.Assume;
import org.junit.Before;

/**
 * @author Augustus
 * 		   created on 2018.12.22
 */
public class IntTestBase {

	protected OpaServer server;

	@Before
	public void init() {
		Assume.assumeTrue("Are integration tests enabled?", TestParams.INT_TESTS_ENABLED);
		server = new OpaServer(TestParams.APP_SERVER, TestParams.APP_USER, TestParams.APP_USER, SessionModel.STATE_FREE);
	}

	protected void log(String msg) {
		System.out.println(msg);
	}
}
