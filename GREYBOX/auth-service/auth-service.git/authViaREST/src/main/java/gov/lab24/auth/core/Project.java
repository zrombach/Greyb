package gov.lab24.auth.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement()
@JsonIgnoreProperties(ignoreUnknown=true)		// Needed to nicely ignore id 
@JsonInclude(JsonInclude.Include.NON_NULL)		// when outputting a subset, ignore null values - lets us handle /dn, etc.
public class Project {

	// TODO: Regrab regular expression from project creation form
	static final String NAME_REGEX = "[^+=\r\n<>#;\\/,]";
	private static java.util.regex.Pattern checker = java.util.regex.Pattern.compile(NAME_REGEX);
	
	@NotEmpty
	@Pattern(regexp=NAME_REGEX)	// same validation associated with group name..
	public String name;

	@ObjectId
	@Id
	String _id;
	
	/**
	 * Default groups for any project
	 *   Includes DNs for folks or NPEs who are readers, editors, or 
	 */
	List<String> readers = new ArrayList<String>();
	List<String> editors = new ArrayList<String>();
	List<String> servers = new ArrayList<String>();
	
	// Group policyEditors;		// if we implement policy handling
		
	/**
	 * Groups configurable by the project itself
	 */
	List<Group> projectGroups = new ArrayList<Group>();
	
	List<String> owners= new ArrayList<String>();;	// dns/lookup keys of users associated
	
	@JsonIgnore
	public String getId() {
		return _id;
	}
	
	public List<Group> getProjectGroups() {
		return projectGroups;
	}
	
	public static boolean nameCheckFails(String name) {
		Matcher matcher = checker.matcher(name);
		return matcher.matches();
	}
	
	public List<String> getServers() {
		return servers;
	}
	
	public List<String> getReaders() {
		return readers;
	}
	
	public List<String> getEditors() {
		return editors;		
	}
	
	public List<String> getOwners() {
		return owners;
	}		

}
