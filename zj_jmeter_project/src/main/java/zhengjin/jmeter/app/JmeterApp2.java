package zhengjin.jmeter.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1. 定义每个组件的生成方式，然后再按一定结构组装各个组件，最后生成jmx文件 2. 生成.jtl结果文件 3. 单机压测
 * 
 * @author zhengjin
 *
 */
public final class JmeterApp2 {

	private static final Logger LOG = LoggerFactory.getLogger(JmeterApp2.class);

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws FileNotFoundException, IOException {

		final String jmeterHomePath = "/usr/local/Cellar/jmeter/5.0/libexec";
		File jmeterHome = new File(jmeterHomePath);
		if (!jmeterHome.exists()) {
			LOG.error("Jmeter home {} not exist!", jmeterHome);
			System.exit(1);
		}

		final String jmeterPropertiesPath = jmeterHome.getAbsolutePath() + File.separator + "bin" + File.separator
				+ "jmeter.properties";
		File jmeterProperties = new File(jmeterPropertiesPath);
		if (!jmeterProperties.exists()) {
			LOG.error("Jmeter properties file {} not exist!", jmeterProperties);
			System.exit(1);
		}

		// JMeter初始化(属性、日志级别、区域设置等)
		JMeterUtils.setJMeterHome(jmeterHome.getPath());
		JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
		// 可以注释这一行，查看额外的日志，例如DEBUG级别
		JMeterUtils.initLogging();
		JMeterUtils.initLocale();

		// HTTP Sampler - 打开 baidu.com
		HTTPSamplerProxy baiducomSampler = new HTTPSamplerProxy();
		baiducomSampler.setDomain("baidu.com");
		baiducomSampler.setPort(80);
		baiducomSampler.setPath("/");
		baiducomSampler.setMethod("GET");
		baiducomSampler.setName("Open_baidu.com");
		baiducomSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
		baiducomSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

		// Loop Controller 循环控制
		LoopController loopController = new LoopController();
		loopController.setLoops(1);
		loopController.setFirst(true);
		loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
		loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
		loopController.initialize();

		// Thread Group 线程组
		ThreadGroup threadGroup = new ThreadGroup();
		threadGroup.setName("ZhengJin_ThreadGroup");
		threadGroup.setNumThreads(1);
		threadGroup.setRampUp(1);
		threadGroup.setSamplerController(loopController);
		threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
		threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

		// Test Plan 测试计划
		TestPlan testPlan = new TestPlan("ZhengJin_Plan");
		testPlan.setSerialized(true);
		testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
		testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

		// 从以上初始化的元素构造测试计划
		// JMeter测试计划，基本上是JOrphan HashTree
		HashTree testPlanTree = new HashTree();
		testPlanTree.add(testPlan);

		HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
		threadGroupHashTree.add(baiducomSampler);

		// 将生成的测试计划保存为JMeter的.jmx文件格式
		SaveService.loadProperties();
		SaveService.saveTree(testPlanTree, new FileOutputStream("/tmp/example.jmx"));

		// 在stdout中添加summary输出，得到测试进度
		String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
		Summariser summer = new Summariser(summariserName);

		// 将执行结果存储到.jtl文件中
		final String jtlFilePath = "/tmp/example.jtl";
		File jtlFile = new File(jtlFilePath);
		if (jtlFile.exists()) {
			jtlFile.delete();
		}
		ResultCollector logger = new ResultCollector(summer);
		logger.setFilename(jtlFilePath);
		testPlanTree.add(testPlanTree.getArray()[0], logger);

		// 单机执行测试计划
		// 初始化压测引擎
		StandardJMeterEngine jmeter = new StandardJMeterEngine();
		jmeter.configure(testPlanTree);
		jmeter.run();

		LOG.info("Create jmx file: /tmp/example.jmx");
		LOG.info("Create jtl file: {}", jtlFilePath);
		LOG.info("Jmeter App Done.");
	}

}
