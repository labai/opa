package com.github.labai.opa;

import com.github.labai.opa.IntTests.AblIntTestBase;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class I01HelloTest extends AblIntTestBase{

	@OpaProc(proc="jpx/test/opa/test_01_hello.p")
	public static class HelloWorldOpp {
		@OpaParam
		private String name;

		@OpaParam(io= IoDir.OUT)
		private String answer;
	}

	//@Ignore
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

		@OpaParam(io= IoDir.OUT)
		private String answer;
	}

	//@Ignore
	@Test
	public void testHello2WithProcName() throws OpaException {
		HelloWorld2Opp opp = new HelloWorld2Opp();
		opp.name = "Augustus";
		server.runProc(opp, "jpx/test/opa/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);
	}

	@OpaProc(proc="SOME_NOT_EXISTING_PROC")
	public static class HelloWorld3Opp {
		@OpaParam
		private String name;

		@OpaParam(io= IoDir.OUT)
		private String answer;
	}

	//@Ignore
	@Test
	public void testHello3WithProcName() throws OpaException {
		HelloWorld3Opp opp = new HelloWorld3Opp();
		opp.name = "Augustus";
		server.runProc(opp, "jpx/test/opa/test_01_hello.p");
		Assert.assertEquals("Hello, Augustus!", opp.answer);
	}

}

