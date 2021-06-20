package opalib.opapa.impl;

import opalib.api.IoDir;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import opalib.api.OpaTransient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Augustus
 * created on 2021.06.18
 */
public class JsonConverterTest {
    private final static Logger logger = LoggerFactory.getLogger(JsonConverterTest.class);

    JacksonConverter converter = new JacksonConverter();


    @Test
    public void test_getInputJson_takes_only_input_params() {
        @OpaProc
        class OppSample1 {
            @OpaParam
            String inp1;
            @OpaParam(io = IoDir.OUT)
            Integer out1;
            String shouldIgnore = "x";
        }

        OppSample1 opp = new OppSample1();
        opp.inp1 = "aaa";
        opp.out1 = 1;
        assertEquals("{'inp1':'aaa'}".replace("'", "\""), converter.getInputJson(opp));
    }

    @Test
    public void test_getInputJson_takes_getters() {
        @OpaProc
        class OppSample1 {
            @OpaParam
            String inp1;
            @OpaParam(io = IoDir.OUT)
            Integer out1;
            String shouldIgnore = "x";

            public String getInp1() { return inp1 + "Z"; }
            public Integer getOut1() { return out1; }
            public String getShouldIgnore() { return shouldIgnore; }
        }

        OppSample1 opp = new OppSample1();
        opp.inp1 = "aaa";
        opp.out1 = 1;
        assertEquals("{'inp1':'aaaZ'}".replace("'", "\""), converter.getInputJson(opp));
        logger.info(converter.getInputJson(opp));

    }

    @Test
    public void test_getInputJson_tt() {
        class OpaSampleTT1 {
            String f1;
            Long f2;
            @OpaTransient
            String shouldIgnore = "x";
            public OpaSampleTT1() {}
            public OpaSampleTT1(String f1, Long f2) {
                this.f1 = f1;
                this.f2 = f2;
            }
            public String getF1() { return f1 + "Z"; }
        }
        @OpaProc
        class OppSample1 {
            @OpaParam
            String inp1;
            @OpaParam(io = IoDir.OUT)
            Integer out1;
            @OpaParam(table = OpaSampleTT1.class)
            List<OpaSampleTT1> tt1 = new ArrayList<>();
        }

        OppSample1 opp = new OppSample1();
        opp.inp1 = "aaa";
        opp.tt1.add(new OpaSampleTT1("aaa", 222L));
        String expect = "{'inp1':'aaa','tt1':[{'f1':'aaaZ','f2':222}]}".replace("'", "\"");
        assertEquals(expect, converter.getInputJson(opp));

    }
    static class OpaSampleTT3 {
        String f1;
    }

    @OpaProc
    static class OppSample3 {
        @OpaParam(io = IoDir.OUT)
        String out1;
        @OpaParam(io = IoDir.OUT, table = OpaSampleTT3.class)
        List<OpaSampleTT3> tt1 = new ArrayList<>();
    }

    @Test
    public void test_03_readJson_simple() {

        OppSample3 opp = new OppSample3();
        String json = "{'out1':'ABRA','tt1':[{'f1':'KADABRA'}]}".replace("'", "\"");
        converter.applyResponseJson(opp, json);

        assertEquals("ABRA", opp.out1);
        assertEquals("KADABRA", opp.tt1.get(0).f1);

    }

    enum Aenum { ABRA, KADABRA }

    static class OpaSampleTT4 {
        Aenum f1;
    }
    @OpaProc
    static class OppSample4 {
        @OpaParam(io = IoDir.OUT)
        Aenum out1;
        @OpaParam(io = IoDir.OUT, table = OpaSampleTT3.class)
        List<OpaSampleTT4> tt1 = new ArrayList<>();
    }
    @Test
    public void test_04_1_readJson_enums() {

        OppSample4 opp = new OppSample4();
        String json = "{'out1':'ABRA','tt1':[{'f1':'KADABRA'}]}".replace("'", "\"");
        converter.applyResponseJson(opp, json);

        assertEquals(Aenum.ABRA, opp.out1);
        assertEquals(Aenum.KADABRA, opp.tt1.get(0).f1);

    }

    @Test
    public void test_04_2_readJson_enums_nulls() {

        OppSample4 opp = new OppSample4();
        String json = "{'out1':null,'tt1':[{'f1':null}]}".replace("'", "\"");
        converter.applyResponseJson(opp, json);

        assertEquals(null, opp.out1);
        assertEquals(null, opp.tt1.get(0).f1);

    }

    @Test
    public void test_04_3_readJson_enums_empty_as_null() {

        OppSample4 opp = new OppSample4();
        String json = "{'out1':'','tt1':[{'f1':''}]}".replace("'", "\"");
        converter.applyResponseJson(opp, json);

        assertEquals(null, opp.out1);
        assertEquals(null, opp.tt1.get(0).f1);

    }

}
