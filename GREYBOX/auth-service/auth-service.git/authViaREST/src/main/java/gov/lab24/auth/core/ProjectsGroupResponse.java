package gov.lab24.auth.core;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement()
@JsonIgnoreProperties(ignoreUnknown=true)		// Needed to nicely ignore id 
@JsonInclude(JsonInclude.Include.NON_NULL)		// when outputting a subset, ignore null values - lets us handle /dn, etc.
public class ProjectsGroupResponse {

	public Set<String> groups;
}
