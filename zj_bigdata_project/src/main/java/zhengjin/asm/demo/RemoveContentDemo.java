package zhengjin.asm.demo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

public class RemoveContentDemo {

	/**
	 * Remove a field and method from Application class, and save to .class file.
	 * 
	 * @throws IOException
	 */
	public void removeContentByCoreAPI(String classFilePath, String savePath) throws IOException {
		ClassReader cr = Tools.getClassReaderFromClassFile(classFilePath);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		ClassVisitor cv = new ClassVisitor(ASM5, cw) {

			@Override
			public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
				if ("a".equals(name)) {
					return null;
				}
				return super.visitField(access, name, descriptor, signature, value);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				if ("test01".equals(name)) {
					return null;
				}
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};

		cr.accept(cv, 0);
		Tools.save(cw.toByteArray(), savePath);
	}

}
