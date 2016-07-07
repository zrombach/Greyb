package gov.lab24.auth.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VerifyEnvConfiguration {
		
	private static final Logger logger = LoggerFactory.getLogger(VerifyEnvConfiguration.class);
	
	Map<String, String> environments = new HashMap<String, String>();
	String verifyLogsLocation;
	Map<String, String> endpoints = new HashMap<String, String>();
	
	@JsonIgnore
	Map<String, VerifyContext> environmentContexts = new HashMap<String, VerifyContext>();
	
	@JsonProperty
	public Map<String, String> getEnvironments() {
		return environments;		
	}
	
	public VerifyContext getEnvironment(String envName) {

		if (environmentContexts.containsKey(envName)) {
			return environmentContexts.get(envName);
		} else {
			if (environments.containsKey(envName)) {
				VerifyContext newContext = new VerifyContext();
				
				// Parse JSON format from String
				String envInfo = environments.get(envName);
				ObjectMapper mapper = new ObjectMapper();
				try {
					newContext = mapper.readValue(envInfo, VerifyContext.class);
				} catch (IOException e) {
					logger.error("Unable to parse information for verifying environment: " + envInfo, e);
					return null;
				}				
				environmentContexts.put(envName, newContext);
				return newContext;				
			} 
		}
		
		return null;
	}
	
	@JsonProperty
	public String getVerifyLogsLocation() {
		return verifyLogsLocation;
	}
	
	@JsonProperty
	public Map<String, String> getEndpoints() {
		return endpoints;
	}
	
}
