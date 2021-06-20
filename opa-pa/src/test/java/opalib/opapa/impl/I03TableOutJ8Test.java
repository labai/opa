package opalib.opapa.impl;

import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaException;
import opalib.api.OpaField;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import opalib.api.OpaTable;
import opalib.opapa.OpaServer;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
   additional tests for Java8 LocalDates
*/
public class I03TableOutJ8Test extends IntTestBase {

	@Test
	public void testTableOutWithLocalDateTests() throws OpaException {
		TableOutWithLocalDateTests.testTableOut(server);
	}

	//
	// TableOut various tests
	//

	/**
	 * for LocalDates
	 */
	private static class TableOutWithLocalDateTests {

		@OpaProc(proc = "opalib/opatest/test_03_table_out.p")
		private static class TableOutOpp {
			@OpaParam
			private Integer numRec = null;

			@OpaParam(table = TableWithLocalDate.class, io = IoDir.OUT)
			private List<TableWithLocalDate> tt;

			@OpaParam(io = IoDir.OUT)
			private String errorCode;

			@OpaParam(io = IoDir.OUT)
			private String errorMessage;
		}

		// with LocalDate
		@OpaTable(allowOmitOpaField = true)
		private static class TableWithLocalDate {
			public String charVal;
			public int intVal;
			public long int64Val;
			public BigDecimal decVal;
			public LocalDate dateVal;
			public boolean logVal;
			public LocalDateTime tmVal;
			public OffsetDateTime tmtzVal;
			@OpaField(dataType = DataType.ROWID)
			public String rowid;
			@OpaField(dataType = DataType.RECID)
			public Long recid1;

			@Override
			public String toString() {
				return this.dateVal + "|" + this.tmVal + "|" + this.tmtzVal;
			}
		}

		static void testTableOut(OpaServer server) throws OpaException {

			TableOutOpp opp = new TableOutOpp();
			opp.numRec = null;
			server.runProc(opp);

			String s0 = "2014-02-15|2014-02-15T00:00|2014-02-15T00:00+02:00";
			String s1 = "null|null|null";
			String s2 = "2014-01-15|2014-01-15T00:00|2014-01-15T00:00+02:00";

			assertEquals(s0, opp.tt.get(0).toString());
			assertEquals(s1, opp.tt.get(1).toString());
			assertEquals(s2, opp.tt.get(2).toString());

		}

	}


}

