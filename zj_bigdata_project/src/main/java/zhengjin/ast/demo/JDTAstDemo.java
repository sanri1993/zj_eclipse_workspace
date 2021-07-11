package zhengjin.ast.demo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class JDTAstDemo {

	public static void main(String[] args) throws IOException {

		System.out.println("JDTAstSample");
		JDTAstSample();

		System.out.println("\n\nvisitorMethods");
		visitorMethods();

		System.out.println("JDT ast sample Done.");
	}

	/**
	 * ASTVisitor Refer:
	 * https://help.eclipse.org/2020-09/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/ASTVisitor.html
	 */
	private static void JDTAstSample() {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(
				"public class A { int i = 9;  \n int j; \n ArrayList<Integer> al = new ArrayList<Integer>();j=1000; }"
						.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {

			Set<String> names = new HashSet<>();

			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				this.names.add(name.getIdentifier());
				System.out.println(String.format("Declaration of %s at line %d", name.getIdentifier(),
						cu.getLineNumber(name.getStartPosition())));
				// false: do not continue to do not print usage info
				return false;
			}

			public boolean visit(SimpleName node) {
				if (this.names.contains(node.getIdentifier())) {
					System.out.println(String.format("Use of %s at line %d", node.getIdentifier(),
							cu.getLineNumber(node.getStartPosition())));
				}
				return true;
			}
		});
	}

	private static void visitorMethods() throws IOException {
		String path = "/tmp/test/Application.java";
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		String source = FileUtils.readFileToString(new File(path));
		if (source.length() == 0) {
			throw new IOException("File is empty: " + path);
		}

		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {

			public boolean visit(MethodDeclaration node) {
				int startPos = node.getStartPosition();
				System.out.println("\nMethod name:" + node.getName().getIdentifier());
				System.out.println(String.format("startLine: %d, startCol: %d", cu.getLineNumber(startPos),
						cu.getColumnNumber(startPos)));

				List<String> stats = Arrays.asList(node.getBody().toString().split("\n"));
				System.out.println("stats count: " + stats.stream().map(stat -> stat.trim()).count());
				stats.stream().forEach(System.out::println);

				node.getBody().accept(new ASTVisitor() {

					public boolean visit(ReturnStatement node) {
						int pos = node.getStartPosition();
						System.out.println(String.format("retLine: %d, retCol: %d", cu.getLineNumber(pos),
								cu.getColumnNumber(pos)));
						return false;
					}
				});

				return false;
			}
		});
	}

}
