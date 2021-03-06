package zhengjin.asm.demo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class AdviceMethodDemo {

	/**
	 * Modify a method of Application, and save to .class file by advice API.
	 * 
	 * @param classFilePath
	 * @param savePath
	 * @throws IOException
	 */
	public void modifyMethodByAdvice(String classFilePath, String savePath) throws IOException {
		ClassReader cr = Tools.getClassReaderFromClassFile(classFilePath);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM7, cw) {

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
				if (!"test03".equals(name)) {
					return mv;
				}

				return new AdviceAdapter(Opcodes.ASM7, mv, access, name, descriptor) {

					@Override
					protected void onMethodEnter() {
						super.onMethodEnter();
						mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
						mv.visitLdcInsn("enter method");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
								false);
					}

					@Override
					protected void onMethodExit(int opcode) {
						super.onMethodExit(opcode);
						mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
						if (opcode == Opcodes.ATHROW) {
							mv.visitLdcInsn("out method -- exception");
						} else {
							mv.visitLdcInsn("out method -- normal");
						}
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
								false);
					}
				};
			}
		};

		cr.accept(cv, 0);
		Tools.save(cw.toByteArray(), savePath);
	}

}
