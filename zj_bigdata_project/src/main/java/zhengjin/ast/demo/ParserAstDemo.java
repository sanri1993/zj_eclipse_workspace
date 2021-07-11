package zhengjin.ast.demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import com.github.javaparser.StaticJavaParser;

public class ParserAstDemo {

	public static void main(String[] args) throws IOException {

		System.out.println("java parser demo");
		javaParserDemo();

		System.out.println("\njdt parser demo");
		jdtParserDemo();

		System.out.println("\ndiff methods:");
		String file1 = "/tmp/test/Application1.java";
		String file2 = "/tmp/test/Application2.java";
		List<String> methods = MethodDiff.methodDiffInClass(file1, file2);
		System.out.println("\ndiff results:");
		methods.forEach(item -> System.out.println(item));
	}

	private static void javaParserDemo() throws IOException {
		String filePath = "/tmp/test/Application.java";
		String input = readFileContent(filePath);

		com.github.javaparser.ast.CompilationUnit cu = StaticJavaParser.parse(input);
		cu.findAll(com.github.javaparser.ast.PackageDeclaration.class).forEach(item -> {
			System.out.println("package: " + item.getName());
		});
	}

	private static void jdtParserDemo() throws IOException {
		String filePath = "/tmp/test/Application.java";
		String input = readFileContent(filePath);

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(input.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		PackageDeclaration pkg = unit.getPackage();
		System.out.println("package: " + pkg.getName());
	}

	private static String readFileContent(String filePath) throws IOException {
		File file = new File(filePath);
		byte[] bytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
		return new String(bytes, StandardCharsets.UTF_8);

	}

}
