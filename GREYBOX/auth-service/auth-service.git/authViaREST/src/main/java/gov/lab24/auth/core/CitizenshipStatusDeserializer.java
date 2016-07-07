package gov.lab24.auth.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

class CitizenshipStatusDeserializer extends JsonDeserializer<CitizenshipStatus> {

	@Override
	public CitizenshipStatus deserialize(JsonParser parser, DeserializationContext context) throws IOException,
			JsonProcessingException {
		return CitizenshipStatus.factory(parser.getValueAsString());
	}

}