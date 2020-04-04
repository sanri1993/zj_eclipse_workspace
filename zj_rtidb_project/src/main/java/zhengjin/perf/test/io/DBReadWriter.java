package zhengjin.perf.test.io;

import java.util.Map;

public interface DBReadWriter {

	boolean put(String tbName, Map<String, Object> row) throws Exception;

	Object[] get(String tbName, String key) throws Exception;

}
