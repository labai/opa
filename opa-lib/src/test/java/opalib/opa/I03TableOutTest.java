package opalib.opa;

import com.progress.open4gl.Rowid;
import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaException;
import opalib.api.OpaField;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import opalib.api.OpaTable;
import opalib.opa.IntTestUtils.SampleTable;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class I03TableOutTest extends IntTestBase {

	@OpaProc(proc = "tests/opalib/test_03_table_out.p")
	private static class TableOutOpp {
		@OpaParam
		private String someId;

		@OpaParam(table = SampleTable.class, io = IoDir.OUT)
		private List<SampleTable> tt;

		@OpaParam(io = IoDir.OUT)
		private String errorCode;

		@OpaParam(io = IoDir.OUT)
		private String errorMessage;
	}


	/**
	 * Test output table (with a) some values; b) default values; c) nulls)
	 */
	@Test
	public void testTableOut() throws OpaException {

		TableOutOpp opp = new TableOutOpp();
		opp.someId = "00000038";
		server.runProc(opp);

		String s0 = "Character value|101234|-123456789012|1.23|2014-02-15|true|2014-02-15T00:00:00|2014-02-15T00:00:00.000+0200|null|100";
		String s1 = "null|null|null|null|null|null|null|null|null|101";
		String s2 = "string|100001|1|1.1|2014-01-15|true|2014-01-15T00:00:00|2014-01-15T00:00:00.000+0200|null|102";

		Assert.assertEquals(s0, opp.tt.get(0).toString());
		Assert.assertEquals(s1, opp.tt.get(1).toString());
		Assert.assertEquals(s2, opp.tt.get(2).toString());

		//log("TableOut ok");
	}

	/*
	 * check cases:
	 * 	 a) list for temp-table is provided (must reuse it);
	 * 	 b) list is not empty (must throw exception);
	 */
	@Test(expected = OpaException.class)
	public void testTableOutProvidedList() throws OpaException {

		TableOutOpp opp = new TableOutOpp();
		opp.someId = "00000038";
		opp.tt = new LinkedList<SampleTable>();
		server.runProc(opp);

		List originalList = opp.tt;

		String s0 = "Character value|101234|-123456789012|1.23|2014-02-15|true|2014-02-15T00:00:00|2014-02-15T00:00:00.000+0200|null|100";
		String s1 = "null|null|null|null|null|null|null|null|null|101";
		String s2 = "string|100001|1|1.1|2014-01-15|true|2014-01-15T00:00:00|2014-01-15T00:00:00.000+0200|null|102";

		// for (SampleTable rec: opp.ttout) System.out.println(rec);

		Assert.assertEquals(s0, opp.tt.get(0).toString());
		Assert.assertEquals(s1, opp.tt.get(1).toString());
		Assert.assertEquals(s2, opp.tt.get(2).toString());

		Assert.assertEquals("not-opa-field", opp.tt.get(0).fake);

		Assert.assertTrue("Must remain same list as was before call", opp.tt == originalList);

		// with some items
		opp = new TableOutOpp();
		opp.someId = "00000038";
		opp.tt = new LinkedList<SampleTable>();
		opp.tt.add(new SampleTable());

		server.runProc(opp); // must throw exception
	}

	/**
	 * Test output table with Java primitive values (int, boolean, long)
	 */
	@Test
	public void testTableOutPrimitive() throws OpaException {
		TableOutWithPrimitiveTests.testTableOut(server);
	}

	/**
	 * test, when table field count mismatched
	 * (some WARN message will be written to log)
	 */
	@Test
	public void testTableHandleMismatched() throws OpaException {
		TableOutMismatchFields.testTableOut(server);
	}


	//
	// TableOut various tests
	//


	/**
	 * for primitive fields
	 */
	private static class TableOutWithPrimitiveTests {

		@OpaProc(proc = "tests/opalib/test_03_table_out.p")
		static class TableOutPrimitiveOpp {
			@OpaParam
			public String someId;

			@OpaParam(table = TableOutPrimitive.class, io = IoDir.OUT)
			public List<TableOutPrimitive> tt;

			@OpaParam(io = IoDir.OUT)
			public String errorCode;

			@OpaParam(io = IoDir.OUT)
			public String errorMessage;
		}

		// with primitive data types (int, long, boolean) - null should be converted to 0/false
		@OpaTable(allowOmitOpaField = false)
		static class TableOutPrimitive {
			@OpaField
			public String charVal;
			@OpaField
			public int intVal;
			@OpaField
			public long int64Val;
			@OpaField
			public BigDecimal decVal;
			@OpaField(dataType = DataType.DATE)
			public Date dateVal;
			@OpaField
			public boolean logVal;
			@OpaField(dataType = DataType.DATETIME)
			public Date tmVal;
			@OpaField(dataType = DataType.DATETIMETZ)
			public Date tmtzVal;
			@OpaField
			public Rowid rowid;
			@OpaField(dataType = DataType.RECID)
			public Long recid1;

			// non opa field - all field w/o @OpaField
			public String fake = "not-opa-field";

			@Override
			public String toString() {
				SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat datetimeSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				SimpleDateFormat datetimetzSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				return this.charVal
						+ "|" + this.intVal
						+ "|" + this.int64Val
						+ "|" + this.decVal
						+ "|" + (this.dateVal == null ? null : dateSdf.format(this.dateVal))
						+ "|" + this.logVal
						+ "|" + (this.tmVal == null ? null : datetimeSdf.format(this.tmVal))
						+ "|" + (this.tmtzVal == null ? null : datetimetzSdf.format(this.tmtzVal))
						+ "|" + this.rowid
						+ "|" + this.recid1
						;
			}
		}

		static void testTableOut(OpaServer server) throws OpaException {

			TableOutPrimitiveOpp opp = new TableOutPrimitiveOpp();
			opp.someId = "00000038";
			server.runProc(opp);

			String s0 = "Character value|1234|-123456789012|1.23|2014-02-15|true|2014-02-15T00:00:00|2014-02-15T00:00:00.000+0200|null|100";
			String s1 = "null|0|0|null|null|false|null|null|null|101";
			String s2 = "string|1|1|1.1|2014-01-15|true|2014-01-15T00:00:00|2014-01-15T00:00:00.000+0200|null|102";

			Assert.assertEquals(s0, opp.tt.get(0).toString());
			Assert.assertEquals(s1, opp.tt.get(1).toString());
			Assert.assertEquals(s2, opp.tt.get(2).toString());

			//log("TableOut ok");
		}

	}

	/**
	 * test fields mismatches
	 */
	private static class TableOutMismatchFields {

		@OpaProc(proc = "tests/opalib/test_05_table_handle.p")
		private static class TableOutTestDiffFieldOpp {
			@OpaParam
			public String someId;

			@OpaParam(table = SampleTable2.class, io = IoDir.OUT)
			public List<SampleTable2> tt;

			@OpaParam(io = IoDir.OUT)
			public String errorCode;

			@OpaParam(io = IoDir.OUT)
			public String errorMessage;
		}


		// table classes can be private
		@OpaTable
		private static class SampleTable2 {
			@OpaField
			public String charVal;
			@OpaField(name = "intVal")
			public Integer intValDiffFromOE; // must use field name from @OpaField(name)
			@OpaField
			public String fieldDoesNotExistsInOE = "AA"; // must ignore

			@Override
			public String toString() {
				return this.charVal + "|" + this.intValDiffFromOE + "|" + this.fieldDoesNotExistsInOE;
			}
		}

		static void testTableOut(OpaServer server) throws OpaException {

			TableOutTestDiffFieldOpp opp = new TableOutTestDiffFieldOpp();
			opp.someId = "00000038";
			server.runProc(opp);

			String s0 = "Character value|1234|AA";
			String s1 = "null|null|AA";
			String s2 = "string|1|AA";

			Assert.assertEquals(s0, opp.tt.get(0).toString());
			Assert.assertEquals(s1, opp.tt.get(1).toString());
			Assert.assertEquals(s2, opp.tt.get(2).toString());

		}


	}


}

