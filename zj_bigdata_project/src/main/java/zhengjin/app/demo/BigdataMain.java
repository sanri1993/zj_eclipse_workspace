package zhengjin.app.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public final class BigdataMain {

	private static final String TAG = BigdataMain.class.getSimpleName() + " => ";
	private static final Logger logger = LoggerFactory.getLogger(BigdataMain.class);

	public static void main(String[] args) {

		BigdataMain app = new BigdataMain();

		for (String arg : args) {
			logger.info(TAG + "argument: " + arg);
		}
		logger.info(TAG + "First maven app.");

		// test showcaseThrowables01
		try {
			app.showcaseThrowables01();
		} catch (InvalidInputException e) {
			// get the root cause
			logger.error(Throwables.getRootCause(e).toString());
		} catch (Exception e) {
			// get the stack trace in string format
			logger.error(Throwables.getStackTraceAsString(e));
		}

		// test showcaseThrowables02
		try {
			app.showcaseThrowables02();
		} catch (Exception e) {
			logger.error(Throwables.getStackTraceAsString(e));
		}
	}

	private void showcaseThrowables01() throws InvalidInputException {
		try {
			this.sqrt(-3.0);
		} catch (Throwable e) {
			// check the type of exception and throw it
			Throwables.throwIfInstanceOf(e, InvalidInputException.class);
			throw new RuntimeException(e);
		}
	}

	public void showcaseThrowables02() {
		try {
			int[] data = { 1, 2, 3 };
			this.getValue(data, 4);
		} catch (Throwable e) {
			Throwables.throwIfInstanceOf(e, IndexOutOfBoundsException.class);
			throw new RuntimeException(e);
		}
	}

	private double sqrt(double input) throws InvalidInputException {
		if (input < 0) {
			throw new InvalidInputException();
		}
		return Math.sqrt(input);
	}

	private double getValue(int[] list, int index) throws IndexOutOfBoundsException {
		return list[index];
	}

	public static class InvalidInputException extends Exception {

		private static final long serialVersionUID = 1L;
	}

}
