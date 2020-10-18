package zhengjin.zookeeper.pool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperConnectionPool implements ConnectionPool<ZooKeeper> {

	private final static Logger logger = LoggerFactory.getLogger(ZookeeperConnectionPool.class);

	// 等待zookeeper客户端创建完成的计数器
	private static ThreadLocal<CountDownLatch> latchThreadLocal = ThreadLocal.withInitial(() -> new CountDownLatch(1));

	private Integer maxActive;
	private Long maxWait;

	// 1. LinkedBlockingQueue不同于ArrayBlockingQueue, 它如果不指定容量, 默认为Integer.MAX_VALUE,
	// 也就是无界队列. 所以为了避免队列过大造成机器负载或者内存爆满的情况出现, 我们在使用的时候建议手动传一个队列的大小.
	// 2. 使用了 putLock 和 takeLock 对并发进行控制, 也就是说, 添加和删除操作并不是互斥操作, 可以同时进行,
	// 这样也就可以大大提高吞吐量.
	private LinkedBlockingQueue<ZooKeeper> idle = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<ZooKeeper> busy = new LinkedBlockingQueue<>();

	// 连接池活动连接数
	private AtomicInteger activeSize = new AtomicInteger(0);
	// 连接池关闭标记
	private AtomicBoolean isClosed = new AtomicBoolean(false);

	public ZookeeperConnectionPool(Integer maxActive, Long maxWait) {
		this.init(maxActive, maxWait);
	}

	@Override
	public void init(Integer maxActive, Long maxWait) {
		this.maxActive = maxActive;
		this.maxWait = maxWait;
	}

	@Override
	public ZooKeeper getResource() throws Exception {
		ZooKeeper zooKeeper;
		Long pid = Thread.currentThread().getId();
		final CountDownLatch countDownLatch = latchThreadLocal.get();

		if ((zooKeeper = this.idle.poll()) != null) {
			busy.offer(zooKeeper);
			return zooKeeper;
		}

		if (this.activeSize.get() < this.maxActive) {
			if (this.activeSize.incrementAndGet() <= this.maxActive) {
				// 创建新的zookeeper连接
				zooKeeper = new ZooKeeper("localhost", 5000, (watch) -> {
					if (watch.getState() == Watcher.Event.KeeperState.SyncConnected) {
						countDownLatch.countDown();
					}
				});
				countDownLatch.await();
				busy.offer(zooKeeper);
				return zooKeeper;
			} else {
				this.activeSize.decrementAndGet();
			}
		}

		// 若活动线程已满则等待busy队列释放连接
		logger.info(String.format("Thread %d wait for idle connection.", pid));
		zooKeeper = this.idle.poll(maxWait, TimeUnit.MILLISECONDS);
		if (zooKeeper == null) {
			throw new Exception(String.format("Thread %d get connection timeout.", pid));
		}
		busy.offer(zooKeeper);
		return zooKeeper;
	}

	@Override
	public void release(ZooKeeper connection) throws Exception {
		if (connection == null) {
			logger.warn("Connection to be removed is null.");
			return;
		}

		if (this.busy.remove(connection)) {
			this.idle.offer(connection);
		} else {
			logger.warn("Release connection failed.");
			this.activeSize.decrementAndGet();
		}
	}

	@Override
	public void close() {
		if (this.isClosed.compareAndSet(false, true)) {
			this.idle.forEach((zooKeeper) -> {
				try {
					zooKeeper.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});

			this.busy.forEach((zooKeeper) -> {
				try {
					zooKeeper.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}

}
