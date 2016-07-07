package gov.lab24.auth.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

// TODO: Validate enum returned from /info versus that returned from /clearance - docs describe different...
// TODO: The below serialization approach feels clunky, but various attempts to handle other ways have got 
//  serialization working, but not deserialization.  Reinvestigate, and otherwise apply to enums.
@JsonSerialize(using = ClearanceValueSerializer.class)
@JsonDeserialize(using = ClearanceValueDeserializer.class)
public enum ClearanceValue {
	SECRET, TOP_SECRET("TOP SECRET"), UNCLEARED, UNKNOWN, UNCLASSIFIED, CONFIDENTIAL;

	private String humanReadable;

	ClearanceValue(String readable) {
		this.humanReadable = readable;
	}

	ClearanceValue() {
		this.humanReadable = this.name();
	}

	public String toString() {
		return humanReadable;
	}

	public static ClearanceValue factory(String readableName) {
		for (ClearanceValue value : ClearanceValue.values()) {
			if (value.toString().equals(readableName))
				return value;
		}
		throw new RuntimeException("Unable to use element " + readableName + " for clearances");
	}
	
	
}
