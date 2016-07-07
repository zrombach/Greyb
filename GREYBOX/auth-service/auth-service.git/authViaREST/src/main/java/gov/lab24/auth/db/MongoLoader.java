package gov.lab24.auth.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import gov.lab24.auth.AuthServiceConfiguration;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.User;
import io.dropwizard.setup.Environment;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MongoLoader {
    private AuthServiceConfiguration config;
    private Environment env;
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private static final String DB_NAME = "mongo-user";
    private static final String USERS_COLLECTION = "users";
    private static final String PROJECTS_COLLECTION = "projects";
    private static final String GROUPS_COLLECTION = "groups";
    private static final Logger LOG = LoggerFactory.getLogger(MongoLoader.class);

    public MongoLoader(AuthServiceConfiguration configuration, Environment environment) {
        this.config = configuration;
        this.env = environment;
    }

    public void bootstrap() throws Exception {
        DataSourceFactory factory = config.getDataSourceFactory();
        MongoClient client = factory.build(env);

        DB mongoDB = client.getDB(DB_NAME);
        loadUsers(mongoDB);
        loadProjects(mongoDB);
    }

    private void loadUsers(DB db) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream("bootstrap/users.json");

        ObjectMapper mapper = new ObjectMapper();
        List<User> userList = mapper.readValue(inputStream, new TypeReference<List<User>>(){});

        JacksonDBCollection<User, String> users = JacksonDBCollection.wrap(db.getCollection(USERS_COLLECTION), User.class, String.class);

        for (User user : userList) {
            DBCursor<User> userDBCursor = users.find().is("dn", user.getDn());
            if (userDBCursor.hasNext()) {
                LOG.info("User < " + user.getDn() + " > is already in the auth database");
            } else {
                LOG.info("Inserting User < " + user.getDn() + " > into the auth database");
                WriteResult<User, String> result = users.insert(user);
                if (result.getError() != null) {
                    LOG.error("Error inserting user into auth database");
                    LOG.error(result.getError());
                }
            }
        }
    }

    private void loadProjects(DB db) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream("bootstrap/projects.json");

        ObjectMapper mapper = new ObjectMapper();
        List<Project> projectList = mapper.readValue(inputStream, new TypeReference<List<Project>>(){});

        JacksonDBCollection<Project, String> projects = JacksonDBCollection.wrap(db.getCollection(PROJECTS_COLLECTION), Project.class, String.class);

        for (Project project : projectList) {
            DBCursor<Project> projectDBCursor = projects.find().is("name", project.name);

            LOG.warn(project.name);

            if (projectDBCursor.hasNext()) {
                LOG.info("Project < " + project.name + " > is already in the auth database");
            } else {
                LOG.info("Inserting Project < " + project.name + " > into the auth database");
                WriteResult<Project, String> result = projects.insert(project);
                if (result.getError() != null) {
                    LOG.error("Error inserting project into auth database");
                    LOG.error(result.getError());
                }
            }
        }
    }
}
