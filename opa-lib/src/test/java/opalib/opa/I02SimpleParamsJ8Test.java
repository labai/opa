package opalib.opa;

import opalib.opa.Opa.DataType;
import opalib.opa.Opa.IoDir;
import opalib.opa.Opa.OpaParam;
import opalib.opa.Opa.OpaProc;
import com.progress.open4gl.Rowid;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


public class I02SimpleParamsJ8Test extends IntTestBase {

	//
	// with LocalDate, LocalDateTime, OffsetDateTime
	//
	@OpaProc(proc="tests/opalib/test_02_simple_params.p")
	public static class SimpleParams2Opp {
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
		public LocalDate dateVal1;
		@OpaParam
		public Boolean logVal1;
		@OpaParam(dataType= DataType.DATETIME)
		public LocalDateTime tmVal1;
		@OpaParam(dataType= DataType.DATETIMETZ)
		public OffsetDateTime tmtzVal1;
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
		@OpaParam(io= IoDir.OUT)
		public LocalDate dateVal2;
		@OpaParam(io= IoDir.OUT)
		public Boolean logVal2;
		@OpaParam(io= IoDir.OUT)
		public LocalDateTime tmVal2;
		@OpaParam(io= IoDir.OUT)
		public OffsetDateTime tmtzVal2;
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
		@OpaParam(io= IoDir.INOUT)
		public LocalDate dateVal3;
		@OpaParam(io= IoDir.INOUT)
		public Boolean logVal3;
		@OpaParam(io= IoDir.INOUT)
		public LocalDateTime tmVal3;
		@OpaParam(io= IoDir.INOUT)
		public OffsetDateTime tmtzVal3;
		@OpaParam(io= IoDir.INOUT, dataType= DataType.LONGCHAR)
		public String longChar3;
		@OpaParam(io= IoDir.INOUT)
		public Rowid rowid3;
		// @OpaParam(io=IoDir.INOUT)
		// public byte[] raw3;
	}


	@Test
	public void testSimpleParamsVariousDate() throws OpaException, ParseException {

		SimpleParams2Opp opp = new SimpleParams2Opp();
		opp.dateVal3 	= opp.dateVal1 	= LocalDate.parse("2014-10-15");
		opp.tmVal3  	= opp.tmVal1 	= LocalDateTime.parse("2014-10-15T13:45:45");
		opp.tmtzVal3 	= opp.tmtzVal1 	= OffsetDateTime.of(2014, 10, 15, 13, 45, 45, 123_000_000, ZoneOffset.ofHours(3));

		server.runProc(opp);

		Assert.assertEquals(opp.tmtzVal1, opp.tmtzVal2);

		String s1 = opp.dateVal1 + "|" + opp.tmVal1 + "|" + opp.tmtzVal1;
		String s2 = opp.dateVal2 + "|" + opp.tmVal2 + "|" + opp.tmtzVal2;

		Assert.assertEquals(s2, s1);

		String s3 = opp.dateVal3 + "|" + opp.tmVal3 + "|" + opp.tmtzVal3;

		String testStr = "2014-10-16|2014-10-16T13:45:45|2014-10-16T13:45:45.123+03:00";
		Assert.assertEquals(testStr, s3);

		//log("Simple params ok");
	}



}

