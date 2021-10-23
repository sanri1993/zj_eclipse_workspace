package zhengjin.asm.demo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectTimerClassVisitor extends ClassVisitor {

	public InjectTimerClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM7, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		System.out.println(String.format("visitMethod, name: %s, desc: %s", name, desc));
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new InjectTimerMethodVisitor(Opcodes.ASM7, mv, access, name, desc);
	}

}
