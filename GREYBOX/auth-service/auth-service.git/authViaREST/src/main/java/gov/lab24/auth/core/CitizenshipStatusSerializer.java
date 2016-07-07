package gov.lab24.auth.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

class CitizenshipStatusSerializer extends JsonSerializer<CitizenshipStatus> {

	@Override
	public void serialize(CitizenshipStatus value, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		generator.writeString(value.toString());
	}

}