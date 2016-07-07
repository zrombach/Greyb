package gov.lab24.auth.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = CitizenshipStatusSerializer.class)
@JsonDeserialize(using = CitizenshipStatusDeserializer.class)
public enum CitizenshipStatus {

	NAT_US("NATURALIZED US"), NOT_INDICATED("NOT INDICATED"), OTHER, US;

	private String humanReadable;

	CitizenshipStatus(String readable) {
		this.humanReadable = readable;
	}

	CitizenshipStatus() {
		this.humanReadable = this.name();
	}

	public String toString() {
		return humanReadable;
	}

	public static CitizenshipStatus factory(String readableName) {
		for (CitizenshipStatus value : CitizenshipStatus.values()) {
			if (value.toString().equals(readableName))
				return value;
		}
		throw new RuntimeException("Unable to use element " + readableName + " for clearances");
	}

}