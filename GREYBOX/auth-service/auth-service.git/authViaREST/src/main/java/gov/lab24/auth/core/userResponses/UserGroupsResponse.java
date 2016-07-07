package gov.lab24.auth.core.userResponses;

import java.util.HashSet;
import java.util.Set;

public class UserGroupsResponse {

	public Set<String> groupDns;
	public UserGroupsResponse() {
		groupDns = new HashSet<String>();
	}
}
