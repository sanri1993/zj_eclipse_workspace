package zhengjin.jmeter.javasampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class JavaSampler02 extends AbstractJavaSamplerClient {

	private String username;
	private String password;

	String requset_str = "";
	String res = "";

	@Override
	public void setupTest(JavaSamplerContext context) {
		super.setupTest(context);
	}

	@Override
	public void teardownTest(JavaSamplerContext context) {
		super.teardownTest(context);
	}

	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();
		params.addArgument("username", "apollo");
		params.addArgument("password", "abcd1234");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult sr = new SampleResult();
		sr.sampleStart();
		this.username = context.getParameter("username");
		this.password = context.getParameter("password");
		requset_str = "username:" + username + "password:" + password;

		Http javaHttp = new Http();
		try {
			res = javaHttp.Post(requset_str, 1);
		} catch (IOException e) {
			sr.setSamplerData(requset_str);
			sr.setResponseData(e.getMessage(), "utf-8");
			sr.setSuccessful(false);
			sr.sampleEnd();
			e.printStackTrace();
		}

		String arr[] = res.split("|");
		if (arr[0].equals("200")) {
			sr.setSamplerData(requset_str);
			sr.setResponseData(arr[1], "utf-8");
			sr.setSuccessful(true);
			sr.setResponseCode("200");
			sr.setResponseCodeOK();
			sr.sampleEnd();
		} else {
			sr.setSamplerData(requset_str);
			sr.setResponseData(arr[1], "utf-8");
			sr.setSuccessful(false);
			sr.sampleEnd();
		}

		return null;
	}

	private static class Http {

		public String Post(String content, int seq) throws IOException {
			URL url = new URL("http://scottishroots.com/");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
			connection.setConnectTimeout(30 * 1000);
			connection.setReadTimeout(30 * 1000);
			connection.connect();

			PrintWriter printWriter = new PrintWriter(connection.getOutputStream());

			try {
				printWriter.write(content);
				printWriter.flush();
			} finally {
				if (printWriter != null) {
					printWriter.close();
				}
			}

			String res = "not response";
			String code = "600";
			InputStream is = connection.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				String responseCode = (new Integer(connection.getResponseCode()).toString());
				String Message = (connection.getResponseMessage());
				if (!(responseCode.equals("200")) || !(Message.equals("OK"))) {
					code = "200";
				}

				int len = 0;
				byte[] buf = new byte[1024];
				while ((len = is.read(buf)) != -1) {
					baos.write(buf, 0, len);
					res = baos.toString();

				}
				baos.flush();
				System.out.println("res: " + res);

			} finally {
				if (baos != null) {
					baos.close();
				}
				if (is != null) {
					is.close();
				}
			}

			return code + "|" + res;
		}
	}

}
