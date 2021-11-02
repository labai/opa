package opalib.opa.impl;

import com.progress.open4gl.Open4GLException;
import opalib.api.OpaException;
import opalib.api.OpaProc;
import opalib.opa.OpaServer.RunResult;
import opalib.opa.OpaServer.SessionModel;
import opalib.opa.impl.Exceptions.OpaSessionTimeoutException;
import opalib.opa.impl.Pool.JpxConnPool;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Augustus
 * created on 2021.08.31
 */
public class F11AppServerRunProcTest {
    @OpaProc
    static class Test11Opp {
    }

    private AppServer appServer;
    private JpxConnPool pool;
    private final Test11Opp opp = new Test11Opp();

    @Before
    public void setUp() {
        appServer = new AppServer("x", "-", "-", SessionModel.STATE_FREE);
        pool = mock(JpxConnPool.class);
        appServer.setPool(pool);
    }

    @Test
    public void testRunProc_whenOk_thenReturnToPool() throws Exception {

        JavaProxyAgent jpx1 = mock(JavaProxyAgent.class);
        when(jpx1.runProc(any(), any(), any())).thenReturn(new RunResult1("a"));

        when(pool.borrowObject()).thenReturn(jpx1);
        doNothing().when(pool).returnObject(jpx1);

        RunResult rr = appServer.runProc(opp, "y", null);

        assertEquals("a", rr.returnValue());
        verify(pool).returnObject(jpx1);
    }

    @Test
    public void testRunProc_whenException_thenInvalidate() throws Exception {

        JavaProxyAgent jpx1 = mock(JavaProxyAgent.class);
        when(jpx1.runProc(any(), any(), any())).thenThrow(new Open4GLException("Open4GLException"));

        when(pool.borrowObject()).thenReturn(jpx1);
        doNothing().when(pool).invalidateObject(jpx1);
        assertThrows(OpaException.class, () ->
                appServer.runProc(opp, "y", null)
        );
        verify(pool).invalidateObject(jpx1);
    }

    @Test
    public void testRunProc_whenSessionException_thenRetry() throws Exception {

        JavaProxyAgent jpx1 = mock(JavaProxyAgent.class);
        JavaProxyAgent jpx2 = mock(JavaProxyAgent.class);
        when(jpx1.runProc(any(), any(), any())).thenThrow(new OpaSessionTimeoutException());
        when(jpx2.runProc(any(), any(), any())).thenReturn(new RunResult1("a"));

        when(pool.borrowObject())
            .thenReturn(jpx1)
            .thenReturn(jpx2);

        doNothing().when(pool).invalidateObject(jpx1);
        doNothing().when(pool).returnObject(jpx2);

        RunResult rr = appServer.runProc(opp, "y", null);

        assertEquals("a", rr.returnValue());
        verify(pool).invalidateObject(jpx1);
        verify(pool).returnObject(jpx2);
    }

    private static class RunResult1 implements RunResult {
        private final String returnValue;
        public RunResult1(String returnValue) {
            this.returnValue = returnValue;
        }
        @Override public String returnValue() { return returnValue; }
        @Override public String requestId() { return "-"; }
    }

}
