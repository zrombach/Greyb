package gov.lab24.auth.security;

public enum GroupMembershipType {
	// OWNERORDADMINISTRATOR is a rollup / union of the two types used in /users/{{username}}/groups
	ADMINISTRATOR("administrators"), OWNER("owners"), MEMBER("members"), OWNERORADMINISTRATOR(null);
	
	private final String collectionName;
	
	GroupMembershipType(String collectionFieldName) {
		this.collectionName = collectionFieldName;
	}
	
	public String fieldName() {
		return collectionName;
	}
	
}
