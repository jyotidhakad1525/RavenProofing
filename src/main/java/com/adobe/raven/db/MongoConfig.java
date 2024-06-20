package com.adobe.raven.db;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Configuration
public class MongoConfig {

	public MongoClient mongoClient() {

		// local
	//	String user = encode("ravenUser");
		String user = encode("ravenApiUser");// encode(psdkPassword.getUserName());
		String password = encode("RaveP@@sw*rd");// encode(psdkPassword.getContent());
	//	return MongoClients.create("mongodb://" + user + ":" + password + "@10.42.68.21:27017");

	//	return MongoClients.create("mongodb://" + user + ":" + password + "@10.42.77.14:27017");
	 	return MongoClients.create("mongodb://" + user + ":" + password + "@10.42.77.218:27017");
		//return MongoClients.create("mongodb://" + "XXX" + ":" + "YYYY" + "@localhost:27017");
	}

	public @Bean MongoTemplate mongoTemplate() {

		return new MongoTemplate(mongoClient(), "raven-mongo-db");
		//return new MongoTemplate(mongoClient(), "local");
	}

	public static String encode(String url) {
		try {
			String encodeURL = URLEncoder.encode(url, "UTF-8");
			return encodeURL;
		} catch (UnsupportedEncodingException e) {
			return "Issue while encoding" + e.getMessage();
		}
	}

}

//import com.mongodb.MongoClientURI;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import org.springframework.data.mongodb.MongoDbFactory;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//
//@Configuration
//public class MongoConfig {
//
//	@Autowired
//	private Environment env;
//
//	@Bean
//	public MongoDbFactory mongoDbFactory() {
//
//		String user = encode("ravenUser");// encode(psdkPassword.getUserName());
//		String password = encode("RaveP@@sw*rd");// encode(psdkPassword.getContent());
//		return new SimpleMongoDbFactory(new MongoClientURI("mongodb://" + user + ":" + password + "@localhost:27017/raven-mongo-db"));
//		//return new SimpleMongoDbFactory(new MongoClientURI(env.getProperty("spring.data.mongodb.uri")));
//	}
//
//	@Bean
//	public MongoTemplate mongoTemplate() {
//		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
//
//		return mongoTemplate;
//
//	}
//
//
//	public static String encode(String url) {
//		try {
//			String encodeURL = URLEncoder.encode(url, "UTF-8");
//			return encodeURL;
//		} catch (UnsupportedEncodingException e) {
//			return "Issue while encoding" + e.getMessage();
//		}
//	}
//}





//@Configuration
//public class MongoConfig {
//
////	public static PSDKPasswordRequest psdkPasswordRequest;
////	public static PSDKPassword psdkPassword;
////
////	public  void initCyberArk() {
////
////		try {
////
////			psdkPasswordRequest = OrigamiCommonFunctions.setupCyberArk(OrigamiConstants.CyberArkObject_MongoDb);
////			psdkPassword = PasswordSDK.getPassword (psdkPasswordRequest);
////
////		} catch (PSDKException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////			System.out.println("error ::::::::::" + e.getMessage());
////		}
////	}
//
//	@Bean
//	public MongoClient mongoClient() {
//
////		initCyberArk();
////    	if(psdkPassword != null) {
//
//		String user = encode("ravenUser");// encode(psdkPassword.getUserName());
//		String password = encode("RaveP@@sw*rd");// encode(psdkPassword.getContent());//
//		String url = "mongodb://" + user + ":" + password + "@localhost:27017/?authSource=admin";
//		System.out.println(" <>>>>>>>>>>>>>" + url);
//		MongoClientURI uri = new MongoClientURI(url);
//
//		MongoClient mongoClient = MongoClients.create();
//		mongoClient.getDatabase(url);
// //		 MongoClient mongoClient = new MongoClient(uri);
//
//		return mongoClient;
////    	}
////
////    	return null;
//	}
//
//	@Bean
//	public MongoTemplate mongoTemplate() {
//
//		MongoClient mongoClient = mongoClient();
//
//		if (mongoClient != null) {
//			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "raven-mongo-db");
//			return mongoTemplate;
//		}
//
//		return null;
//	}
//
//	public static String encode(String url) {
//		try {
//			String encodeURL = URLEncoder.encode(url, "UTF-8");
//			return encodeURL;
//		} catch (UnsupportedEncodingException e) {
//			return "Issue while encoding" + e.getMessage();
//		}
//	}
//
//}
