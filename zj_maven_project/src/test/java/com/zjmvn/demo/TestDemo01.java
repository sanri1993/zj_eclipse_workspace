package com.zjmvn.demo;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDemo01 {

	private final static String TAG = TestDemo01.class.getSimpleName() + " => ";
	private final static Logger logger = Logger.getLogger(TestDemo01.class);

	@Test
	public void testSample01() {
		logger.info(TAG + "First maven test demo.");
		Assert.assertTrue(true);
	}

}
