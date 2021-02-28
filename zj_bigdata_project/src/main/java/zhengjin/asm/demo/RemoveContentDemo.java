package zhengjin.asm.demo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

/**
 * Remove field and method of class by asm.
 *
 */
public class RemoveContentDemo {

	String savePath;

	public RemoveContentDemo(String path) {
		this.savePath = path;
	}

	public void removeContentByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassWriter cw = new ClassWriter(0);

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
		Tools.save(cw.toByteArray(), this.savePath);
	}

	public static void main(String[] args) throws Exception {

		String path = "/tmp/test/ApplicationModified.class";
		RemoveContentDemo demo = new RemoveContentDemo(path);

		System.out.println("delete field and method for class:");
		demo.removeContentByCoreAPI();
		Class<?> clazz = Tools.loadClass(path);
		Tools.printDeclaredMethodsAndFields(clazz);
	}

}
