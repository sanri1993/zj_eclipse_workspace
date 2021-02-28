package zhengjin.asm.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public final class Tools {

	/**
	 * Save ASM modified bytes to .class file.
	 * 
	 * @param bytes
	 * @param path
	 * @throws IOException
	 */
	public static void save(byte[] bytes, String path) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			fos.write(bytes);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		System.out.println("save bytes to file: " + path);
	}

	public static Class<?> loadJAR(String path, String className) throws ClassNotFoundException, IOException {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException("JAR file not found: " + path);
		}

		Class<?> clz = null;
		URLClassLoader loader = null;
		try {
			URL url = file.toURI().toURL();
			loader = new URLClassLoader(new URL[] { url }, Thread.currentThread().getContextClassLoader());
			clz = loader.loadClass(className);
		} finally {
			if (loader != null) {
				loader.close();
			}
		}
		return clz;
	}

	public static Class<?> loadClass(String path) throws IOException {
		File classFile = new File(path);
		if (!classFile.exists()) {
			throw new FileNotFoundException(".class file not found: " + path);
		}

		MyClassLoader loader = new MyClassLoader();
		return loader.loadClass(classFile);
	}

	public static void printDeclaredMethodsAndFields(Class<?> clazz) {
		System.out.println("class fields:");
		for (Field field : clazz.getDeclaredFields()) {
			System.out.println(field.getName());
		}

		System.out.println("class methods:");
		for (Method method : clazz.getDeclaredMethods()) {
			System.out.println(method.getName());
		}
	}

	public static void main(String[] args) throws Exception {

		System.out.println("Application class hashcode: " + Application.class.hashCode());

		// after load jar, currently it contains duplicated class "Application", and it
		// does not overwrite existing class "Application".
		System.out.println("\nload jar, and print class info:");
		Class<?> clz = loadJAR("/tmp/test/app.jar", "zhengjin.asm.demo.Application");
		System.out.println("load (jar) Application class hashcode: " + clz.hashCode());
		printDeclaredMethodsAndFields(clz);

		System.out.println("\nload .class, and print class info:");
		Class<?> clazz = loadClass("/tmp/test/ApplicationModifiedByTreeApi.class");
		System.out.println("load (.class) Application class hashcode: " + clazz.hashCode());
		printDeclaredMethodsAndFields(clazz);
	}

}
