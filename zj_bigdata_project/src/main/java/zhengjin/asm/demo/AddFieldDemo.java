package zhengjin.asm.demo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

public class AddFieldDemo {

	String coreApiSavePath;
	String treeApiSavePath;

	public AddFieldDemo(String coreApiSavePath, String treeApiSavePath) {
		this.coreApiSavePath = coreApiSavePath;
		this.treeApiSavePath = treeApiSavePath;
	}

	public void addFieldByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassWriter cw = new ClassWriter(0);

		@SuppressWarnings("restriction")
		ClassVisitor cv = new ClassVisitor(ASM5, cw) {

			@Override
			public void visitEnd() {
				super.visitEnd();

				FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC, "name", "Ljava/lang/String;", null, "demo");
				if (fv != null) {
					System.out.println("field added.");
					fv.visitEnd();
				}
			}
		};
		cr.accept(cv, 0);
		Tools.save(cw.toByteArray(), this.coreApiSavePath);
	}

	@SuppressWarnings("restriction")
	public void addFiledByTreeAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, ASM5);

		FieldNode fn = new FieldNode(Opcodes.ACC_PUBLIC, "name", "Ljava/lang/String;", null, "demo");
		cn.fields.add(fn);

		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		Tools.save(cw.toByteArray(), this.treeApiSavePath);
	}

	public static void main(String[] args) throws Exception {

		// see modified bytecode:
		// javap -verbose ApplicationModified.class

		String coreApiSavePath = "/tmp/test/ApplicationModifiedByCoreApi.class";
		String treeApiSavePath = "/tmp/test/ApplicationModifiedByTreeApi.class";
		AddFieldDemo demo = new AddFieldDemo(coreApiSavePath, treeApiSavePath);

		System.out.println("add field for class by core API:");
		demo.addFieldByCoreAPI();
		Class<?> clazz = Tools.loadClass(coreApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		System.out.println("\nadd field for class by tree API:");
		demo.addFiledByTreeAPI();
		clazz = Tools.loadClass(treeApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);
	}

}
