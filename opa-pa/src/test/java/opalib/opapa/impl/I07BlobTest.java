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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by Augustus on 2015.06.19.
 */
public class I07BlobTest extends IntTestBase {

	@OpaTable
	static class SampleTableWithBlob {
		@OpaField
		public String charVal;

		@OpaField(dataType = DataType.BLOB)
		public byte[] blob1;

		@OpaField(dataType = DataType.CLOB)
		public String clob1;

		@Override
		public String toString() {
			return charVal == null ? "null" : charVal;
		}
	}


	@OpaProc(proc="opalib/opatest/test_07_blobs.p")
	public static class TableOutBlobOpp {
		@OpaParam
		public String charValIn;

		@OpaParam(dataType = DataType.LONGCHAR)
		public String longCharValIn;

		@OpaParam(dataType = DataType.MEMPTR)
		public byte[] mmptrIn;

		@OpaParam(table = SampleTableWithBlob.class, io = IoDir.IN)
		public List<SampleTableWithBlob> ttin;

		@OpaParam(table = SampleTableWithBlob.class, io = IoDir.OUT)
		public List<SampleTableWithBlob> ttout;

		@OpaParam(dataType = DataType.LONGCHAR, io = IoDir.OUT)
		public String longCharValOut;

		@OpaParam(dataType = DataType.MEMPTR, io = IoDir.OUT)
		public byte[] dataOut;

	}

	/**
	 * Test output table with blobs
	 */
	@Test
	public void testTableOutBlob() throws OpaException {

		TableOutBlobOpp opp = new TableOutBlobOpp();
		opp.mmptrIn = "data-in".getBytes();
		opp.charValIn = "charValIn";
		opp.longCharValIn = "longCharValIn";

		SampleTableWithBlob ttrec = new SampleTableWithBlob();
		ttrec.blob1 = "blobin".getBytes();
		ttrec.clob1 = "clobin";

		opp.ttin = new ArrayList<>();
		opp.ttin.add(ttrec);

		server.runProc(opp);

		SampleTableWithBlob row = opp.ttout.get(0);
		assertEquals("blobin", new String(row.blob1, StandardCharsets.UTF_8));
		assertEquals("clobin", row.clob1);

		row = opp.ttout.get(1);
		assertEquals("Blob value", new String(row.blob1, StandardCharsets.UTF_8));
		assertEquals("Clob value", row.clob1);

		row = opp.ttout.get(2);

		assertEquals(null, row.blob1);
		assertEquals(null, row.clob1);

		assertEquals("data-in", new String(opp.dataOut));
		assertEquals("longCharValIn-out", opp.longCharValOut);

		//log("TableOut ok");
	}


	/**
	 * Test output table with blobs
	 */
	@Ignore
	@Test
	public void testTableOutBlobNulls() throws OpaException {

		TableOutBlobOpp opp = new TableOutBlobOpp();
		opp.mmptrIn = null;
		opp.longCharValIn = null;

		SampleTableWithBlob ttrec = new SampleTableWithBlob();
		ttrec.blob1 = null;
		ttrec.clob1 = null;

		opp.ttin = new ArrayList<>();
		opp.ttin.add(ttrec);

		server.runProc(opp);

		SampleTableWithBlob row = opp.ttout.get(0);
		assertEquals(null, row.blob1);
		assertEquals(null, row.clob1);

		// WARNING: memptr is not null
		assertArrayEquals(new byte[]{}, opp.dataOut);
		// WARNING: longchar is "" (fixme?)
		assertEquals(null, opp.longCharValOut);

	}
}
