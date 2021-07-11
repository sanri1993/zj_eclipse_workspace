package zhengjin.ast.demo;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Finds Changes Between Methods of Two Java Source Files.
 * 
 * Refer:
 * https://stackoverflow.com/questions/46795853/find-methods-with-modified-code-between-two-versions-of-a-java-class
 */
public class MethodDiff {

	private static PrettyPrinterConfiguration ppc = null;

	private static class ClassPair {

		final ClassOrInterfaceDeclaration clazz;
		final String name;

		ClassPair(ClassOrInterfaceDeclaration c, String n) {
			clazz = c;
			name = n;
		}

		@Override
		public String toString() {
			return String.format("name=%s,cls=%s", this.name, this.clazz.getName());
		}
	}

	private static class DiffResult {

		String src;
		String dst;

		DiffResult(String src, String dst) {
			this.src = src;
			this.dst = dst;
		}

		String getType() {
			if (this.src.length() == 0 && this.dst.length() > 0) {
				return "add";
			} else if (this.dst.length() == 0 && this.src.length() > 0) {
				return "delete";
			} else if (this.src == null || !this.src.equals(this.dst)) {
				return "change";
			} else {
				return "same";
			}
		}
	}

	public static PrettyPrinterConfiguration getPPC() {
		if (ppc != null) {
			return ppc;
		}
		PrettyPrinterConfiguration localPpc = new PrettyPrinterConfiguration();
		localPpc.setColumnAlignFirstMethodChain(false);
		localPpc.setColumnAlignParameters(false);
		localPpc.setEndOfLineCharacter("");
		localPpc.setIndentSize(0);
		localPpc.setPrintComments(false);
		localPpc.setPrintJavadoc(false);

		ppc = localPpc;
		return ppc;
	}

	public static List<String> methodDiffInClass(String srcFile, String dstFile) throws FileNotFoundException {
		Map<String, DiffResult> diffResults = new HashMap<>();

		// Load all method and constructor from srcFile
		MethodDiff md = new MethodDiff();
		List<ClassPair> clazzes = md.getClassesFromFile(srcFile);
		for (ClassPair classPair : clazzes) {
			List<ConstructorDeclaration> constructors = getChildNodesByClassType(classPair.clazz,
					ConstructorDeclaration.class);
			for (ConstructorDeclaration constructor : constructors) {
				String methodSignature = getSignature(classPair.name, constructor);
				diffResults.put(methodSignature, new DiffResult(constructor.getBody().toString(getPPC()), ""));
			}

			List<MethodDeclaration> methods = getChildNodesByClassType(classPair.clazz, MethodDeclaration.class);
			for (MethodDeclaration method : methods) {
				String methodSignature = getSignature(classPair.name, method);
				if (method.getBody().isPresent()) {
					diffResults.put(methodSignature, new DiffResult(method.getBody().get().toString(getPPC()), ""));
				} else {
					System.out.println("Warning: No Body for " + srcFile + " " + methodSignature);
				}
			}
		}

		// Load all method and constructor from dstFile
		clazzes = md.getClassesFromFile(dstFile);
		for (ClassPair classPair : clazzes) {
			List<ConstructorDeclaration> constructors = getChildNodesByClassType(classPair.clazz,
					ConstructorDeclaration.class);
			for (ConstructorDeclaration constructor : constructors) {
				String methodSignature = getSignature(classPair.name, constructor);
				if (diffResults.containsKey(methodSignature)) {
					DiffResult result = diffResults.get(methodSignature);
					result.dst = constructor.getBody().toString(getPPC());
				} else {
					diffResults.put(methodSignature, new DiffResult("", constructor.getBody().toString(getPPC())));
				}
			}

			List<MethodDeclaration> methods = getChildNodesByClassType(classPair.clazz, MethodDeclaration.class);
			for (MethodDeclaration method : methods) {
				String methodSignature = getSignature(classPair.name, method);
				if (method.getBody().isPresent()) {
					if (diffResults.containsKey(methodSignature)) {
						DiffResult result = diffResults.get(methodSignature);
						result.dst = method.getBody().get().toString(getPPC());
					} else {
						diffResults.put(methodSignature, new DiffResult("", method.getBody().get().toString(getPPC())));
					}
				} else {
					System.out.println("Warning: No Body for " + dstFile + " " + methodSignature);
				}
			}
		}

		List<String> diffMethods = new ArrayList<>();
		for (Entry<String, DiffResult> entry : diffResults.entrySet()) {
			diffMethods.add(String.format("%s:\t%s", entry.getValue().getType(), entry.getKey()));
		}
		Collections.sort(diffMethods, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.charAt(0) - o2.charAt(0);
			}
		});
		return diffMethods;
	}

	/**
	 * Load class and nested class declaration from file.
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	private List<ClassPair> getClassesFromFile(String file) throws FileNotFoundException {
		CompilationUnit cu = StaticJavaParser.parse(new File(file));
		return getClasses(cu, "", false);
	}

	private List<ClassPair> getClasses(Node root, String parents, boolean inMethod) {
		if (root.getChildNodes().size() == 0) {
			return Collections.emptyList();
		}

		List<ClassPair> pairList = new ArrayList<>();
		for (Node child : root.getChildNodes()) {
			if (child instanceof ClassOrInterfaceDeclaration) {
				ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) child;
				String cName = parents + c.getNameAsString();
				if (inMethod) {
					System.out.println(
							"WARNING: Class " + cName + " is located inside a method. We cannot predict its name at"
									+ " compile time so it will not be diffed.");
				} else {
					pairList.add(new ClassPair(c, cName));
					pairList.addAll(getClasses(c, cName + "$", inMethod));
				}
			} else if (child instanceof MethodDeclaration || child instanceof ConstructorDeclaration) {
				pairList.addAll(getClasses(child, parents, true));
			} else {
				pairList.addAll(getClasses(child, parents, inMethod));
			}
		}
		return pairList;
	}

	public static <N extends Node> List<N> getChildNodesByClassType(Node n, Class<N> clazz) {
		if (n.getChildNodes().size() == 0) {
			return Collections.emptyList();
		}

		List<N> nodes = new ArrayList<>();
		for (Node child : n.getChildNodes()) {
			if (child instanceof ClassOrInterfaceDeclaration) {
				// don't go into a nested class
				continue;
			}
			if (clazz.isInstance(child)) {
				nodes.add(clazz.cast(child));
			}
			nodes.addAll(getChildNodesByClassType(child, clazz));
		}
		return nodes;
	}

	public static String getSignature(String className, CallableDeclaration<?> callable) {
		String signature = className + "." + callable.getSignature().asString();
		// Java assist doesn't add spaces for methods with 2+ parameters...
		return signature.replace(" ", "");
	}

	@SuppressWarnings("unused")
	private static void removeComments(Node node) {
		for (Comment child : node.getAllContainedComments()) {
			child.remove();
		}
	}

}