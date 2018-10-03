package com.github.labai.opa;

import com.github.labai.opa.IntTests.AblIntTestBase;
import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaField;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.Opa.OpaTable;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Augustus on 2015.06.19.
 */
public class I07BlobTest extends AblIntTestBase {

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


	@OpaProc(proc="jpx/test/opa/test_07_blobs.p")
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
	public void testTableOutBlob() throws OpaException, UnsupportedEncodingException {

		TableOutBlobOpp opp = new TableOutBlobOpp();
		opp.mmptrIn = "data-in".getBytes();
		opp.charValIn = "charValIn";
		opp.longCharValIn = "longCharValIn";

		SampleTableWithBlob ttrec = new SampleTableWithBlob();
		ttrec.blob1 = "blobin".getBytes();
		ttrec.clob1 = "clobin";

		opp.ttin = new ArrayList<SampleTableWithBlob>();
		opp.ttin.add(ttrec);

		server.runProc(opp);

		SampleTableWithBlob row = opp.ttout.get(0);
		Assert.assertEquals("blobin", new String(row.blob1, "UTF-8"));
		Assert.assertEquals("clobin", row.clob1);

		row = opp.ttout.get(1);
		Assert.assertEquals("Blob value", new String(row.blob1, "UTF-8"));
		Assert.assertEquals("Clob value", row.clob1);

		row = opp.ttout.get(2);

		Assert.assertEquals(null, row.blob1);
		Assert.assertEquals(null, row.clob1);

		Assert.assertEquals("data-in", new String(opp.dataOut));
		Assert.assertEquals("longCharValIn-out", opp.longCharValOut);

		//log("TableOut ok");
	}


	/**
	 * Test output table with blobs
	 */
	@Test
	public void testTableOutBlobNulls() throws OpaException, UnsupportedEncodingException {

		TableOutBlobOpp opp = new TableOutBlobOpp();
		opp.mmptrIn = null;
		opp.longCharValIn = null;

		SampleTableWithBlob ttrec = new SampleTableWithBlob();
		ttrec.blob1 = null;
		ttrec.clob1 = null;

		opp.ttin = new ArrayList<SampleTableWithBlob>();
		opp.ttin.add(ttrec);

		server.runProc(opp);

		SampleTableWithBlob row = opp.ttout.get(0);
		Assert.assertEquals(null, row.blob1);
		Assert.assertEquals(null, row.clob1);

		// WARNING: memptr is not null
		Assert.assertArrayEquals(new byte[]{}, opp.dataOut);
		Assert.assertEquals(null, opp.longCharValOut);

		//log("TableOut ok");
	}
}
