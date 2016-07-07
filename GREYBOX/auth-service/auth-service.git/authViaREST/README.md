### Development Dependencies
To build/develop the authorization service you need a JDK (version 8+) and maven installed. To run the tests you also need a mongo instance.

### Build
By default the tests will try to connect mongo on `localhost:27017`. You can also override host/port with environment varialbes. For example: `MONGODB_DB_HOST=localhost MONGODB_DB_PORT=27017 mvn package`.

### Run
`java -jar target/authViaREST-0.0.1-SNAPSHOT.jar server src/main/resources/authServiceConfig.yml`

When developing, use -Dspring.profiles.active=test to support use of the usernames + passwords listed in sec:user-service in applicationContext-security.

## Architecture
Makes use of [DropWizard.io](http://dropwizard.io/), which we've then integrated with [Spring Security](http://projects.spring.io/spring-security/) to allow us to handle the PKI certificates.

Built using Java 8.
