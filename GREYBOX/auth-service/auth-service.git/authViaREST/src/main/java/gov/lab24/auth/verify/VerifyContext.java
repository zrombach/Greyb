package gov.lab24.auth.verify;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.security.Password;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;


public class VerifyContext {

	@JsonProperty
	@NotEmpty
	String keyStorePath;
	
	@JsonProperty
	@NotEmpty
	String keyStorePassword;
	
	@JsonProperty("usernames")
	String[] usernames = {};
	
	@JsonProperty("projectnames")
	String[] projects = {};  

	@JsonProperty("groupnames")
	String[] groups = {};

	@JsonProperty
	@NotEmpty
	String baseURL;
	
	@JsonProperty
	boolean isPasswordEncrypted = false;
	
	String getDeobfuscatedPassword() {
		if (isPasswordEncrypted) {
			return Password.deobfuscate(keyStorePassword);
		} else
			return keyStorePassword;
	}

	@Override
	public String toString() {
		return "VerifyContext [" 
				//+ "user1Name=" + user1Name + ", serverUserName=" + serverUserName + ", baseURL=" + baseURL
				+ "]";
	}
	
	public List<String> endpointSubstitution( String endpoint ) {
		
		int userMultiplier = 1, groupMultiplier = 1, projectMultiplier = 1;

		if (endpoint.contains("[username]")) userMultiplier = usernames.length;
		if (endpoint.contains("[groupname]")) groupMultiplier = groups.length;
		if (endpoint.contains("[projectname]")) projectMultiplier = projects.length;
				
		List<String> endpoints = new ArrayList<String>();
		
		// for loop grants the potential number of endpoints.. (multiple users * groups * projects ] - we may end up with duplicates, depending on the number of substitution tags.....  ugh...
		for (int userIter = 0; userIter < userMultiplier; userIter++) {
			String populatedEndpoint = baseURL + endpoint;
			for (int groupIter = 0; groupIter < groupMultiplier; groupIter++) {
				for (int projectIter = 0; projectIter < projectMultiplier; projectIter++) {
					
					// handle case where arrays aren't populated, since we may not need all tags for our endpoints list
					if (usernames.length > 0) populatedEndpoint = populatedEndpoint.replaceAll("\\[username\\]", usernames[userIter]);
					if (groups.length > 0) populatedEndpoint = populatedEndpoint.replaceAll("\\[groupname\\]", groups[groupIter]);
					if (projects.length > 0) populatedEndpoint = populatedEndpoint.replaceAll("\\[projectname\\]", projects[projectIter]);
					
					endpoints.add(populatedEndpoint);
				}
			}
		}	
		return endpoints;
	
	}
}
