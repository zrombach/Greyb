package gov.lab24.auth.verify;

import static org.mockito.Mockito.mock;
import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


import com.google.common.collect.ImmutableMultimap;


/**
 * Rapid development test class for use in building VerifyServiceResults effort 
 * 
 * Assumes that the service is running, using baseURL information provided below.  Typical usage model:
 *   - Compile and run from the command line, so the port, etc, are live.
 *   - Execute this test case from within IDE to help with break points
 *
 */
public class VerifyServiceResultsTaskTest {

	private final PrintWriter writer = mock(PrintWriter.class);
	private Task verifyTask;
	private VerifyEnvConfiguration env;
	
	@Before
	public void setup() {		
		env = new VerifyEnvConfiguration();

		String envValue = "{ \"baseURL\": \"https://localhost:8443\", \"keyStorePath\": \"./testcerts/localhost.jks\", " +
				"\"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"isPasswordEncrypted\": \"true\", \"username\": [\"foo\", \"server\"] }";
		env.environments.put("self", envValue);
		
		String envEdeValue = "{ \"baseURL\": \"https://localhost:8443\", \"keyStorePath\": \"./testcerts/localhost.jks\", " +
				"\"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"isPasswordEncrypted\": \"true\", \"username\": [\"zoobar\", \"fooServer\"]}";
		env.environments.put("ede", envEdeValue);
		
		String envMismatched = "{ \"baseURL\": \"https://localhost:8443\", \"keyStorePath\": \"./testcerts/localhost.jks\", " +
				"\"keyStorePassword\": \"OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v\", \"isPasswordEncrypted\": \"true\", \"username\": [\"zoobar\"]}";
		env.environments.put("mismatched", envMismatched);
		
		env.endpoints.put("/foo", null);
		env.endpoints.put("/users/[username]/info", "info-short");
		env.endpoints.put("/users/[username]", "info-all");
		
		env.verifyLogsLocation = "./verifyLogs/";
		verifyTask = new VerifyServiceResults(env);
	}
	
	@Test
	public void testAccessViaHTTPsDefault() throws Exception {
				
		verifyTask.execute(ImmutableMultimap.<String, String>of("env1", "self", "env2", "self"), writer);
		
	}
	
	@Test
	public void testAccessViaHTTPsAllTests() throws Exception {
				
		verifyTask.execute(ImmutableMultimap.<String, String>of("env1", "self", "env2", "self", "test", "all"), writer);
		
	}

	@Test
	public void testAccessViaHTTPsSingleTest() throws Exception {
				
		verifyTask.execute(ImmutableMultimap.<String, String>of("env1", "self", "env2", "ede", "test", "info-all"), writer);
		
	}

	@Test
	public void testMismatchedEndpointSubstitutions() throws Exception {
		
		try {
			verifyTask.execute(ImmutableMultimap.<String, String>of("env1", "self", "env2", "mismatched", "test", "info-all"), writer);
			fail("Task should have failed - should not be able to calculate matching endpoints");
		} catch (RuntimeException e) {
			
		}
	}
}

