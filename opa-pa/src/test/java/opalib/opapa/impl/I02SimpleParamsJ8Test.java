package opalib.opapa.impl;

// import com.progress.open4gl.Rowid;

import opalib.api.DataType;
import opalib.api.IoDir;
import opalib.api.OpaException;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;


public class I02SimpleParamsJ8Test extends IntTestBase {

    //
    // with LocalDate, LocalDateTime, OffsetDateTime
    //
    @OpaProc(proc = "opalib/opatest/test_02_simple_params.p")
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
        @OpaParam
        public LocalDate dateVal1;
        @OpaParam
        public Boolean logVal1;
        @OpaParam
        public LocalDateTime tmVal1;
        @OpaParam
        public OffsetDateTime tmtzVal1;
        @OpaParam(dataType = DataType.LONGCHAR)
        public String longChar1;
        @OpaParam(dataType = DataType.ROWID)
        public String rowid1;

        // output
        @OpaParam(io = IoDir.OUT)
        public String charVal2;
        @OpaParam(io = IoDir.OUT)
        public Integer intVal2;
        @OpaParam(io = IoDir.OUT)
        public Long int64Val2;
        @OpaParam(io = IoDir.OUT)
        public BigDecimal decVal2;
        @OpaParam(io = IoDir.OUT)
        public LocalDate dateVal2;
        @OpaParam(io = IoDir.OUT)
        public Boolean logVal2;
        @OpaParam(io = IoDir.OUT)
        public LocalDateTime tmVal2;
        @OpaParam(io = IoDir.OUT)
        public OffsetDateTime tmtzVal2;
        @OpaParam(io = IoDir.OUT, dataType = DataType.LONGCHAR)
        public String longChar2;
		@OpaParam(io = IoDir.OUT, dataType = DataType.ROWID)
        public String rowid2;

    }


    @Test
    public void testSimpleParamsVariousDate() throws OpaException, ParseException {

        SimpleParams2Opp opp = new SimpleParams2Opp();
        opp.dateVal1 = LocalDate.parse("2014-10-15");
        opp.tmVal1 = LocalDateTime.parse("2014-10-15T13:45:45");
        opp.tmtzVal1 = OffsetDateTime.of(2014, 10, 15, 13, 45, 45, 123_000_000, ZoneOffset.ofHours(3));

        server.runProc(opp);

        assertEquals(opp.tmtzVal1, opp.tmtzVal2);

        String s1 = opp.dateVal1 + "|" + opp.tmVal1 + "|" + opp.tmtzVal1;
        String s2 = opp.dateVal2 + "|" + opp.tmVal2 + "|" + opp.tmtzVal2;

        assertEquals(s2, s1);

    }


}

