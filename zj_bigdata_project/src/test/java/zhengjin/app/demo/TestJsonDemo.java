package zhengjin.app.demo;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
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

	@Test
	public void testJsonObject() {
		// json object操作
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode json = mapper.createObjectNode();
		json.put("name", "Henry");
		json.put("age", 31);
		System.out.println(json);

		ArrayNode jsonNodes = mapper.createArrayNode();
		jsonNodes.add(json);
		System.out.println(jsonNodes);
	}

	@Test
	public void testSerialize() throws JsonProcessingException {
		// 序列化操作 将Java对象转化成json
		User user = new User();
		user.setName("Henry");
		user.setAge(30);
		user.setGender(GENDER.MALE);
		user.setBirthday(new Date());

		ObjectMapper mapper = new ObjectMapper();
		String s = mapper.writeValueAsString(user);
		System.out.println(s);
	}

	@Test
	public void testDeSerialize() throws JsonProcessingException {
		// 反序列化 将json转化成Java对象
		String json = "{\"name\":\"Henry\",\"age\":30}";
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(json, User.class);
		System.out.println(user);
	}

	@Test
	public void testDeSerializeDate() throws JsonProcessingException {
		String json = "{\"name\":\"Henry\",\"age\":30,\"birthday\":1592800446397}";
		ObjectMapper mapper = new ObjectMapper();
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
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(json, User.class);
		System.out.println(user);
		System.out.println("user birthday: " + user.getCustomBirthday());
	}

	@Test
	public void testDeSerializeEnum() throws JsonProcessingException {
		String json = "{\"name\":\"Henry\",\"age\":30,\"gender\":1}";
		ObjectMapper mapper = new ObjectMapper();
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
