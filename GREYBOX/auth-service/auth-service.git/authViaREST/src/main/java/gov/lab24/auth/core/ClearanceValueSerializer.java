package gov.lab24.auth.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
public class ClearanceValueSerializer extends JsonSerializer<ClearanceValue> {

	@Override
	public void serialize(ClearanceValue value, JsonGenerator generator, SerializerProvider provider) throws IOException,
			JsonProcessingException {	
		generator.writeString(value.toString());		
	}

}
