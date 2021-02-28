package zhengjin.asm.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class MyClassLoader extends ClassLoader {

	public Class<?> loadClass(File classFile) throws IOException {
		byte[] bytes = new byte[(int) classFile.length()];
		FileInputStream fis = null;
		int len = 0;

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

	public static void main(String[] args) throws Exception {

		File classFile = new File("/tmp/test/ApplicationModifiedByTreeApi.class");
		MyClassLoader loader = new MyClassLoader();
		Class<?> clazz = loader.loadClass(classFile);

		System.out.println("class fields:");
		for (Field field : clazz.getDeclaredFields()) {
			System.out.println(field.getName());
		}

		System.out.println("class methods:");
		for (Method method : clazz.getDeclaredMethods()) {
			System.out.println(method.getName());
		}
	}

}
