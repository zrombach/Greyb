package gov.lab24.auth;

import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.User;
import gov.lab24.auth.db.DataSourceFactory;
import gov.lab24.auth.db.GroupsCollection;
import gov.lab24.auth.db.UsersCollection;
import gov.lab24.auth.verify.VerifyEnvConfiguration;
import io.dropwizard.setup.Environment;

import org.mongojack.JacksonDBCollection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mongodb.MongoException;

public class AuthServiceModule extends AbstractModule {

	private JacksonDBCollection<User, String> userCollection = null;
	private JacksonDBCollection<Project, String> groupsCollection;

	@Override
	protected void configure() {
//		bind(GroupsResource.class).toProvider(GroupsResourceProvider.class);
//		bind(ProjectsResource.class).toProvider(ProjectResourceProvider.class);
	}

	@SuppressWarnings("unchecked")
	@Provides
	@UsersCollection
	private JacksonDBCollection<User, String> providesUserCollection(
			AuthServiceConfiguration configuration, Environment environment) {

		if (userCollection == null) {
			try {
				configuration.getDataSourceFactory().build(environment);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				userCollection = (JacksonDBCollection<User, String>) configuration
						.getDataSourceFactory().retrieveDataSet(
								DataSourceFactory.DATASETS.USERS);
			} catch (MongoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return userCollection;
	}

	@SuppressWarnings("unchecked")
	@Provides
	@GroupsCollection
	private JacksonDBCollection<Project, String> provideProjectsCollection(
			AuthServiceConfiguration configuration, Environment environment) {

		if (groupsCollection == null) {
			try {
				configuration.getDataSourceFactory().build(environment);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				groupsCollection = (JacksonDBCollection<Project, String>) configuration
						.getDataSourceFactory().retrieveDataSet(
								DataSourceFactory.DATASETS.PROJECTS);
			} catch (MongoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return groupsCollection;
	}
	
	@Provides
	private VerifyEnvConfiguration providesVerifyFactory (AuthServiceConfiguration configuration) {		
		return configuration.getVerifyConfig();
	}
	
	
}
