package zhengjin.app.demo;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * Test demo for Jackson.
 */
public class TestJsonDemo {

	private static ObjectMapper mapper;

	@BeforeClass
	public static void classSetup() {
		mapper = new ObjectMapper();
	}

	@Test
	public void testJsonObject() {
		// json object操作
		ObjectNode json = mapper.createObjectNode();
		json.put("name", "Henry");
		json.put("age", 31);
		System.out.println(json);

		ArrayNode jsonNodes = mapper.createArrayNode();
		jsonNodes.add(json);
		System.out.println(jsonNodes);
	}

	@Test
	public void testJsonObjectNull() throws JsonProcessingException {
		// fastjson
		JSONObject obj1 = new JSONObject();
		obj1.put("name", "Vieira");
		obj1.put("age", 35);
		obj1.put("desc", null);
		System.out.println("user info: " + JSONObject.toJSONString(obj1));

		// jackson
		ObjectNode obj2 = mapper.createObjectNode();
		obj2.put("name", "Vieira");
		obj2.put("age", 35);
		// obj2.put("desc", null);
		obj2.put("desc", "");
		System.out.println("user info: " + mapper.writeValueAsString(obj2));
	}

	@Test
	public void testJsonObjectUpdate() throws JsonMappingException, JsonProcessingException {
		String json = "{\"name\":\"Henry\",\"age\":30}";

		// fastjson
		JSONObject user1 = (JSONObject) JSONObject.parse(json);
		user1.put("age", 35);
		System.out.println("update user info: " + JSONObject.toJSONString(user1));

		// jackson
		JsonNode user2 = mapper.readValue(json, JsonNode.class);
		((ObjectNode) user2).put("age", 38);
		System.out.println("update user info: " + mapper.writeValueAsString(user2));
	}

	@Test
	public void testSerialize() throws JsonProcessingException {
		// 序列化操作 将Java对象转化成json
		User user = new User();
		user.setName("Henry");
		user.setAge(30);
		user.setGender(GENDER.MALE);
		user.setBirthday(new Date());

		String s = mapper.writeValueAsString(user);
		System.out.println(s);
	}

	@Test
	public void testDeSerialize() throws JsonProcessingException {
		// 反序列化 将json转化成Java对象
		System.out.println("De serialize by class:");
		String json = "{\"name\":\"Henry\",\"age\":30}";
		User user = mapper.readValue(json, User.class);
		System.out.println(user);

		System.out.println("\nDe serialize by map:");
		Map<?, ?> map = mapper.readValue(json, Map.class);
		System.out.println(String.format("name:%s, age:%s", map.get("name"), map.get("age")));

		System.out.println("\nDe serialize by jackson node:");
		json = "[{\"name\":\"Henry\",\"age\":31}]";
		JsonNode arr = mapper.readValue(json, JsonNode.class);
		if (arr.isArray()) {
			for (int i = 0; i < arr.size(); i++) {
				JsonNode node = arr.get(i);
				System.out
						.println(String.format("name:%s, age:%d", node.get("name").asText(), node.get("age").asInt()));
			}
		}
	}

	@Test
	public void testDeSerializeDate() throws JsonProcessingException {
		String json = "{\"name\":\"Henry\",\"age\":30,\"birthday\":1592800446397}";
		User user = mapper.readValue(json, User.class);
		System.out.println(user);
		System.out.println("user birthday: " + user.getBirthday());

		String json1 = "{\"name\":\"Henry\",\"age\":31,\"birthday\":\"2020-01-01 12:13:14\"}";
		User user1 = mapper.readValue(json1, User.class);
		System.out.println(user1);
		System.out.println("user birthday: " + user1.getBirthday());
	}

	@Test
	public void testDeSerializeCustomDate() throws JsonProcessingException {
		String json = "{\"name\":\"Henry\",\"age\":30,\"custom_birthday\":\"2020-01-01 01:12:23\"}";
		User user = mapper.readValue(json, User.class);
		System.out.println(user);
		System.out.println("user birthday: " + user.getCustomBirthday());
	}

	@Test
	public void testDeSerializeEnum() throws JsonProcessingException {
		String json = "{\"name\":\"Henry\",\"age\":30,\"gender\":1}";
		User user = mapper.readValue(json, User.class);
		System.out.println(user);
		System.out.println("user gender: " + user.getGender());
	}

	@SuppressWarnings("unused")
	private static class User implements Serializable {

		private static final long serialVersionUID = 1L;

		private String name;
		private int age;
		private GENDER gender;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private Date birthday;
		@JsonDeserialize(using = CustomDeserializerDate.class)
		private Date custom_birthday;

		@Override
		public String toString() {
			return String.format("user info: name=%s, age=%d", this.name, this.age);
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public int getAge() {
			return this.age;
		}

		public void setGender(GENDER genger) {
			this.gender = genger;
		}

		public GENDER getGender() {
			return this.gender;
		}

		public void setBirthday(Date birthday) {
			this.birthday = birthday;
		}

		public Date getBirthday() {
			return this.birthday;
		}

		public void setCustomBirthday(Date birthday) {
			this.custom_birthday = birthday;
		}

		public Date getCustomBirthday() {
			return this.custom_birthday;
		}
	}

	@SuppressWarnings("unused")
	private static enum GENDER {
		MALE("男", 1), FEMALE("女", 0);

		private String name;
		private int value;

		GENDER(String name, int value) {
			this.name = name;
			this.value = value;
		}

		@JsonCreator
		public static GENDER getGenderById(int value) {
			for (GENDER c : GENDER.values()) {
				if (c.getValue() == value) {
					return c;
				}
			}
			return null;
		}

		@JsonValue
		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getValue() {
			return this.value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	@SuppressWarnings("unused")
	private static class CustomDeserializerDate extends StdDeserializer<Date> {

		private static final long serialVersionUID = 1L;

		private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public CustomDeserializerDate() {
			this(null);
		}

		protected CustomDeserializerDate(Class<?> vc) {
			super(vc);
		}

		@Override
		public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			String date = p.getText();
			try {
				return sdf.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}
