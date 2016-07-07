package gov.lab24.auth.core.groupsResponses;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)		// when outputting a subset, ignore null values 
public class MembersList {

	public String listUri;
	
	public Set<String> user;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((listUri == null) ? 0 : listUri.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MembersList other = (MembersList) obj;
		if (listUri == null) {
			if (other.listUri != null)
				return false;
		} else if (!listUri.equals(other.listUri))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}
