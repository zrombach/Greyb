package gov.lab24.auth.core;

import io.dropwizard.validation.ValidationMethod;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name="user")	// use same name as UserAbbreviated - no distinction to caller
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * 
 * Resource representation returned when the user is self or this is a system proxying a request
 *
 */
public class User extends UserAbbreviated {

	/**
	 * List below is intended to be in order of items from /info call, for those which are not returned in non-self, non-proxy access
	 * @see UserAbbreviated for list of non-self, non-proxy items
	 * All items are optional, unless they otherwise have a validation annotation attached
	 */
	private String employeeId;
	
	private ArrayList<ClearanceValue> clearances = null;
	
	private ArrayList<String> formalAccessList = null;
	
	private ArrayList<String> coiList = null;
	
	private ArrayList<String> briefingList = null;
	
	private ArrayList<String> formalGroupList = null;
	
	private CitizenshipStatus citizenStatus;
	
	private CountryAbbr country;
	
	private ArrayList<String> grantByEntities = null;
	
	/**
	 * Organization is the office(s) to which the person is assigned.  
	 * Note: this may be empty, even if the user is known to be listed with an organization in other systems.
	 */
	private ArrayList<String> organizations = null;
	
	private ArrayList<Affiliation> affiliations = null;
	
	private ArrayList<String> dissemControls = null;
	
	private ArrayList<String> dissemToList = null;
	
	// intentionally ignoring subRegion and topic - not appropriate in this usage scenario
		
	private String title;
	
	private String dutyOrg;
	
	
	public User() {
	}

	@XmlElement
	public String getEmployeeId() {
		return employeeId;
	}
	
	@XmlElement
	public ArrayList<ClearanceValue> getClearances() {
		return clearances;
	}

	
	@JsonIgnore
	@ValidationMethod(message="Clearance set given is not valid")
	public boolean isValidClearanceSet() {
		
		if (clearances == null)		// optional
			return true;
		
		if (clearances.contains(ClearanceValue.UNKNOWN)) {
			if (clearances.size() == 1)		// it should be the only level listed
				return true;
			return false;
		}
		
		return true;
	}

	/**
	 * valid combinations: UNKNOWN or any subset of SECRET, TOP SECRET, OR UNCLEARED
	 * @param clearances
	 * @see isValidClearanceSet for calculation of valid combinations
	 */
	public void setClearances(ArrayList<ClearanceValue> clearances) {
		this.clearances = clearances;		
				
	}
	
	@XmlElement
	public ArrayList<String> getFormalAccess() {
		return formalAccessList;
	}
	
	public void setFormalAccess( ArrayList<String> formalAccess ) {
		this.formalAccessList = formalAccess;
	}
	
	@XmlElement
	public ArrayList<String> getCoi() {
		return coiList;		
	}
	
	public void setCoi(ArrayList<String> coiList) {
		this.coiList = coiList;
	}
	
	@XmlElement
	public ArrayList<String> getBriefing() {
		return briefingList;
	}
	
	public void setBriefing( ArrayList<String> briefingList) {
		this.briefingList = briefingList;
	}
	
	@XmlElement
	public ArrayList<String> getFormalGroup() {
		return formalGroupList;
	}
	
	public void setFormalGroup( ArrayList<String> formalGroupList ) {
		this.formalGroupList = formalGroupList;
	}
	
	@XmlElement
	public CitizenshipStatus getCitizenshipStatus() {
		return citizenStatus;
	}
	
	public void setCitizenshipStatus(CitizenshipStatus citizenStatus) {
		this.citizenStatus = citizenStatus;
	}
	
	@XmlElement
	public CountryAbbr getCountry() {
		return country;
	}
	
	@XmlElement 
	public ArrayList<String> getGrantBy() {
		return grantByEntities;
	}
	
	public void setGrantBy(ArrayList<String> grantedBy ) {
		this.grantByEntities = grantedBy;
	}
	
	@XmlElement(name = "organization")
	@JsonProperty("organization")
	public ArrayList<String> getOrganizations() {
		return organizations;
	}

	public void setOrganizations( ArrayList<String> organizations ) {
		this.organizations = organizations;
	}
	
	@XmlElement(name="affiliations")
	@JsonProperty("affiliations")
	public ArrayList<Affiliation> getAffiliations() {
		return affiliations;
	}
	
	@XmlElement(name="dissemControl")
	@JsonProperty("dissemControl")
	public ArrayList<String> getDissemControls() {
		return dissemControls;
	}
	
	public void setDissemControl( ArrayList<String> dissemControlList ) {
		this.dissemControls = dissemControlList;
	}
	
	@XmlElement(name="dissemTo")
	@JsonProperty("dissemTo")
	public ArrayList<String> getDissemToList() {
		return dissemToList;
	}
	
	public void setDissemToList( ArrayList<String> dissemToList ) {
		this.dissemToList = dissemToList;
	}
	
	@XmlElement
	public String getTitle() {
		return title;
	}
	
	@XmlElement(name="dutyorg")
	public String getDutyOrg() {
		return dutyOrg;		
	}
}
