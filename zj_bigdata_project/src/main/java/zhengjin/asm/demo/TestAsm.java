package zhengjin.asm.demo;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class TestAsm {

	// build jar package:
	// jar -cvf app.jar Application.class
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

		Tools.save(cw.toByteArray(), this.classFilePath);

	}

	@Test
	public void testLoadClassFile() throws Exception {
		System.out.println("Application class hashcode: " + Application.class.hashCode());

		Class<?> clazz = Tools.loadClass(this.classFilePath);
		System.out.println("load (.class) Application class hashcode: " + clazz.hashCode());
		Tools.printDeclaredMethodsAndFields(clazz);
	}

	@Test
	public void testAddFieldByCoreAPI() throws Exception {
		String coreApiSavePath = "/tmp/test/ApplicationAddFieldByCoreApi.class";
		AddFieldDemo demo = new AddFieldDemo();
		demo.addFieldByCoreAPI(coreApiSavePath);

		Class<?> clazz = Tools.loadClass(coreApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);
	}

	@Test
	public void testAddFieldByTreeAPI() throws Exception {
		String treeApiSavePath = "/tmp/test/ApplicationAddFieldByTreeApi.class";
		AddFieldDemo demo = new AddFieldDemo();
		demo.addFiledByTreeAPI(treeApiSavePath);

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
		demo.addMethodByCoreAPI(coreAPISavePath);
		Class<?> clazz = Tools.loadClass(coreAPISavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method add = clazz.getMethod("add", int.class, int.class);
		Object result = add.invoke(clazz.newInstance(), 10, 20);
		System.out.println("\nadd results: " + (Integer) result);
	}

	@Test
	public void testAddMethodByTreeAPI() throws Exception {
		String coreAPISavePath = "/tmp/test/ApplicationAddMethodByCoreApi.class";
		AddMethodDemo demo = new AddMethodDemo();
		demo.addMethodByTreeAPI(coreAPISavePath);
		Class<?> clazz = Tools.loadClass(coreAPISavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method add = clazz.getMethod("add", int.class, int.class);
		Object result = add.invoke(clazz.newInstance(), 15, 25);
		System.out.println("\nadd results: " + (Integer) result);
	}

	@Test
	public void testModifyMethodByCoreAPI() throws Exception {
		String coreApiSavePath = "/tmp/test/ApplicationModifyMethodByCoreApi.class";
		ModifyMethodDemo demo = new ModifyMethodDemo();
		demo.modifyMethodByCoreAPI(coreApiSavePath);
		Class<?> clazz = Tools.loadClass(coreApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method test01 = clazz.getMethod("test01", int.class);
		Integer result = (Integer) test01.invoke(clazz.newInstance(), 2);
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
		Integer result = (Integer) test01.invoke(clazz.newInstance(), 3);
		System.out.println("\napplication test01 results: " + result);
	}

}
