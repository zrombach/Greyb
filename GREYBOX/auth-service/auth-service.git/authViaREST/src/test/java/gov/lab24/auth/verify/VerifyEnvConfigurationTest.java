package gov.lab24.auth.verify;

import static gov.lab24.auth.verify.VerifyConfigAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class VerifyEnvConfigurationTest {

	VerifyEnvConfiguration configuration;
	
	@Before
	public void setup() {
		configuration = new VerifyEnvConfiguration();
		configuration.environments = new HashMap<>();
		
	}
	@Test 
	public void testValueNotFound() {		
		VerifyEnvConfiguration configuration = new VerifyEnvConfiguration();
		assertThat(configuration.getEnvironment("foo")).isNull();
	}
	
	@Test
	public void testParseEnvironmentInfoToContextMissingField() {
		// missing baseURL
		String environmentInfo ="{ \"keyStorePath\": \"./testcerts/localhost.jks\", \"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"usernames\": [\"foo\"] }"; 
		configuration.environments.put("self", environmentInfo);		
		VerifyContext environment = configuration.getEnvironment("self");
		assertThat(environment).isNotNull().isNot(VerifyConfigAssert.populated);
		
		// missing keyStorePath
		environmentInfo ="{ \"baseURL\": \"./testcerts/localhost.jks\", \"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"usernames\": [\"foo\"]}"; 
		configuration.environments.put("self", environmentInfo);		
		environment = configuration.getEnvironment("self");
		assertThat(environment).isNotNull().isNot(VerifyConfigAssert.populated);

		// missing password
		environmentInfo ="{ \"baseURL\": \"./testcerts/localhost.jks\", \"keyStorePath\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"usernames\": [\"foo\"] }"; 
		configuration.environments.put("self", environmentInfo);		
		environment = configuration.getEnvironment("self");
		assertThat(environment).isNotNull().isNot(VerifyConfigAssert.populated);		
	}
	
	
	@Test
	public void testValidInfoRetrieved() {

		String environmentInfo = "{ \"baseURL\": \"https://localhost:8443\",\"keyStorePath\": \"./testcerts/localhost.jks\", \"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"usernames\": [\"foo\"] }";
		configuration.environments.put("self", environmentInfo);		
		VerifyContext environment = configuration.getEnvironment("self");
		assertThat(environment).isNotNull().is(VerifyConfigAssert.populated);

	}
	
	@Test 
	public void testEndpointSubstitutionUsername() {
		
		String environmentInfo = "{ \"baseURL\": \"https://localhost:8443\",\"keyStorePath\": \"./testcerts/localhost.jks\", \"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"usernames\": [\"foo\", \"server\"] }";
		
		configuration.environments.put("self", environmentInfo);		
		VerifyContext environment = configuration.getEnvironment("self");
		
		// single substitution, multiple users
		List<String> endpointSubstitution = environment.endpointSubstitution("/users/[username]/foo");
		assertThat(endpointSubstitution).containsOnly("https://localhost:8443/users/foo/foo", "https://localhost:8443/users/server/foo");
		
		// no substitution - but making sure it's not just picking up "username"
		endpointSubstitution = environment.endpointSubstitution("/users/username/foo");
		assertThat(endpointSubstitution).containsOnly("https://localhost:8443/users/username/foo");
		
		// multiple replacements substitution
		endpointSubstitution = environment.endpointSubstitution("/users/[username]/foo/[username]");
		assertThat(endpointSubstitution).containsOnly("https://localhost:8443/users/foo/foo/foo", "https://localhost:8443/users/server/foo/server");
				
	}
	
	@Test
	public void testEndpointSubstitutionGroupsAndMultipleTags() {

		String environmentInfo = "{ \"baseURL\": \"https://localhost:8443\",\"keyStorePath\": \"./testcerts/localhost.jks\", \"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"usernames\": [\"foo\", \"server\"], \"groupnames\": [\"groupFoo\"] }";

		configuration.environments.put("self", environmentInfo);		
		VerifyContext environment = configuration.getEnvironment("self");
		
		// single substitution, multiple users
		List<String> endpointSubstitution = environment.endpointSubstitution("/users/[username]/foo/[groupname]");
		assertThat(endpointSubstitution).containsOnly("https://localhost:8443/users/foo/foo/groupFoo", "https://localhost:8443/users/server/foo/groupFoo");
		assertThat(endpointSubstitution.size()).isEqualTo(2);
		
		// no substitution - but making sure it's not just picking up "groupname"
		endpointSubstitution = environment.endpointSubstitution("/users/groupname/foo");
		assertThat(endpointSubstitution).containsOnly("https://localhost:8443/users/groupname/foo");
		assertThat(endpointSubstitution.size()).isEqualTo(1);
		
		// multiple replacements substitution
		endpointSubstitution = environment.endpointSubstitution("/users/[groupname]/foo/[groupname]");
		assertThat(endpointSubstitution).containsOnly("https://localhost:8443/users/groupFoo/foo/groupFoo");

	}
	
}
