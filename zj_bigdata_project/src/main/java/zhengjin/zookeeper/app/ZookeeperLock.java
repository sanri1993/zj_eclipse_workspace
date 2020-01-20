package zhengjin.zookeeper.app;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZookeeperLock {

	private final static String TAG = ZookeeperLock.class.getSimpleName() + " => ";
	private final static Logger logger = Logger.getLogger(ZookeeperLock.class);
	private final static ZookeeperLock lock = new ZookeeperLock();

	private final String ROOT_LOCK_PATH = "/ZkLocks";
	private final String PRE_LOCK_NAME = "zklock_";

	private String zkIP;
	private int zkPort;
	private ZooKeeper zkClient;

	public static ZookeeperLock getInstance() {
		return lock;
	}

	public ZookeeperLock build(String zkIP, int zkPort) {
		this.zkIP = zkIP;
		this.zkPort = zkPort;
		return this;
	}

	public ZooKeeper getZkClient() throws IOException {
		if (this.zkClient == null) {
			this.zkClient = new ZooKeeper(this.zkIP, this.zkPort, null);
		}
		return this.zkClient;
	}

	public void createRootLockPath() throws KeeperException, InterruptedException, IOException {
		Stat stat = this.getZkClient().exists(ROOT_LOCK_PATH, false);
		if (stat == null) {
			logger.info(TAG + "create zk root lock path.");
			this.getZkClient().create(ROOT_LOCK_PATH, "zk_lock_test".getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		}
	}

	public String getLock() throws KeeperException, InterruptedException, IOException {
		String lockPrefix = ROOT_LOCK_PATH + '/' + PRE_LOCK_NAME;
		String retLockPath = getZkClient().create(lockPrefix, this.GetThreadName().getBytes(), Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		logger.info(TAG + this.GetThreadName() + " create lock path: " + retLockPath);
		tryLock(retLockPath);

		return retLockPath;
	}

	private void tryLock(String lockPath) throws KeeperException, InterruptedException, IOException {
		List<String> locks = getZkClient().getChildren(ROOT_LOCK_PATH, false);
		Collections.sort(locks);
		int curIdx = locks.indexOf(lockPath.substring(ROOT_LOCK_PATH.length() + 1));
		if (curIdx == 0) { // if zk node with min index, then get lock
			logger.info(TAG + this.GetThreadName() + " get lock, lock path: " + lockPath);
			return;
		}

		String preLockPath = locks.get(curIdx - 1); // set delete event watcher to pre node
		Stat retStat = getZkClient().exists(ROOT_LOCK_PATH + "/" + preLockPath, new Watcher() {

			@Override
			public void process(WatchedEvent event) { // watcher callback
				if (!event.getType().equals(EventType.NodeDeleted)) {
					return;
				}

				logger.info(String.format(TAG + GetThreadName() + " node delete event, path %s", event.getPath()));
				synchronized (ZookeeperLock.this) {
					ZookeeperLock.this.notifyAll(); // cannot use notify()
					logger.info(TAG + GetThreadName() + " send notify.");
				}
			}
		});

		if (retStat == null) {
			tryLock(lockPath);
		} else {
			logger.info(TAG + this.GetThreadName() + " wait for " + preLockPath);
			synchronized (this) {
				this.wait();
				logger.info(TAG + this.GetThreadName() + " get notify and resume.");
			}
			tryLock(lockPath);
		}
	}

	public void releaseLock(String lockPath) throws InterruptedException, KeeperException, IOException {
		getZkClient().delete(lockPath, -1);
		logger.info(TAG + this.GetThreadName() + " release lock, path: " + lockPath);
	}

	private String GetThreadName() {
		return Thread.currentThread().getName(); // dynamic value
	}

}
