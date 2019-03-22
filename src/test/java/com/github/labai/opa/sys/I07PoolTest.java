package com.github.labai.opa.sys;

import com.github.labai.opa.Opa.IoDir;
import com.github.labai.opa.Opa.OpaParam;
import com.github.labai.opa.Opa.OpaProc;
import com.github.labai.opa.OpaException;
import com.github.labai.opa.OpaServer.SessionModel;
import com.github.labai.opa.TestParams;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by Augustus on 2014.12.18.
 *
 * Test max pool connections
 */
//@Ignore
public class I07PoolTest {
	private static final String PROC_07_NAME = "jpx/test/opa/test_07_wait.p";

	protected AppServer server;

	@Before
	public void init() {
		Assume.assumeTrue("Is integration tests enabled?", TestParams.INT_TESTS_ENABLED);
		server = new AppServer(TestParams.APP_SERVER, TestParams.APP_USER, TestParams.APP_PASSWORD, SessionModel.STATE_FREE);
	}

	protected void log(String msg){
		System.out.println(msg);
	}


	@OpaProc
	public static class WaitOpp {
		@OpaParam
		public BigDecimal waitSec;

		@OpaParam(io= IoDir.OUT)
		public String errorCode;

		@OpaParam(io=IoDir.OUT)
		public String errorMessage;
	}

	/**
	 * Test pool connections
	 */
	@Test
	public void testPoolConnMax2() throws OpaException, InterruptedException {
		server.setMaxPoolSize(2);

		log("Starting testPoolConnMax2");
		Thread t1 = new Thread(createRunnable());
		Thread t2 = new Thread(createRunnable());
		Thread t3 = new Thread(createRunnable());
		t1.start(); t2.start(); t3.start();
		while (t1.isAlive() || t2.isAlive() || t3.isAlive()) {
			log("Still waiting...");
			t1.join(600);
			t2.join(600);
			t3.join(600);
		}

		Assert.assertEquals(0, server.getPool().getNumActive());
		Assert.assertEquals(2, server.getPool().getNumIdle()); // 2 idle connections - limited by pool
	}

	@Test
	public void testPoolConnMax5() throws OpaException, InterruptedException {
		server.setMaxPoolSize(5);

		log("Starting testPoolConnMax5");
		Thread t1 = new Thread(createRunnable());
		Thread t2 = new Thread(createRunnable());
		Thread t3 = new Thread(createRunnable());
		t1.start(); t2.start(); t3.start();
		while (t1.isAlive() || t2.isAlive() || t3.isAlive()) {
			log("Still waiting...");
			t1.join(600);
			t2.join(600);
			t3.join(600);
		}

		Assert.assertEquals(0, server.getPool().getNumActive());
		Assert.assertEquals(3, server.getPool().getNumIdle()); // 3 idle connections - was max at 1 time

	}

	@Test
	public void testPoolDropTTL() throws Exception {
		server.setMaxPoolSize(5);
		server.setConnectionTTLSec(1);

		log("Starting testPoolDropTTL");
		Thread t1 = new Thread(createRunnable());
		Thread t2 = new Thread(createRunnable());
		Thread t3 = new Thread(createRunnable());
		t1.start(); t2.start(); t3.start();
		while (t1.isAlive() || t2.isAlive() || t3.isAlive()) {
			log("Still waiting...");
			t1.join(600);
			t2.join(600);
			t3.join(600);
		}

		Assert.assertEquals(0, server.getPool().getNumActive());
		Assert.assertEquals(3, server.getPool().getNumIdle()); // 3 idle connections - was max at 1 time

		Thread.sleep(500);

		t1 = new Thread(createRunnable());
		t1.start();
		while (t1.isAlive()) {
			log("Still waiting...");
			t1.join(600);
		}

		// expect to be closed all expired and new 1 created
		Assert.assertEquals(1, server.getPool().getNumIdle());

	}


	private Runnable createRunnable() {
		return new Runnable(){
			@Override
			public void run() {
				try {
					log(Thread.currentThread().getName() + " starting thread, will do job 1s. Pool before: busy=" + server.getPool().getNumActive() + " idle=" + server.getPool().getNumIdle());
					WaitOpp opp1 = new WaitOpp();
					opp1.waitSec = new BigDecimal("1");
					server.runProc(opp1, PROC_07_NAME);
					log(Thread.currentThread().getName() + " thread done. Pool after: busy=" + server.getPool().getNumActive() + " idle=" + server.getPool().getNumIdle());
				} catch (OpaException e) {
					log(Thread.currentThread().getName() + " OpaException: " + e.getMessage());
					e.printStackTrace();
				}
			}
		};
	}

}
