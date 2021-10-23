package zhengjin.asm.demo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class InjectTimerMethodVisitor extends AdviceAdapter {

	private boolean inject;

	protected InjectTimerMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name,
			String descriptor) {
		super(api, methodVisitor, access, name, descriptor);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (Type.getDescriptor(ASMTest.class).equals(desc)) {
			System.out.println("visitAnnotation: " + desc);
			this.inject = true;
		}
		return super.visitAnnotation(desc, visible);
	}

	private int start;

	/**
	 * insert on method enter:
	 * long start = System.currentTimeMillis();
	 */
	@Override
	protected void onMethodEnter() {
		super.onMethodEnter();
		if (this.inject) {
			this.invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
			start = this.newLocal(Type.LONG_TYPE);
			this.storeLocal(start);
		}
	}

	/**
	 * insert on method exit:
	 * long end = System.currentTimeMillis();
	 * System.out.println("take time: " + (end - start));
	 */
	@Override
	protected void onMethodExit(int opcode) {
		super.onMethodExit(opcode);
		if (this.inject) {
			this.invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
			int end = this.newLocal(Type.LONG_TYPE);
			this.storeLocal(end);

			this.getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"));
			this.newInstance(Type.getType("Ljava/lang/StringBuilder;"));
			this.dup();
			this.invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"), new Method("<init>", "()V"));
			this.visitLdcInsn("take time: ");

			this.invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"),
					new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
			this.loadLocal(end);
			this.loadLocal(start);
			this.math(SUB, Type.LONG_TYPE);

			invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"),
					new Method("append", "(J)Ljava/lang/StringBuilder;"));
			invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("toString", "()Ljava/lang/String;"));
			invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"));
		}
	}

}
