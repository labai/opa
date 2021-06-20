package opalib.opapa.impl;

import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaException;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;


public class I02SimpleParamsTest extends IntTestBase {

	@OpaProc(proc="opalib/opatest/test_02_simple_params.p")
	public static class SimpleParamsOpp {
		// input
		@OpaParam
		public String charVal1;
		@OpaParam
		public Integer intVal1;
		@OpaParam
		public Long int64Val1;
		@OpaParam
		public BigDecimal decVal1;
		@OpaParam(dataType= DataType.DATE)
		public Date dateVal1;
		@OpaParam
		public Boolean logVal1;
		@OpaParam(dataType=DataType.DATETIME)
		public Date tmVal1;
		@OpaParam(dataType=DataType.DATETIMETZ)
		public Date tmtzVal1;
		@OpaParam(dataType=DataType.LONGCHAR)
		public String longChar1;
		@OpaParam(dataType = DataType.ROWID)
		public String rowid1;

		// output
		@OpaParam(io= IoDir.OUT)
		public String charVal2;
		@OpaParam(io=IoDir.OUT)
		public Integer intVal2;
		@OpaParam(io=IoDir.OUT)
		public Long int64Val2;
		@OpaParam(io=IoDir.OUT)
		public BigDecimal decVal2;
		@OpaParam(io=IoDir.OUT, dataType=DataType.DATE)
		public Date dateVal2;
		@OpaParam(io=IoDir.OUT)
		public Boolean logVal2;
		@OpaParam(io=IoDir.OUT, dataType=DataType.DATETIME)
		public Date tmVal2;
		@OpaParam(io=IoDir.OUT, dataType=DataType.DATETIMETZ)
		public Date tmtzVal2;
		@OpaParam(io=IoDir.OUT, dataType=DataType.LONGCHAR)
		public String longChar2;
		@OpaParam(io=IoDir.OUT, dataType = DataType.ROWID)
		public String rowid2;

	}

	//@Ignore
	@Test
	public void testSimpleParams() throws OpaException, ParseException {

		SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat datetimeSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat datetimetzSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		Date date;
		Date datetime;
		Date datetimetz;

		// yyyy-MM-dd'T'HH:mm:ss.SSSZ
		date = dateSdf.parse("2014-10-15");
		datetime = datetimeSdf.parse("2014-10-15T13:45:45");
		datetimetz = datetimetzSdf.parse("2014-10-15T13:45:45.001+0300");

		SimpleParamsOpp opp = new SimpleParamsOpp();
		opp.charVal1 	= "string";
		opp.intVal1 	= 123;
		opp.int64Val1   = 1001001001000L;
		opp.decVal1 	= new BigDecimal("10.01");
		opp.dateVal1 	= date;
		opp.logVal1 	= true;
		opp.tmVal1 	    = datetime;
		opp.tmtzVal1 	= datetimetz;
		opp.longChar1	= "long string";
		opp.rowid1 	    = "AAAAAAAAiQA=";

		server.runProc(opp);

		String s1 = opp.charVal1
				+ "|" + opp.intVal1
				+ "|" + opp.int64Val1
				+ "|" + opp.decVal1
				+ "|" + dateSdf.format(opp.dateVal1)
				+ "|" + opp.logVal1
				+ "|" + datetimeSdf.format(opp.tmVal1)
				+ "|" + datetimetzSdf.format(opp.tmtzVal1)
				+ "|" + opp.longChar1
				;
		//string|123|1001001001000|10.01|2014-10-15|true|2014-10-15T13:45:45|2014-10-15T13:45:45.001+0300|long string
		//System.out.println(s1);

		String s2 = opp.charVal2
				+ "|" + opp.intVal2
				+ "|" + opp.int64Val2
				+ "|" + opp.decVal2
				+ "|" + dateSdf.format(opp.dateVal2)
				+ "|" + opp.logVal2
				+ "|" + datetimeSdf.format(opp.tmVal2)
				+ "|" + datetimetzSdf.format(opp.tmtzVal2)
				+ "|" + opp.longChar2
				;

		assertEquals(s2, s1);
	}


}

