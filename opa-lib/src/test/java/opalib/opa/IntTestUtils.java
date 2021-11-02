package opalib.opa;

import com.progress.open4gl.Rowid;
import opalib.api.DataType;
import opalib.api.OpaField;
import opalib.api.OpaTable;
import opalib.api.OpaTransient;
import opalib.opa.OpaServer.SessionModel;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IntTestUtils {

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
			return (SampleTable) super.clone();
		}
	}

	public static OpaServer createOpaServer(String url) {
		return createOpaServer(url, SessionModel.STATE_FREE);
	}
	public static OpaServer createOpaServer(String url, SessionModel sessionModel) {
		OpaServer server = new OpaServer(url, "", "", sessionModel);
		if (url.startsWith("https://")) {
			String psccerts = Thread.currentThread().getContextClassLoader().getResource(TestParams.APP_PSCCERTS).getFile();
			server.setCertificateStore(psccerts);
		}
		return server;
	}

}
