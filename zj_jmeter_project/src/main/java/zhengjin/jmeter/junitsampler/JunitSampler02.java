package zhengjin.jmeter.junitsampler;

import org.apache.jmeter.protocol.java.sampler.JUnitSampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Junit samplers for set and get jmeter variable.
 * 
 * Scenario: User Parameters + Junit Request01 (test01SetJMeterVariable) + Junit
 * Request02 (test02GetJMeterVariable) + Debug Sampler + View Results Tree
 * 
 * CSV Data: CSV Data Set Config (foo,bar)
 * 
 * @author zhengjin
 *
 */
public final class JunitSampler02 {

	private static final Logger LOG = LoggerFactory.getLogger(JunitSampler02.class);

	public JunitSampler02() {
	}

	public JunitSampler02(String label) {
		LOG.info("Label:" + label);
	}

	@Test
	public void test01SetJMeterVariable() {
		JUnitSampler sampler = new JUnitSampler();
		JMeterContext context = sampler.getThreadContext();
		JMeterVariables vars = context.getVariables();
		vars.put("JUnitVariable", "this variable is set by " + context.getCurrentSampler().getName());
		context.setVariables(vars);
		LOG.info("set variable (JUnitVariable)");
		// check JMeterVariables by "Debug Sampler" component
	}

	@Test
	public void test02GetJMeterVariable() {
		// pre-condition: Add "User Parameters" component with "foo:bar" before Junit
		// sampler
		JUnitSampler sampler = new JUnitSampler();
		JMeterVariables vars = sampler.getThreadContext().getVariables();
		LOG.info("get variable (foo): " + vars.get("foo"));
		LOG.info("get variable (JUnitVariable): " + vars.get("JUnitVariable"));
	}

}
