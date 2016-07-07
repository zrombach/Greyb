package gov.lab24.auth.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

class AffiliationDeserializer extends JsonDeserializer<Affiliation> {

	@Override
	public Affiliation deserialize(JsonParser parser, DeserializationContext context) throws IOException,
			JsonProcessingException {
		return Affiliation.factory(parser.getValueAsString());
	}

}