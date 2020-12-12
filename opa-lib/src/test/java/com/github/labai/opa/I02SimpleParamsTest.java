package com.github.labai.opa;

import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.progress.open4gl.Rowid;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class I02SimpleParamsTest extends IntTestBase {

	@OpaProc(proc="tests/opalib/test_02_simple_params.p")
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
		@OpaParam(dataType= DataType.DATETIME)
		public Date tmVal1;
		@OpaParam(dataType= DataType.DATETIMETZ)
		public Date tmtzVal1;
		@OpaParam(dataType= DataType.LONGCHAR)
		public String longChar1;
		@OpaParam
		public Rowid rowid1;
		// @OpaParam
		// public byte[] raw1;

		// output
		@OpaParam(io= IoDir.OUT)
		public String charVal2;
		@OpaParam(io= IoDir.OUT)
		public Integer intVal2;
		@OpaParam(io= IoDir.OUT)
		public Long int64Val2;
		@OpaParam(io= IoDir.OUT)
		public BigDecimal decVal2;
		@OpaParam(io= IoDir.OUT, dataType= DataType.DATE)
		public Date dateVal2;
		@OpaParam(io= IoDir.OUT)
		public Boolean logVal2;
		@OpaParam(io= IoDir.OUT, dataType= DataType.DATETIME)
		public Date tmVal2;
		@OpaParam(io= IoDir.OUT, dataType= DataType.DATETIMETZ)
		public Date tmtzVal2;
		@OpaParam(io= IoDir.OUT, dataType= DataType.LONGCHAR)
		public String longChar2;
		@OpaParam(io= IoDir.OUT)
		public Rowid rowid2;
		// @OpaParam(io=IoDir.OUT)
		// public byte[] raw2;

		// input-output
		@OpaParam(io= IoDir.INOUT)
		public String charVal3;
		@OpaParam(io= IoDir.INOUT)
		public Integer intVal3;
		@OpaParam(io= IoDir.INOUT)
		public Long int64Val3;
		@OpaParam(io= IoDir.INOUT)
		public BigDecimal decVal3;
		@OpaParam(io= IoDir.INOUT, dataType= DataType.DATE)
		public Date dateVal3;
		@OpaParam(io= IoDir.INOUT)
		public Boolean logVal3;
		@OpaParam(io= IoDir.INOUT, dataType= DataType.DATETIME)
		public Date tmVal3;
		@OpaParam(io= IoDir.INOUT, dataType= DataType.DATETIMETZ)
		public Date tmtzVal3;
		@OpaParam(io= IoDir.INOUT, dataType= DataType.LONGCHAR)
		public String longChar3;
		@OpaParam(io= IoDir.INOUT)
		public Rowid rowid3;
		// @OpaParam(io=IoDir.INOUT)
		// public byte[] raw3;
	}

	//@Ignore
	@Test
	public void testSimpleParams() throws OpaException, ParseException {

		SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat datetimeSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat datetimetzSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		Date date = null;
		Date datetime = null;
		Date datetimetz = null;

		// yyyy-MM-dd'T'HH:mm:ss.SSSZ
		date = dateSdf.parse("2014-10-15");
		datetime = datetimeSdf.parse("2014-10-15T13:45:45");
		datetimetz = datetimetzSdf.parse("2014-10-15T13:45:45.001+0300");

		SimpleParamsOpp opp = new SimpleParamsOpp();
		opp.charVal3 	= opp.charVal1 	= "string";
		opp.intVal3 	= opp.intVal1 	= 123;
		opp.int64Val3	= opp.int64Val1 = 1001001001000L;
		opp.decVal3 	= opp.decVal1 	= new BigDecimal("10.01");
		opp.dateVal3 	= opp.dateVal1 	= date;
		opp.logVal3 	= opp.logVal1 	= true;
		opp.tmVal3  	= opp.tmVal1 	= datetime;
		opp.tmtzVal3 	= opp.tmtzVal1 	= datetimetz;
		opp.longChar3	= opp.longChar1	= "long string";
		opp.rowid3 		= opp.rowid1 	= null;
		//proc.raw3 		= proc.raw1 		= null;

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

		Assert.assertEquals(s2, s1);

		String s3 = opp.charVal3
				+ "|" + opp.intVal3
				+ "|" + opp.int64Val3
				+ "|" + opp.decVal3
				+ "|" + dateSdf.format(opp.dateVal3)
				+ "|" + opp.logVal3
				+ "|" + datetimeSdf.format(opp.tmVal3)
				+ "|" + datetimetzSdf.format(opp.tmtzVal3)
				+ "|" + opp.longChar3
				;
		String testStr = "GOT:string|124|1001001001001|11.01|2014-10-16|false|2014-10-16T13:45:45|2014-10-16T13:45:45.001+0300|GOT:long string";
		Assert.assertEquals(testStr, s3);

		//log("Simple params ok");
	}


}

