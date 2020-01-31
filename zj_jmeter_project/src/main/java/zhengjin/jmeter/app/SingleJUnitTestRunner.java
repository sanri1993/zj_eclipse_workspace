package zhengjin.jmeter.app;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleJUnitTestRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SingleJUnitTestRunner.class);

	public static void main(String[] args) throws ClassNotFoundException {

		String[] classAndMethod = args[0].split("#");
		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
		Result result = new JUnitCore().run(request);

		if (!result.wasSuccessful()) {
			for (Failure f : result.getFailures()) {
				LOG.error("error message:{}, trace:\n{}", f.getMessage(), f.getTrace());
			}
			System.exit(99);
		}
		System.exit(0);
	}

}
