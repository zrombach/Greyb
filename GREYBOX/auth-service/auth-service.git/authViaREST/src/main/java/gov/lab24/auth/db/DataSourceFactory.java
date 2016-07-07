package gov.lab24.auth.db;

import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.User;
import io.dropwizard.setup.Environment;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class DataSourceFactory {
	// TODO: Can we pass in an enum of datasets, such that we can reuse this class in other contexts?
	public enum DATASETS {
		USERS, PROJECTS;
	}
	
	@Pattern(regexp="mongo|mysql")	// testing pattern matching - only supporting mongo today out of this factory	
	private String type = "mongo";
	
	@NotEmpty
	private String host;
	
	@Min(1)
	@Max(65535) 
	private int port = 27017;	
	
	@NotEmpty
	private String db;

	private MongoClient mongoClient;

	@JsonProperty("type")
	public String getDbType() {
		return type;
	}

	
	@JsonProperty("type")
	public void setDbType(String dbType) {
		if (!dbType.equals("mongo")) {
			
		}
		this.type = dbType;
	}
	
	@JsonProperty
	public String getHost() {
		return host;
	}

	@JsonProperty
	public void setHost(String host) {
		this.host = host;
	}

	@JsonProperty
	public int getPort() {
		return port;
	}

	@JsonProperty
	public void setPort(int port) {
		this.port = port;
	}

	@JsonProperty
	public String getDb() {
		return db;
	}

	@JsonProperty
	public void setDb(String db) {
		this.db = db;
	}
	
	public MongoClient build(Environment environment) throws Exception {
		
		if (this.mongoClient == null) {
			final MongoClient mongo = new MongoClient(getHost(), getPort());
		
/*		environment.lifecycle().manage(new Managed() {

			@Override
			public void start() throws Exception {
				// we're assuming it already exists and the service is running
				
			}

			@Override
			public void stop() throws Exception {			
				// close any connection opened
				mongo.close();
				
			}});
*/		
			this.mongoClient = mongo;
		}
		return mongoClient;
		
	}
	
	public JacksonDBCollection<?, ?> retrieveDataSet(DATASETS dataSet) {
		
		DB db = mongoClient.getDB(this.getDb());
		JacksonDBCollection<?, ?> collection;
		
		switch (dataSet) {
		case USERS:
			collection = JacksonDBCollection.wrap(db.getCollection("users"),  User.class, String.class);
			collection.createIndex(new BasicDBObject("dn", 1), new BasicDBObject("unique", true));
			break;
		case PROJECTS:
			collection = JacksonDBCollection.wrap(db.getCollection("projects"), Project.class, String.class);
			break;
		default:
			collection = null;
		}
		return collection;
	}
	

}
