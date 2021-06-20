package com.github.labai.opa;

import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaField;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.Opa.OpaTable;
import com.progress.open4gl.Rowid;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Augustus
 * created on 2020.12.13
 */
public class I10ExtConverterTest extends IntTestBase {

    public static class DeciX {
        private final BigDecimal decimal;
        public DeciX(String val) { decimal = new BigDecimal(val); }
        public BigDecimal toDecimal() { return decimal; }
        @Override public String toString() { return decimal.toString(); }
    }

    @OpaProc(proc="tests/opalib/test_04_table_inout.p")
    static class TablesInOutOpp {
        @OpaParam
        public String someId;
        @OpaParam(table = Table10.class, io = IoDir.OUT)
        public List<Table10> ttout;
        @OpaParam(table = Table10.class, io = IoDir.INOUT)
        public List<Table10> ttinout = new ArrayList<>();
        @OpaParam(table =Table10.class, io = IoDir.IN)
        public List<Table10> ttin = new ArrayList<>();
        @OpaParam(io = IoDir.OUT)
        public String errorCode;
        @OpaParam(io = IoDir.OUT)
        public String errorMessage;
    }

    @OpaTable(allowOmitOpaField = true)
    static class Table10 {
        public String charVal;
        public Integer intVal;
        public Long int64Val;
        public DeciX decVal;
        public LocalDate dateVal;
        public Boolean logVal;
        public LocalDateTime tmVal;
        @OpaField(dataType = DataType.DATETIMETZ)
        public Date tmtzVal;
        public Rowid rowid;
        @OpaField(dataType = DataType.RECID)
        public Long recid1;

        @Override
        public String toString() {
            return this.charVal + "|" + this.decVal;
        }
    }



    @Test
    public void test_decix() {
        OpaConverters.registerTypeConverter(DataType.DECIMAL, DeciX.class,
                val -> val == null ? null : new DeciX(val),
                deciX -> deciX == null ? null : deciX.toDecimal());

        TablesInOutOpp opp = new TablesInOutOpp();

        Table10 tt1 = new Table10();
        tt1.charVal = "rec1";
        tt1.decVal = new DeciX("1.1");

        Table10 tt2 = new Table10();
        tt2.charVal = "rec2";
        tt2.decVal = new DeciX("1.2");

        opp.ttin = new ArrayList<>();
        opp.ttin.add(tt1);
        opp.ttinout = new ArrayList<>();
        opp.ttinout.add(tt2);

        server.runProc(opp);

        assertEquals("rec1|1.1", opp.ttout.get(0).toString());
        assertEquals("rec2|1.2", opp.ttinout.get(0).toString());

    }
}
