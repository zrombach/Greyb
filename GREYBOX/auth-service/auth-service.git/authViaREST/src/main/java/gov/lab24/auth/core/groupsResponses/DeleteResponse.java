package gov.lab24.auth.core.groupsResponses;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement()
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteResponse extends GenericGroupResponse {

	@XmlElement(name="delete")
	@JsonProperty(value="delete")
	public String callSucceeded;
	
	public DeleteResponse() {
		
	}
	public DeleteResponse(Boolean outcome) {
		this.callSucceeded = outcome.toString();
		super.callSucceeded = this.callSucceeded;
	}
	
}
