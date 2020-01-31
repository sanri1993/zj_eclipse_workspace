package zhengjin.jmeter.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Common {

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

}
