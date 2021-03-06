package zhengjin.asm.demo;

import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.esotericsoftware.reflectasm.shaded.org.objectweb.asm.Type;

public class ModifyMethodDemo {

	private String owner = Type.getInternalName(Application.class);

	/**
	 * Modify a method of Application, and save to .class file by core API.
	 * 
	 * @param coreAPISavePath
	 * @throws IOException
	 */
	public void modifyMethodByCoreAPI(String coreAPISavePath) throws IOException {
		ClassReader cr = new ClassReader(Application.class.getName());
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				if ("test01".equals(name)) {
					MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, name, "(I)I", null, exceptions);
					mv.visitCode();
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ModifyMethodDemo.this.owner, "test02", "()V", false);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, ModifyMethodDemo.this.owner, "a", "I");
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, ModifyMethodDemo.this.owner, "b", "I");
					mv.visitInsn(Opcodes.IADD);
					mv.visitVarInsn(Opcodes.ILOAD, 1);
					mv.visitInsn(Opcodes.IADD);
					mv.visitInsn(Opcodes.IRETURN);
//					mv.visitMaxs(2, 2);
					mv.visitEnd();
					return mv;
				}
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};

		cr.accept(cv, 0);
		Tools.save(cw.toByteArray(), coreAPISavePath);
	}

	/**
	 * Modify a method of Application, and save to .class file by tree API.
	 * 
	 * @param treeAPISavePath
	 * @throws IOException
	 */
	public void modifyMethodByTreeAPI(String treeAPISavePath) throws IOException {
		ClassReader cr = new ClassReader(Application.class.getName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, Opcodes.ASM5);

		final String methodName = "test01";
		MethodNode newmn = new MethodNode(Opcodes.ACC_PUBLIC, methodName, "(I)I", null, null);
		newmn.visitCode();
		newmn.visitVarInsn(Opcodes.ALOAD, 0);
		newmn.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ModifyMethodDemo.this.owner, "test02", "()V", false);
		newmn.visitVarInsn(Opcodes.ALOAD, 0);
		newmn.visitFieldInsn(Opcodes.GETFIELD, ModifyMethodDemo.this.owner, "a", "I");
		newmn.visitVarInsn(Opcodes.ALOAD, 0);
		newmn.visitFieldInsn(Opcodes.GETFIELD, ModifyMethodDemo.this.owner, "b", "I");
		newmn.visitInsn(Opcodes.IADD);
		newmn.visitVarInsn(Opcodes.ILOAD, 1);
		newmn.visitInsn(Opcodes.IADD);
		newmn.visitInsn(Opcodes.IRETURN);
		newmn.visitMaxs(2, 2);
		newmn.visitEnd();

		List<MethodNode> methods = cn.methods;
		for (int i = 0; i < methods.size(); i++) {
			MethodNode mn = methods.get(i);
			if (methodName.equals(mn.name)) {
				methods.set(i, newmn);
			}
		}

		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		Tools.save(cw.toByteArray(), treeAPISavePath);
	}

}
