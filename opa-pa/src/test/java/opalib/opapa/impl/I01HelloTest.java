package opalib.opapa.impl;

import opalib.api.IoDir;
import opalib.api.OpaParam;
import opalib.api.OpaException;
import opalib.api.OpaProc;
import org.junit.Assert;
import org.junit.Test;


public class I01HelloTest extends IntTestBase {

	@OpaProc(proc="opalib/opatest/test_01_hello.p")
	public static class HelloWorldOpp {
		@OpaParam
		private String name;

		@OpaParam(io = IoDir.OUT)
		private String answer;
	}

	@Test
	public void testHello() throws OpaException {
		HelloWorldOpp opp = new HelloWorldOpp();
		opp.name = "Augustus";
		server.runProc(opp);
		Assert.assertEquals("Hello, Augustus!", opp.answer);
		//log("Hello ok");
	}


	@OpaProc
	public static class HelloWorld2Opp {
		@OpaParam
		private String name;

		@OpaParam(io = IoDir.OUT)
		private String answer;
	}

	@Test
	public void testHello2WithProcName() throws OpaException {
		HelloWorld2Opp opp = new HelloWorld2Opp();
		opp.name = "Augustus";
		server.runProc(opp, "opalib/opatest/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);
	}

	@OpaProc(proc="SOME_NOT_EXISTING_PROC")
	public static class HelloWorld3Opp {
		@OpaParam
		private String name;

		@OpaParam(io = IoDir.OUT)
		private String answer;
	}

	@Test
	public void testHello3WithProcName() throws OpaException {
		HelloWorld3Opp opp = new HelloWorld3Opp();
		opp.name = "Augustus";
		server.runProc(opp, "opalib/opatest/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);
	}

}

