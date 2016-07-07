package gov.lab24.auth.core;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name="user")	// use same name as User - no distinction to receiving party
@JsonIgnoreProperties(ignoreUnknown=true)		// Needed to nicely ignore id 
@JsonInclude(JsonInclude.Include.NON_NULL)		// when outputting a subset, ignore null values - lets us handle /dn, etc.
/**
 * 
 * Resource representation returned when the user isn't proxied and isn't self
 *
 */
public class UserAbbreviated {
 
	private String _id;
	
	@Id
	@ObjectId
	@JsonIgnore
	public String getId() {
		return _id;
	}
	
	@Id
	@ObjectId
	@JsonIgnore
	public void setId(String id) {
		this._id= id;
	}
	
	/**
	 * Attributes listed in order of spec - see '*' elements
	 */
	@NotEmpty
	private String dn;
	
	private String displayName;
	
	@NotEmpty
	private String fullName;
	
	private String firstName;
	
	@NotEmpty
	private String lastName;
	
	@Email
	private String email;
	
	private String personalTitle;
	
	private String securePhone;
	
	private String telephone;
	
	@NotEmpty
	private String uid;
	
	public UserAbbreviated() {
	}
	
	/**
	 * Not intended for use publicly.  
	 * 
	 * DropWizard recommends resources be in one package and representations in another
	 * UserResource needs this call for getUser.  Avoid otherwise
	 * @param curr
	 */
	public UserAbbreviated(User curr) {
		this._id = curr.getId();
		this.dn = curr.getDn();
		this.displayName = curr.getDisplayName();
		this.fullName = curr.getFullName();
		this.firstName = curr.getFirstName();
		this.lastName = curr.getLastName();
		this.email = curr.getEmail();
		this.personalTitle = curr.getPersonalTitle();
		this.securePhone = curr.getSecurePhone();
		this.telephone = curr.getTelephone();
		this.uid = curr.getUid();
	}

	@XmlElement
	@JsonProperty()
	public String getDn() {
		return dn;
	}
	
	@XmlElement
	@JsonProperty()
	public String getDisplayName() {
		return displayName;
	}

	public void setDn(String dn2) {
		this.dn = dn2;		
	}

	@XmlElement()
	@JsonProperty()
	public String getFullName() {
		return fullName;
	}
	

	@XmlElement()
	@JsonProperty()
	public String getFirstName() {
		return firstName;
	}
	
	@XmlElement()
	@JsonProperty()
	public String getLastName() {
		return lastName;		
	}
	
	@XmlElement()
	@JsonProperty()
	public String getEmail() {
		return email;		
	}
	
	@XmlElement
	@JsonProperty()
	public String getPersonalTitle() {
		return personalTitle;
	}
	
	@XmlElement(name="secureTelephoneNumber")
	@JsonProperty("secureTelephoneNumber")
	public String getSecurePhone() {
		return securePhone;
	}
	
	@XmlElement(name="telephoneNumber")
	@JsonProperty("telephoneNumber")
	public String getTelephone() {
		return telephone;		
	}
	
	@XmlElement()
	@JsonProperty()
	public String getUid() {
		return uid;
	}
}
