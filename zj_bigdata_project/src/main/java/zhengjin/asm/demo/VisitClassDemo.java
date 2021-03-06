package zhengjin.asm.demo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class VisitClassDemo {

	/**
	 * Visit Application class fields and methods by core API.
	 * 
	 * @throws IOException
	 */
	public void visitByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getName());
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
				System.out.println("field in visitor: " + name);
				return super.visitField(access, name, descriptor, signature, value);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				System.out.println("method in visitor: " + name);
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};

		cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
	}

	/**
	 * Visit Application class fields and methods by tree API.
	 * 
	 * @throws IOException
	 */
	public void visitByTreeAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

		for (FieldNode fn : cn.fields) {
			System.out.println("field in visitor: " + fn.name);
		}

		for (MethodNode mn : cn.methods) {
			System.out.println("method in visitor: " + mn.name);
		}
	}

	public static void main(String[] args) throws Exception {

		VisitClassDemo visit = new VisitClassDemo();
		System.out.println("visit class by core API:");
		visit.visitByCoreAPI();

		System.out.println("\nvisit class by tree API:");
		visit.visitByTreeAPI();
	}

}
