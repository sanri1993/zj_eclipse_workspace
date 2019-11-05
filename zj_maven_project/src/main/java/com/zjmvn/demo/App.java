package com.zjmvn.demo;

import org.apache.log4j.Logger;

public class App {

	private static final String TAG = App.class.getSimpleName() + " => ";
	private static final Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) {

		for (String arg : args) {
			logger.info(TAG + "argument: " + arg);
		}
		logger.info(TAG + "First maven app.");

		// guava utils test
		try {
			System.out.println("guava utils getValue() results:" + GuavaUtils.getValueWithPrecondition(6));
		} catch (IndexOutOfBoundsException e) {
			System.out.println(e.getMessage());
		}
	}

}
