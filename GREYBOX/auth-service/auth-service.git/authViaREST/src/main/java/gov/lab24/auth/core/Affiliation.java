package gov.lab24.auth.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = AffiliationSerializer.class)
@JsonDeserialize(using = AffiliationDeserializer.class)
public enum Affiliation {

	CANDIDATE, CONSULTANT, CONTRACTOR, EMPLOYEE, MILITARY, CIVILIAN,
	NONAGENCY("NON-AGENCY"), NONAGENCY_CIV("NON-AGENCY CIVILIAN"), NONAGENCY_CON("NON-AGENCY CONTRACTOR"), 
	NONAGENCY_MIL("NON-AGENCY MILITARY");

	private String humanReadable;

	Affiliation(String readable) {
		this.humanReadable = readable;
	}

	Affiliation() {
		this.humanReadable = this.name();
	}

	public String toString() {
		return humanReadable;
	}

	public static Affiliation factory(String readableName) {
		for (Affiliation value : Affiliation.values()) {
			if (value.toString().equals(readableName))
				return value;
		}
		throw new RuntimeException("Unable to use element " + readableName + " for clearances");
	}

}