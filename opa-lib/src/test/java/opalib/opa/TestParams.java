package opalib.opa;

/**
 * Created by Augustus on 2015.02.10.
 */
public class TestParams {

	// disable all tests, as it is integration tests
	final static public boolean INT_TESTS_ENABLED = true;

	// Using Pasoe connection (e.g. oepas1 from installation).
	// Before running tests you need to copy (resource)/pcode/*.p files to (propath_on_appserver)/tests/opalib/
	// oepas1 should allow call apsv (enabled in openedge.properties, also allowed by security)

	final static public String APP_SERVER = "http://localhost:8810/apsv";
	final static public String APP_SERVER_STATELESS = "http://localhost:8810/apsv";
	final static public String APP_USER = "";
	final static public String APP_PASSWORD = "";

	final static public String APP_PSCCERTS = "psccerts.zip";
}
