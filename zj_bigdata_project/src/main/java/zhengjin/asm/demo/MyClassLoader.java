package zhengjin.asm.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class MyClassLoader extends ClassLoader {

	public Class<?> loadClass(File classFile) throws IOException {
		int len = 0;
		byte[] bytes = new byte[(int) classFile.length()];

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(classFile);
			while (true) {
				int res = fis.read(bytes);
				if (res == -1) {
					break;
				}
				len += res;
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		return super.defineClass(null, bytes, 0, len);
	}

	public Class<?> defineClass(byte[] bytes) {
		return super.defineClass(null, bytes, 0, bytes.length);
	}

}
