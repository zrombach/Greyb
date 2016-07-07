package gov.lab24.auth.core;

import gov.lab24.auth.core.groupsResponses.MembersList;

import java.util.ArrayList;
import java.util.Set;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement()
@JsonIgnoreProperties(ignoreUnknown=true)		// Needed to nicely ignore id
@JsonInclude(JsonInclude.Include.NON_NULL)		// when outputting a subset, ignore null values - lets us handle /dn, etc.
public class Group {

	public String dn;

	@NotEmpty
	@Pattern(regexp=Project.NAME_REGEX)
	public String name;

	public String displayName;

	public String description;

	public String clearance;

	public ArrayList<String> formalAccesses = null;

	@XmlElement(name="private")
	@JsonProperty("private")
	public Boolean privateGroup;

	public Boolean visible;

	public String administratorsUri;

	public String ownersUri;

	public String membersUri;

	public MembersList administratorList;

	public MembersList ownerList;

	public MembersList memberList;

	public Set<String> owners;
	public Set<String> administrators;
	public Set<String> members;

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getClearance() {
		return clearance;
	}

	public void setClearance(String clearance) {
		this.clearance = clearance;
	}

	public ArrayList<String> getFormalAccesses() {
		return formalAccesses;
	}

	public void setFormalAccesses(ArrayList<String> formalAccesses) {
		this.formalAccesses = formalAccesses;
	}

	public Boolean getPrivateGroup() {
		return privateGroup;
	}

	public void setPrivateGroup(Boolean privateGroup) {
		this.privateGroup = privateGroup;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}


	public String getAdministratorsUri() {
		return administratorsUri;
	}

	public String getOwnersUri() {
		return ownersUri;
	}

	public String getMembersUri() {
		return membersUri;
	}

	private String _id;

	@Id
	@ObjectId
	@JsonIgnore
	public String getId() {
		return _id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((administratorList == null) ? 0 : administratorList.hashCode());
		result = prime * result + ((administrators == null) ? 0 : administrators.hashCode());
		result = prime * result + ((administratorsUri == null) ? 0 : administratorsUri.hashCode());
		result = prime * result + ((clearance == null) ? 0 : clearance.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((dn == null) ? 0 : dn.hashCode());
		result = prime * result + ((formalAccesses == null) ? 0 : formalAccesses.hashCode());
		result = prime * result + ((memberList == null) ? 0 : memberList.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		result = prime * result + ((membersUri == null) ? 0 : membersUri.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((ownerList == null) ? 0 : ownerList.hashCode());
		result = prime * result + ((owners == null) ? 0 : owners.hashCode());
		result = prime * result + ((ownersUri == null) ? 0 : ownersUri.hashCode());
		result = prime * result + ((privateGroup == null) ? 0 : privateGroup.hashCode());
		result = prime * result + ((visible == null) ? 0 : visible.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (administratorList == null) {
			if (other.administratorList != null)
				return false;
		} else if (!administratorList.equals(other.administratorList))
			return false;
		if (administrators == null) {
			if (other.administrators != null)
				return false;
		} else if (!administrators.equals(other.administrators))
			return false;
		if (administratorsUri == null) {
			if (other.administratorsUri != null)
				return false;
		} else if (!administratorsUri.equals(other.administratorsUri))
			return false;
		if (clearance == null) {
			if (other.clearance != null)
				return false;
		} else if (!clearance.equals(other.clearance))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (dn == null) {
			if (other.dn != null)
				return false;
		} else if (!dn.equals(other.dn))
			return false;
		if (formalAccesses == null) {
			if (other.formalAccesses != null)
				return false;
		} else if (!formalAccesses.equals(other.formalAccesses))
			return false;
		if (memberList == null) {
			if (other.memberList != null)
				return false;
		} else if (!memberList.equals(other.memberList))
			return false;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		if (membersUri == null) {
			if (other.membersUri != null)
				return false;
		} else if (!membersUri.equals(other.membersUri))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (ownerList == null) {
			if (other.ownerList != null)
				return false;
		} else if (!ownerList.equals(other.ownerList))
			return false;
		if (owners == null) {
			if (other.owners != null)
				return false;
		} else if (!owners.equals(other.owners))
			return false;
		if (ownersUri == null) {
			if (other.ownersUri != null)
				return false;
		} else if (!ownersUri.equals(other.ownersUri))
			return false;
		if (privateGroup == null) {
			if (other.privateGroup != null)
				return false;
		} else if (!privateGroup.equals(other.privateGroup))
			return false;
		if (visible == null) {
			if (other.visible != null)
				return false;
		} else if (!visible.equals(other.visible))
			return false;
		return true;
	}

}
