package zhengjin.asm.demo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class TestAsm {

	// build jar package:
	// jar -cvf app.jar Application.class
	//
	// see java bytecode:
	// javap -verbose Application.class

	private String classFilePath = "/tmp/test/Application.class";

	@Test
	public void testSaveBytesToClassFile() throws IOException {
		String className = Application.class.getName();
		System.out.println("save class: " + className);

		ClassReader cr = new ClassReader(className);
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public void visitEnd() {
				System.out.println("class visit end.");
				super.visitEnd();
			}
		};
		cr.accept(cv, 0);

		String savePath = "/tmp/test/SavedApplication.class";
		Tools.save(cw.toByteArray(), savePath);
	}

	@Test
	public void testLoadClassFile() throws Exception {
		// copy Application.java with package changed as "test.asm.demo"
		System.out.println("Application class hashcode: " + Application.class.hashCode());

		Class<?> clazz = Tools.loadClass(this.classFilePath);
		System.out.println("load (.class) Application class hashcode: " + clazz.hashCode());
		Tools.printDeclaredMethodsAndFields(clazz);
	}

	@Test
	public void testAddFieldByCoreAPI() throws Exception {
		String coreApiSavePath = "/tmp/test/ApplicationAddFieldByCoreApi.class";
		AddFieldDemo demo = new AddFieldDemo();
		demo.addFieldByCoreAPI(this.classFilePath, coreApiSavePath);

		Class<?> clazz = Tools.loadClass(coreApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);
	}

	@Test
	public void testAddFieldByTreeAPI() throws Exception {
		String treeApiSavePath = "/tmp/test/ApplicationAddFieldByTreeApi.class";
		AddFieldDemo demo = new AddFieldDemo();
		demo.addFiledByTreeAPI(this.classFilePath, treeApiSavePath);

		Class<?> clazz = Tools.loadClass(treeApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);
	}

	@Test
	public void testReadClassFileAndAddField() throws Exception {
		AddFieldDemo demo = new AddFieldDemo();
		Class<?> clazz = demo.readClassFileAndAddField(this.classFilePath);
		Tools.printDeclaredMethodsAndFields(clazz);
	}

	@Test
	public void testAddMethodByCoreAPI() throws Exception {
		String coreAPISavePath = "/tmp/test/ApplicationAddMethodByCoreApi.class";
		AddMethodDemo demo = new AddMethodDemo();
		demo.addMethodByCoreAPI(this.classFilePath, coreAPISavePath);

		Class<?> clazz = Tools.loadClass(coreAPISavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method add = clazz.getMethod("add", int.class, int.class);
		add.setAccessible(true);
		Object result = add.invoke(clazz.newInstance(), 10, 20);
		System.out.println("\nadd results: " + (Integer) result);
	}

	@Test
	public void testAddMethodByTreeAPI() throws Exception {
		String coreAPISavePath = "/tmp/test/ApplicationAddMethodByCoreApi.class";
		AddMethodDemo demo = new AddMethodDemo();
		demo.addMethodByTreeAPI(this.classFilePath, coreAPISavePath);

		Class<?> clazz = Tools.loadClass(coreAPISavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method add = clazz.getMethod("add", int.class, int.class);
		add.setAccessible(true);
		Object result = add.invoke(clazz.newInstance(), 15, 25);
		System.out.println("\nadd results: " + (Integer) result);
	}

	@Test
	public void testremoveContentByCoreAPI() throws Exception {
		String path = "/tmp/test/ApplicationRemoved.class";
		RemoveContentDemo demo = new RemoveContentDemo();
		demo.removeContentByCoreAPI(this.classFilePath, path);

		Class<?> clazz = Tools.loadClass(path);
		// NoSuchFieldError: a
		// clazz.newInstance();
		System.out.println("class fields:");
		for (Field field : clazz.getDeclaredFields()) {
			System.out.println(field.getName());
		}

		System.out.println("class methods:");
		for (Method method : clazz.getDeclaredMethods()) {
			System.out.println(method.getName());
		}
	}

	@Test
	public void testModifyMethodByCoreAPI() throws Exception {
		// modified method invokes variable and method in package, so we cannot use
		// class from .class file whose package is updated.
		String coreApiSavePath = "/tmp/test/ApplicationModifyMethodByCoreApi.class";
		ModifyMethodDemo demo = new ModifyMethodDemo();
		demo.modifyMethodByCoreAPI(coreApiSavePath);

		Class<?> clazz = Tools.loadClass(coreApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method test01 = clazz.getMethod("test01", int.class);
		test01.setAccessible(true);
		Integer result = (Integer) test01.invoke(clazz.newInstance(), 6);
		System.out.println("\napplication test01 results: " + result);
	}

	@Test
	public void testModifyMethodByTreeAPI() throws Exception {
		String treeApiSavePath = "/tmp/test/ApplicationModifyMethodByTreeApi.class";
		ModifyMethodDemo demo = new ModifyMethodDemo();
		demo.modifyMethodByTreeAPI(treeApiSavePath);

		Class<?> clazz = Tools.loadClass(treeApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method test01 = clazz.getMethod("test01", int.class);
		test01.setAccessible(true);
		Integer result = (Integer) test01.invoke(clazz.newInstance(), 3);
		System.out.println("\napplication test01 results: " + result);
	}

	@Test
	public void testModifyMethodByAdvice() throws Exception {
		String savePath = "/tmp/test/ApplicationModifyMethodByAdvice.class";
		AdviceMethodDemo demo = new AdviceMethodDemo();
		demo.modifyMethodByAdvice(this.classFilePath, savePath);

		Class<?> clazz = Tools.loadClass(savePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method test = clazz.getDeclaredMethod("test03");
		test.setAccessible(true);
		test.invoke(clazz.newInstance());
	}

}
