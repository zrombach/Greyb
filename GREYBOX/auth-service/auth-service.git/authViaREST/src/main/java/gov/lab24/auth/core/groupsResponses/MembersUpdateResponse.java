package gov.lab24.auth.core.groupsResponses;

/**
 * Response for update, add, delete - meant to be 1 per member
 *
 */
public class MembersUpdateResponse {

	public String groupName;
	public String groupAttribute;
	public String members;	// TODO: this should be singular - validate
	public Disposition disposition;
	
	public enum Disposition {
		ADDED, ALREADY_ADDED, REMOVED, ALREADY_REMOVED, NOT_FOUND;
	}
}
