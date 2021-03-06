package zhengjin.asm.demo;

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
	 * Add a field for Application class and save to .class file by core API.
	 * 
	 * @param coreApiSavePath
	 * @throws IOException
	 */
	public void addFieldByCoreAPI(String classFilePath, String coreApiSavePath) throws IOException {
		ClassReader cr = Tools.getClassReaderFromClassFile(classFilePath);
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public void visitEnd() {
				super.visitEnd();

				FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC, "name", Type.getDescriptor(String.class), null,
						"demo");
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
	 * Add a field for Application class and save to .class file by tree API.
	 * 
	 * @param treeApiSavePath
	 * @throws IOException
	 */
	public void addFiledByTreeAPI(String classFilePath, String treeApiSavePath) throws IOException {
		ClassReader cr = Tools.getClassReaderFromClassFile(classFilePath);
		ClassNode cn = new ClassNode();
		cr.accept(cn, Opcodes.ASM5);

		FieldNode fn = new FieldNode(Opcodes.ACC_PUBLIC, "name", Type.getDescriptor(String.class), null, "demo");
		cn.fields.add(fn);

		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		Tools.save(cw.toByteArray(), treeApiSavePath);
	}

	/**
	 * Get class from .class file, and add a field for class.
	 * 
	 * @param readPath
	 * @return Modified class.
	 * @throws IOException
	 */
	public Class<?> readClassFileAndAddField(String readPath) throws IOException {
		ClassReader reader = Tools.getClassReaderFromClassFile(readPath);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

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
