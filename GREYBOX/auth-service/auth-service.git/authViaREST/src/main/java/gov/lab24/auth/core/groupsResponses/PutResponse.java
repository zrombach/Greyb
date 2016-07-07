package gov.lab24.auth.core.groupsResponses;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement()
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PutResponse extends GenericGroupResponse {

	@XmlElement(name="put")
	@JsonProperty(value="put")
	public String callSucceeded;

	/**
	 * Constructor for serialization
	 */
	public PutResponse() {
		
	}
	public PutResponse(Boolean succeeded) {
		this.callSucceeded = succeeded.toString();
		super.callSucceeded = this.callSucceeded;
	}

}
