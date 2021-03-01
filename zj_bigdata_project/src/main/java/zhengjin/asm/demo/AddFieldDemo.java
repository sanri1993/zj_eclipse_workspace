package zhengjin.asm.demo;

import java.io.FileInputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class AddFieldDemo {

	/**
	 * Add a field for class and save to .class file by core API.
	 * 
	 * @param coreApiSavePath
	 * @throws IOException
	 */
	public void addFieldByCoreAPI(String coreApiSavePath) throws IOException {
		ClassReader cr = new ClassReader(Application.class.getName());
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

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
		Tools.save(cw.toByteArray(), coreApiSavePath);
	}

	/**
	 * Add a field for class and save to .class file by tree API.
	 * 
	 * @param treeApiSavePath
	 * @throws IOException
	 */
	public void addFiledByTreeAPI(String treeApiSavePath) throws IOException {
		ClassReader cr = new ClassReader(Application.class.getName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, Opcodes.ASM5);

		FieldNode fn = new FieldNode(Opcodes.ACC_PUBLIC, "name", Type.getDescriptor(String.class), null, "demo");
		cn.fields.add(fn);

		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		Tools.save(cw.toByteArray(), treeApiSavePath);
	}

	/**
	 * Read bytes from .class file and add a field.
	 * 
	 * @param readPath
	 * @param savePath
	 * @return
	 * @throws IOException
	 */
	public Class<?> readClassFileAndAddField(String readPath) throws IOException {
		FileInputStream fis = new FileInputStream(readPath);
		ClassReader reader = new ClassReader(fis);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, writer) {

			@Override
			public void visitEnd() {
				FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC, "c", Type.getDescriptor(int.class), null, 3);
				fv.visitEnd();
			}
		};
		reader.accept(cv, ClassReader.SKIP_DEBUG);

		MyClassLoader loader = new MyClassLoader();
		return loader.defineClass(writer.toByteArray());
	}

}
