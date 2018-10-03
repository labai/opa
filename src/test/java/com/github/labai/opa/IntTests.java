package com.github.labai.opa;

import com.github.labai.opa.Opa.DataType;
import com.github.labai.opa.Opa.OpaField;
import com.github.labai.opa.Opa.OpaTable;
import com.github.labai.opa.Opa.OpaTransient;
import com.github.labai.opa.OpaServer.SessionModel;
import com.progress.open4gl.Rowid;
import org.junit.Assume;
import org.junit.Before;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IntTests {

	public static class AblIntTestBase {

		protected OpaServer server;

		@Before
		public void init() {
			Assume.assumeTrue("Is integrations test enabled?", TestParams.INT_TESTS_ENABLED);
			server = new OpaServer(TestParams.APP_SERVER, TestParams.APP_USER, TestParams.APP_USER, SessionModel.STATE_FREE);
		}

		protected void log(String msg){
			System.out.println(msg);
		}
	}

	/*
	 * sample table, used in parameters
	 *
	 */
	@OpaTable(allowOmitOpaField = true)
	static class SampleTable implements Cloneable {
		@OpaField(name = "charVal")
		public String charValx;
		private Integer intVal;
		public Long int64Val;
		public BigDecimal decVal;
		@OpaField(dataType = DataType.DATE)
		public Date dateVal;
		public Boolean logVal;
		@OpaField(dataType = DataType.DATETIME)
		public Date tmVal;
		@OpaField(dataType = DataType.DATETIMETZ)
		public Date tmtzVal;
		public Rowid rowid;
		@OpaField(dataType = DataType.RECID)
		public Long recid1;

		// non opa field - must be declared with @OpaTransient
		@OpaTransient
		public String fake = "not-opa-field";

		// sample getter
		public Integer getIntVal() {
			return intVal;
		}

		// some strange setter - increase value by 100000
		public void setIntVal(Integer intVal) {
			this.intVal = (intVal == null) ? null : intVal + 100000;
		}

		// non public setter is not setter - should be ignored
		protected void setInt64Val(Long int64Val) {
			throw new RuntimeException("setInt64Val is not public");
		}

		@Override
		public String toString() {
			SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat datetimeSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat datetimetzSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			return this.charValx
					+ "|" + this.intVal
					+ "|" + this.int64Val
					+ "|" + this.decVal
					+ "|" + (this.dateVal == null ? null :dateSdf.format(this.dateVal))
					+ "|" + this.logVal
					+ "|" + (this.tmVal == null ? null : datetimeSdf.format(this.tmVal))
					+ "|" + (this.tmtzVal == null ? null : datetimetzSdf.format(this.tmtzVal))
					+ "|" + this.rowid
					+ "|" + this.recid1
					;
		}

		public SampleTable clone() throws CloneNotSupportedException {
			return (SampleTable)super.clone();
		}
	}



}
