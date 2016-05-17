package com.github.labai.opa;

import com.github.labai.opa.IntTests.AblIntTestBase;
import com.github.labai.opa.IntTests.SampleTable;
import com.github.labai.opa.Opa.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;


@Ignore
public class I05TableHandleTest extends AblIntTestBase{

	@OpaProc(proc="jpx/test/opa/test_05_table_handle.p")
	public static class TableHandleOpp {
		@OpaParam
		public String someId;

		@OpaParam(table=SampleTable.class, io= IoDir.OUT)
		public List<SampleTable> tt;

		@OpaParam(io=IoDir.OUT)
		public String errorCode;

		@OpaParam(io=IoDir.OUT)
		public String errorMessage;
	}

	//@Ignore
	@Test
	public void testTableHandle() throws OpaException {

		TableHandleOpp opp = new TableHandleOpp();
		opp.someId = "00000038";
		server.runProc(opp);

		String s0 = "Character value|101234|-123456789012|1.23|2014-02-15|true|2014-02-15T00:00:00|2014-02-15T00:00:00.000+0200|null";
		String s1 = "string|100001|1|1.1|2014-01-15|true|2014-01-15T00:00:00|2014-01-15T00:00:00.000+0200|null";
		String s2 = "null|null|null|null|null|null|null|null|null";

		Assert.assertEquals(s0, opp.tt.get(0).toString());
		Assert.assertEquals(s1, opp.tt.get(1).toString());
		Assert.assertEquals(s2, opp.tt.get(2).toString());

		//log("TableOut ok");
	}

	///////////////////////////////////////////////////////////////////
	// Test
	//  a) @OpaField(name="...")
	//  b) different fields between OE and Java
	///////////////////////////////////////////////////////////////////

	@OpaTable
	private static class SampleTable2 {
		@OpaField
		public String charVal;
		@OpaField (name = "intVal")
		public Integer intValDiffFromOE; // must use field name from @OpaField(name)
		@OpaField
		public String fieldDoesNotExistsInOE = "AA"; // must ignore

		@Override
		public String toString() {
			return this.charVal + "|" + this.intValDiffFromOE + "|" + this.fieldDoesNotExistsInOE;
		}
	}


	@OpaProc(proc="jpx/test/opa/test_05_table_handle.p")
	private static class TableHandle2Opp {
		@OpaParam
		public String someId;

		@OpaParam(table=SampleTable2.class, io= IoDir.OUT)
		public List<SampleTable2> tt;

		@OpaParam(io=IoDir.OUT)
		public String errorCode;

		@OpaParam(io=IoDir.OUT)
		public String errorMessage;
	}

	/**
	 * test, when table field count mismatched
	 */
	@Test
	public void testTableHandleMismatched() throws OpaException {

		TableHandle2Opp opp = new TableHandle2Opp();
		opp.someId = "00000038";
		server.runProc(opp);

		String s0 = "Character value|1234|AA";
		String s1 = "string|1|AA";
		String s2 = "null|null|AA";

		Assert.assertEquals(s0, opp.tt.get(0).toString());
		Assert.assertEquals(s1, opp.tt.get(1).toString());
		Assert.assertEquals(s2, opp.tt.get(2).toString());

		//log("TableOut ok");
	}


}
