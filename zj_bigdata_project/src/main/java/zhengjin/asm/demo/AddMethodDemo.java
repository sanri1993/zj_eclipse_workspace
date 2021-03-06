package zhengjin.asm.demo;

import java.io.IOException;

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

public class AddMethodDemo {

	/**
	 * Add a method for Application class and save to .class file by core API.
	 * 
	 * @param coreAPISavePath
	 * @throws IOException
	 */
	public void addMethodByCoreAPI(String classFilePath, String coreAPISavePath) throws IOException {
		ClassReader cr = Tools.getClassReaderFromClassFile(classFilePath);
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

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
		Tools.save(cw.toByteArray(), coreAPISavePath);
	}

	/**
	 * Add a method for Application class and save to .class file by tree API.
	 * 
	 * @param treeAPISavePath
	 * @throws IOException
	 */
	public void addMethodByTreeAPI(String classFilePath, String treeAPISavePath) throws IOException {
		ClassReader cr = Tools.getClassReaderFromClassFile(classFilePath);
		ClassNode cn = new ClassNode();
		cr.accept(cn, Opcodes.ASM5);

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
		Tools.save(cw.toByteArray(), treeAPISavePath);
	}

}
