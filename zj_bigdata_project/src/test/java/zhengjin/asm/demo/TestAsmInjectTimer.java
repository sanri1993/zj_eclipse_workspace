package zhengjin.asm.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class TestAsmInjectTimer {

	// build Application.class:
	// cd ${HOME}/Workspaces/zj_repos/zj_eclipse_workspace/zj_bigdata_project/src/main/java/zhengjin/asm/demo
	// javac ASMTest.java Application.java
	// mv ASMTest.class Application.class /tmp/test

	String classRoot = "/tmp/test";
	String outPath = this.classRoot + File.separator + "InjectTimerApplication.class";

	@Test
	public void TestInjectTimer() throws IOException {
		// 1. 读取class文件
		String inPath = this.classRoot + File.separator + "Application.class";
		FileInputStream fis = new FileInputStream(inPath);

		// 2. 执行分析与插桩
		ClassReader cr = new ClassReader(fis);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cr.accept(new InjectTimerClassVisitor(cw), ClassReader.EXPAND_FRAMES);

		// 3. 获得结果并输
		FileOutputStream fos = new FileOutputStream(this.outPath);
		fos.write(cw.toByteArray());

		if (fis != null) {
			fis.close();
		}
		if (fos != null) {
			fos.close();
		}
		System.out.println("asm inject timer done");
	}

	@Test
	public void LoadInjectTimerClass() throws Exception {
		Class<?> clazz = Tools.loadClass(this.outPath);
		Tools.printDeclaredMethodsAndFields(clazz);
		System.out.println();

		Method sayHello = clazz.getDeclaredMethod("sayHello");
		sayHello.invoke(clazz.newInstance());
	}

}
