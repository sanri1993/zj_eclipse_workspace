package zhengjin.jmeter.javasampler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class JavaSamplerTcp01 extends AbstractJavaSamplerClient {

	private static String host = null;
	private static int port;

	private static ThreadLocal<PrintWriter> outHolder = new ThreadLocal<PrintWriter>();

	private static ThreadLocal<Socket> socketHolder = new ThreadLocal<Socket>() {

		protected Socket initialValue() {
			Socket socket = new Socket();
			try {
				socket.setKeepAlive(true);
				socket.connect(new InetSocketAddress(host, port));
				if (socket.isConnected()) {
					PrintWriter out = new PrintWriter(socket.getOutputStream());
					outHolder.set(out);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			return socket;
		}
	};

	private PrintWriter out = null;

	@Override
	public void setupTest(JavaSamplerContext context) {
		host = context.getParameter("host");
		port = context.getIntParameter("port");
		socketHolder.get();
		out = outHolder.get();
		System.out.println("setupTest:" + Thread.currentThread().getId());
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult result = getSampleResult();
		result.sampleStart();

		out.write(Thread.currentThread().getId() + ":Hello JavaSamplerClient!");
		out.flush();
		result.setResponseData((Thread.currentThread().getId() + ":success!").getBytes());
		result.sampleEnd();
		result.setSuccessful(true);
		return result;
	}

	private SampleResult getSampleResult() {
		SampleResult result = new SampleResult();
		String label = "TCPSampler" + Thread.currentThread().getId();
		result.setSampleLabel(label);
		return result;
	}

	@Override
	public void teardownTest(JavaSamplerContext context) {
		Socket socket = socketHolder.get();
		try {
			out.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("teardownTest:" + Thread.currentThread().getId());
	}

}
