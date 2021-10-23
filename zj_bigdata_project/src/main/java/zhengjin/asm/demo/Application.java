package zhengjin.asm.demo;

import java.util.concurrent.TimeUnit;

/**
 * Refer: https://blog.csdn.net/wanxiaoderen/article/details/107043218
 * 
 */
public class Application {

	public int a = 0;
	int b = 1;

	public void test01() {
		b = 2;
	}

	void test02() {
	}

	public void test03() {
		System.out.println("hello");
		System.out.println("asm");
	}

	@ASMTest
	public void sayHello() throws InterruptedException {
		TimeUnit.SECONDS.sleep(1);
		System.out.println("Hello ASM inject timer.");
	}

	public static void main(String[] args) throws Exception {

		Application app = new Application();
		app.sayHello();
		System.out.println("application exec done");
	}

}
