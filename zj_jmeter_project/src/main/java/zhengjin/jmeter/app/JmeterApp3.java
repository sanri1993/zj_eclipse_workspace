package zhengjin.jmeter.app;

import java.io.File;
import java.io.IOException;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1. 加载已有jmx文件并解析 2. 生成.csv格式结果
 * 
 * @author zhengjin
 *
 */
public final class JmeterApp3 {

	private static final Logger LOG = LoggerFactory.getLogger(JmeterApp3.class);

	public static void main(String[] args) throws IOException {

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

		JMeterUtils.setJMeterHome(jmeterHome.getPath());
		JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
		JMeterUtils.initLocale();

		// 设置jmx脚本文件的工作目录，可以根据这个来找到参数化文件及实现其文件流
		File jmxFile = new File("/tmp/example.jmx");
		FileServer.getFileServer().setBaseForScript(jmxFile);

		// 加载jmx脚本，本身这个操作非常复杂
		HashTree testPlanTree = SaveService.loadTree(jmxFile);
		// 去掉没用的节点元素，替换掉可以替换的控制器，这个是递归实现的，比较复杂
		JMeter.convertSubTree(testPlanTree);

		String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
		Summariser summer = new Summariser(summariserName);

		// 将执行结果存储到.csv文件中
		final String jtlFilePath = "/tmp/example.csv";
		File jtlFile = new File(jtlFilePath);
		if (jtlFile.exists()) {
			jtlFile.delete();
		}
		ResultCollector logger = new ResultCollector(summer);
		logger.setFilename(jtlFilePath);
		testPlanTree.add(testPlanTree.getArray()[0], logger);

		// 单机执行测试计划
		StandardJMeterEngine jmeter = new StandardJMeterEngine();
		jmeter.configure(testPlanTree);
		jmeter.run();

		LOG.info("Load jmx file: {}", jmxFile);
		LOG.info("Create jtl file: {}", jtlFilePath);
		LOG.info("Jmeter App Done.");
	}

}
