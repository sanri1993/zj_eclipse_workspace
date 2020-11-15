package zhengjin.app.demo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * 1. 使用 ReentrantLock 和 Condition 完成线程间同步；2. 测试 await 线程被中断。
 * 
 */
public final class ConditionSyncMain {

	// 1. If Runner get lock first, then wait for trigger to continue;
	// 2. If trigger get lock first, then runner will be exit because of timeout;
	// 3. When Runner await for signal, do Interrupted.
	public static void main(String[] args) throws InterruptedException {

		final ReentrantLock lock = new ReentrantLock(false);
		final Condition isRun = lock.newCondition();

		int timeOut = 5;
		int triggerTime = 3;
		new Thread(new Runner(lock, isRun, timeOut)).start();
		new Thread(new Trigger(lock, isRun, triggerTime)).start();
	}

	private static class Runner implements Runnable {

		private ReentrantLock lock;
		private Condition isRun;
		private int timeOut;

		public Runner(ReentrantLock lock, Condition isRun, int timeOut) {
			this.lock = lock;
			this.isRun = isRun;
			this.timeOut = timeOut;
		}

		@Override
		public void run() {
			try {
				this.lock.lockInterruptibly();
				System.out.println("Runner get lock, and wait for trigger.");
				long nanos = TimeUnit.SECONDS.toNanos(this.timeOut);
				nanos = this.isRun.awaitNanos(nanos);
				if (nanos <= 0) {
					System.out.println("Timeout, and runner exit.");
					return;
				}
				System.out.println("Runner get notify, is processing...");
				TimeUnit.SECONDS.sleep(1);
				System.out.println("Runner done.");
			} catch (InterruptedException e) {
				System.out.println("Runner is Interrupted.");
				e.printStackTrace();
			} finally {
				this.lock.unlock();
			}
		}
	}

	private static class Trigger implements Runnable {
		private ReentrantLock lock;
		private Condition isRun;
		private int time;

		public Trigger(ReentrantLock lock, Condition isRun, int time) {
			this.lock = lock;
			this.isRun = isRun;
			this.time = time;
		}

		@Override
		public void run() {
			try {
				this.lock.lockInterruptibly();
				System.out.println("Trigger get lock, and is processing...");
				TimeUnit.SECONDS.sleep(time);
				System.out.println("Trigger send notify and exit.");
				this.isRun.signal();
			} catch (InterruptedException e) {
				System.out.println("Trigger is Interrupted.");
				e.printStackTrace();
			} finally {
				this.lock.unlock();
			}
		}
	}

}
