package gov.lab24.auth.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ClearanceValueDeserializer extends JsonDeserializer<ClearanceValue> {

	@Override
	public ClearanceValue deserialize(JsonParser parser, DeserializationContext context) throws IOException,
			JsonProcessingException {
		return ClearanceValue.factory(parser.getValueAsString());
	}

}
