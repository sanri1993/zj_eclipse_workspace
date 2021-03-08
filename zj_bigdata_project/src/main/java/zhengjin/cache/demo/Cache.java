package zhengjin.cache.demo;

public interface Cache {

	String get(String key);

	void put(String key, String value);

}
