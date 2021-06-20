package opalib.opapa.impl;

import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaException;
import opalib.api.OpaField;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import opalib.api.OpaTable;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/*
   additional tests for Java8 LocalDates
*/
public class P01PerfTableOut extends IntTestBase {


	//
	// TableOut various tests
	//
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


	@Ignore
	@Test
	public void test_performance_1() throws OpaException {
		// warmup
		for (int i = 1; i < 5; i++) {
			TableOutOpp opp = new TableOutOpp();
			opp.numRec = 1;
			server.runProc(opp);
		}

		TableOutOpp opp = new TableOutOpp();
		opp.numRec = 1000;

		long startTs = System.currentTimeMillis();

		server.runProc(opp);

		long delta = System.currentTimeMillis() - startTs;

		log("2 in time=" + delta + "ms");

	}

}

