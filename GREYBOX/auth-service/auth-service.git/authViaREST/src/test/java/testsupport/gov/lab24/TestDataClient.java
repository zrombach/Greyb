package testsupport.gov.lab24;

import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.User;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.*;

import org.mongojack.JacksonDBCollection;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import static org.junit.Assert.fail;

public class TestDataClient {

		private static TestDataClient instance = null;
		private MongoClient mongoClient;
		private DB db;
		
		// environment variables used for testing
		private static String MONGO_HOST = "MONGODB_DB_HOST";
		private static String MONGO_PORT = "MONGODB_DB_PORT";
		
		Logger log = Logger.getLogger(TestDataClient.class.toString());
		
		protected TestDataClient() {
			
			// check to see if we've specified a MONGO_HOST and/or MONGO_PORT for use
			Map<String, String> ENV = System.getenv();
			int mongoPort = 27017;		// default;
			String mongoHost = "localhost";
			
			String dataValue = ENV.get(MONGO_HOST);
			if (dataValue != null) {
				mongoHost = dataValue;
			}
			log.info("Connecting on host " + mongoHost);
			
			dataValue = ENV.get(MONGO_PORT);
			if (dataValue != null) {
				try {
					mongoPort = Integer.valueOf(dataValue);
				} catch (NumberFormatException ne) {
					throw new RuntimeException("Unable to connect to database on non-numeric port " + dataValue);
				}
			}
			log.info("Connecting on port " + mongoPort);
			
			System.out.println("Connecting on host: [" + mongoHost + "] and port: [" + mongoPort);
			
			try {
				mongoClient = new MongoClient( mongoHost, mongoPort );
			} catch (UnknownHostException e) {
				throw new RuntimeException("Unable to build test data client: unknown host exception ", e);
			}			
			db = mongoClient.getDB("test-test");
		}
	
		public static TestDataClient getInstance() {
			
			if (instance == null) {
				instance = new TestDataClient();
			}
			return instance;		
		}
		
		private void addToCollection(String collectionName, String json) {
						
			DBCollection collection = db.getCollection(collectionName);
			DBObject dbObject = (DBObject) JSON.parse(json);
			collection.insert(dbObject);
		}
		
		public void clearUserData() {
			DBCollection collection = db.getCollectionFromString("users");
			if (collection != null) {
				collection.drop();
			}
			
		}
		
		public void clearProjectData() {
			DBCollection collection = db.getCollectionFromString("projects");
			if (collection != null) {
				collection.drop();
			}
		}
		public void addUserData(String json) {
			addToCollection("users", json);
		}
		
		public void addProjectData(String json) {
			addToCollection("projects", json);
		}
		
		public JacksonDBCollection<gov.lab24.auth.core.User, String> getUserCollection() {
			return JacksonDBCollection.wrap(db.getCollection("users"),  User.class, String.class);
		}
		
		public JacksonDBCollection<gov.lab24.auth.core.Project, String> getProjectCollection() {
			return JacksonDBCollection.wrap(db.getCollection("projects"), Project.class, String.class);
		}
			
}
