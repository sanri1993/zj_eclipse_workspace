package zhengjin.asm.demo;

import java.io.IOException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ASM5;

public class AddMethodDemo {

	String coreAPISavePath;
	String treeAPISavePath;

	public AddMethodDemo(String coreAPISavePath, String treeAPISavePath) {
		this.coreAPISavePath = coreAPISavePath;
		this.treeAPISavePath = treeAPISavePath;
	}

	public void addMethodByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(ASM5, cw) {

			@Override
			public void visitEnd() {
				super.visitEnd();
				// 添加add方法
				MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "add", "(II)I", null, null);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitVarInsn(Opcodes.ILOAD, 2);
				mv.visitInsn(Opcodes.IADD);
				mv.visitInsn(Opcodes.IRETURN);
				mv.visitMaxs(2, 3);
				mv.visitEnd();
			}
		};
		cr.accept(cv, 0);
		Tools.save(cw.toByteArray(), this.coreAPISavePath);
	}

	public void addMethodByTreeAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, ASM5);

		// 添加add方法
		MethodNode fn = new MethodNode(Opcodes.ACC_PUBLIC, "add", "(II)I", null, null);
		InsnList il = fn.instructions;
		il.add(new VarInsnNode(Opcodes.ILOAD, 1));
		il.add(new VarInsnNode(Opcodes.ILOAD, 2));
		il.add(new InsnNode(Opcodes.IADD));
		il.add(new InsnNode(Opcodes.IRETURN));
		fn.maxStack = 2;
		fn.maxLocals = 3;
		cn.methods.add(fn);

		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		Tools.save(cw.toByteArray(), this.treeAPISavePath);
	}

	public static void main(String[] args) throws Exception {

		String coreAPISavePath = "/tmp/test/ApplicationModifiedByCoreApi.class";
		String treeAPISavePath = "/tmp/test/ApplicationModifiedByTreeApi.class";
		AddMethodDemo demo = new AddMethodDemo(coreAPISavePath, treeAPISavePath);

		System.out.println("add method for class by core API:");
		demo.addMethodByCoreAPI();
		Class<?> clazz = Tools.loadClass(coreAPISavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method add = clazz.getMethod("add", int.class, int.class);
		Object result = add.invoke(clazz.newInstance(), 10, 20);
		System.out.println("\nadd results: " + (Integer) result);

		System.out.println("\nadd method for class by tree API:");
		demo.addMethodByTreeAPI();
		clazz = Tools.loadClass(treeAPISavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		add = clazz.getMethod("add", int.class, int.class);
		result = add.invoke(clazz.newInstance(), 15, 25);
		System.out.println("\nadd results: " + (Integer) result);
	}

}
