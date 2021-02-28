package zhengjin.asm.demo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.esotericsoftware.reflectasm.shaded.org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM5;

public class ModifyMethodDemo {

	String coreAPISavePath;
	String treeAPISavePath;

	private String owner = Type.getInternalName(Application.class);

	public ModifyMethodDemo(String coreAPISavePath, String treeAPISavePath) {
		this.coreAPISavePath = coreAPISavePath;
		this.treeAPISavePath = treeAPISavePath;
	}

	public void modifyMethodByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(ASM5, cw) {

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
					mv.visitMaxs(2, 2);
					mv.visitEnd();
					return mv;
				}
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};

		cr.accept(cv, 0);
		Tools.save(cw.toByteArray(), this.coreAPISavePath);
	}

	public void modifyMethodByTreeAPI() throws IOException {
		ClassReader cr = new ClassReader(Application.class.getCanonicalName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, ASM5);

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
		Tools.save(cw.toByteArray(), this.treeAPISavePath);
	}

	public static void main(String[] args) throws Exception {

		String coreApiSavePath = "/tmp/test/ApplicationModifiedByCoreApi.class";
		String treeApiSavePath = "/tmp/test/ApplicationModifiedByTreeApi.class";
		ModifyMethodDemo demo = new ModifyMethodDemo(coreApiSavePath, treeApiSavePath);

		// ERROR: java.lang.ClassFormatError
//		Tools.loadClass(coreApiSavePath);

		System.out.println("modify method for class by tree api:");
		demo.modifyMethodByTreeAPI();
		Class<?> clazz = Tools.loadClass(treeApiSavePath);
		Tools.printDeclaredMethodsAndFields(clazz);

		Method test01 = clazz.getMethod("test01", int.class);
		Integer result = (Integer) test01.invoke(clazz.newInstance(), 3);
		System.out.println("\napplication test01 results: " + result);
	}

}
