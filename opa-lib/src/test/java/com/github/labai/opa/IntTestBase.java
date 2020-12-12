package com.github.labai.opa;

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
		server = IntTestUtils.createOpaServer(TestParams.APP_SERVER);
	}

	protected void log(String msg) {
		System.out.println(msg);
	}
}
