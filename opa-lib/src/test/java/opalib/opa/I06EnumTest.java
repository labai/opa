package opalib.opa;

import opalib.api.IoDir;
import opalib.api.OpaException;
import opalib.api.OpaField;
import opalib.api.OpaParam;
import opalib.api.OpaProc;
import opalib.api.OpaTable;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Augustus on 2014.12.18.
 */
public class I06EnumTest extends IntTestBase {

	public enum SampleEnum {
		ENUM_1, ENUM_2;
	}

	@OpaTable
	static class SampleTableWithEnum {
		@OpaField
		public SampleEnum enumVal;

		@Override
		public String toString() {
			return enumVal == null ? "null" : enumVal.toString();
		}
	}


	@OpaProc(proc = "tests/opalib/test_06_enums.p")
	public static class TableOutEnumOpp {
		@OpaParam
		public SampleEnum enumIn;

		@OpaParam(table = SampleTableWithEnum.class)
		public List<SampleTableWithEnum> ttin;

		@OpaParam(io = IoDir.OUT)
		public SampleEnum enumOut1;

		@OpaParam(io = IoDir.OUT)
		public SampleEnum enumOut2;

		@OpaParam(io = IoDir.OUT)
		public SampleEnum enumOut3;

		@OpaParam(io = IoDir.OUT)
		public SampleEnum enumOut4;

		@OpaParam(table = SampleTableWithEnum.class, io = IoDir.OUT)
		public List<SampleTableWithEnum> tt;

		@OpaParam(io = IoDir.OUT)
		public String errorCode;

		@OpaParam(io = IoDir.OUT)
		public String errorMessage;
	}

	/**
	 * Test output table with Java enums
	 */
	//@Ignore
	@Test
	public void testTableOutEnum() throws OpaException {

		TableOutEnumOpp opp = new TableOutEnumOpp();
		opp.enumIn = SampleEnum.ENUM_2;

		SampleTableWithEnum rec1 = new SampleTableWithEnum();
		rec1.enumVal = SampleEnum.ENUM_2;

		opp.ttin = new ArrayList<SampleTableWithEnum>();
		opp.ttin.add(rec1);

		server.runProc(opp);


		String s0 = "null"; // empty string will be converted to null
		String s1 = "null";
		String s2 = "ENUM_1";

		Assert.assertEquals(s0, opp.tt.get(0).toString());
		Assert.assertEquals(s1, opp.tt.get(1).toString());
		Assert.assertEquals(s2, opp.tt.get(2).toString());
		Assert.assertEquals("ENUM_2", opp.tt.get(3).toString()); // must be copy form ttin

		Assert.assertEquals(opp.enumOut1, SampleEnum.ENUM_2);
		Assert.assertEquals(opp.enumOut2, null);
		Assert.assertEquals(opp.enumOut3, null);
		Assert.assertEquals(opp.enumOut4, SampleEnum.ENUM_1);

		//log("TableOut ok");
	}

}
