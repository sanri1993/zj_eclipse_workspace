package zhengjin.jmeter.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Common {

	private static final Logger LOG = LoggerFactory.getLogger(Common.class);

	public static String getCurrentPath() {
		return System.getProperty("user.dir");
	}

	public static String readFileContent(String filepath) throws IOException {
		if (!new File(filepath).exists()) {
			throw new FileNotFoundException("file not found: " + filepath);
		}
		byte[] data = Files.readAllBytes(Paths.get(filepath));
		return new String(data, StandardCharsets.UTF_8);
	}

	public static void appendContentToFile(String path, String content) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(path, true));
			out.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String readResource(String path) throws IOException {
		InputStream in = Object.class.getResourceAsStream(path);
		if (in == null) {
			LOG.warn("getResourceAsStream returns null.");
			return "";
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			if (br != null) {
				br.close();
			}

			if (in != null) {
				in.close();
			}
		}

		return sb.toString();
	}

}
