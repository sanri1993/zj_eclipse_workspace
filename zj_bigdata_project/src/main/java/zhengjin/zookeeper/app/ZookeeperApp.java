package zhengjin.zookeeper.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperApp {

	private final static String TAG = ZookeeperLock.class.getSimpleName() + " => ";
	private final static Logger logger = LoggerFactory.getLogger(ZookeeperApp.class);

	private static List<String> msgCache = new ArrayList<>(20);

	public static void main(String[] args) {

		// zk env => cmd: docker-compose -f zookeeper-compose.yaml up
		final String zkIP = "127.0.0.1";
		final int zkPort = 2181;
		final int poolSize = 3;

		// setup
		ZookeeperLock lock = ZookeeperLock.getInstance().build(zkIP, zkPort);
		try {
			lock.createRootLockPath();
		} catch (KeeperException | InterruptedException | IOException e) {
			e.printStackTrace();
			logger.error(TAG + "create root lock path failed!");
			return;
		}

		for (int i = 0; i < 10; i++) {
			msgCache.add("msg" + i);
		}

		// consume message in msg cache by using distributed zk lock
		List<Thread> pool = new ArrayList<>(10);
		for (int i = 0; i < poolSize; i++) {
			pool.add(new Thread(new MsgConsumer()));
		}

		for (Thread t : pool) {
			t.start();
		}
		for (Thread t : pool) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}

		try {
			lock.getZkClient().close();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			logger.warn(TAG + "close zk connection failed!");
		}
		logger.info(TAG + "zookeeper lock demo done.");
	}

	private static class MsgConsumer implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (msgCache.size() == 0) {
					logger.info(TAG + "message cached is empty, and return.");
					return;
				}

				String lock;
				try {
					lock = ZookeeperLock.getInstance().getLock();
				} catch (KeeperException | InterruptedException | IOException e) {
					e.printStackTrace();
					logger.error(TAG + "get lock failed!");
					return;
				}

				if (msgCache.size() > 0) {
					String msg = msgCache.get(0);
					logger.info(TAG + Thread.currentThread().getName() + " consume msg: " + msg);
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					msgCache.remove(msg);
				}

				try {
					ZookeeperLock.getInstance().releaseLock(lock);
				} catch (InterruptedException | KeeperException | IOException e) {
					e.printStackTrace();
					logger.error(TAG + "release lock failed!");
					return;
				}
			}
		}
	}

}
