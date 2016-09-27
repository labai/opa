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
import java.util.List;

/**
 * Created by Augustus on 2015.06.19.
 */
//@Ignore
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

		@OpaParam(dataType = DataType.MEMPTR)
		public byte[] dataIn;

		@OpaParam(table = SampleTableWithBlob.class, io = IoDir.OUT)
		public List<SampleTableWithBlob> tt;

		@OpaParam(dataType = DataType.MEMPTR, io = IoDir.OUT)
		public byte[] dataOut;

	}

	/**
	 * Test output table with blobs
	 */
	@Test
	public void testTableOutBlob() throws OpaException, UnsupportedEncodingException {

		TableOutBlobOpp opp = new TableOutBlobOpp();
		opp.dataIn = "data-in".getBytes();
		server.runProc(opp);

		SampleTableWithBlob row = opp.tt.get(0);

		Assert.assertEquals("Blob value", new String(row.blob1, "UTF-8"));
		Assert.assertEquals("Clob value", row.clob1);

		row = opp.tt.get(1);

		Assert.assertEquals(null, row.blob1);
		Assert.assertEquals(null, row.clob1);

		Assert.assertEquals("Memptr value from: data-in", new String(opp.dataOut));

		//log("TableOut ok");
	}

}
