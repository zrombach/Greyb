package gov.lab24.auth.resources;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.User;
import gov.lab24.auth.core.UserAbbreviated;
import gov.lab24.auth.core.userResponses.UserGroupsResponse;
import gov.lab24.auth.db.UsersCollection;
import gov.lab24.auth.security.UsageRole;
import gov.lab24.auth.security.UsageRoleChecker;
import io.dropwizard.auth.Auth;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.springframework.security.core.userdetails.UserDetails;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;


@Path("/users/{username}")
@Produces({MediaType.APPLICATION_JSON})		// default for most is JSON - annotating additionally if otherwise
public class UserResource {

	private JacksonDBCollection<User, String> collection;
	private ProjectsResource projectHelper;
	private GroupsResource groupHelper;
	private UsageRoleChecker roleChecker;
	
	@Context
	UriInfo uriInfo;
	
	
	@Inject
	public UserResource(@UsersCollection org.mongojack.JacksonDBCollection<User, String> users, ProjectsResource projectResource, 
			GroupsResource groupResource, UsageRoleChecker roleChecker) {
		this.collection = users;
		this.projectHelper = projectResource;
		this.groupHelper = groupResource;
		this.roleChecker = roleChecker;
	}
	
	@GET	
	@Timed	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })  // Not supporting morse
	public UserAbbreviated getInfo(@Auth UserDetails user, @PathParam("username") String username) {	

		return getInfo2(user, username);
	}

	@GET
	@Path("/info")		
	@Timed	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })  // Not supporting morse
	public UserAbbreviated getInfo2(@Auth UserDetails user, @PathParam("username") String username) {
		UserAbbreviated retrievedUser = getUser(user, username);
		return retrievedUser;		
	}

	private UserAbbreviated getUser(UserDetails user, String username) {
		UsageRole usageRole = roleChecker.getUsageRoleForUserRecords(user, username);

		// TODO: determine filtering approach for formalaccess, cois, etc. 
		DBCursor<User> dbCursor = collection.find(DBQuery.is("dn", username));
		if (dbCursor.hasNext()) {		
			User curr = dbCursor.next();

			if (usageRole.equals(UsageRole.OTHER)) {
				// return new copy of just abbreviated stuff...
				return new UserAbbreviated(curr);
			} else if (usageRole.equals(UsageRole.SELF) || usageRole.equals(UsageRole.PROXY)){
				return curr;
			} else if (usageRole.equals(UsageRole.NONE)) {
				throw new WebApplicationException(Response.Status.FORBIDDEN);
			}
		} 
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	}
	
	@GET
	@Path("/dn")
	public UserAbbreviated getDn(@Auth UserDetails user, @PathParam("username") String username)  {
		
		UserAbbreviated retrievedUser = getUser(user, username);
		UserAbbreviated shortenedData = new UserAbbreviated();
		shortenedData.setDn(retrievedUser.getDn());
		return shortenedData;
	}
	
	@GET
	@Path("/clearances")	
	public UserAbbreviated getClearances(@Auth UserDetails user, @PathParam("username") String username) {
		UserAbbreviated retrievedUser = getUser(user, username);
		if (retrievedUser instanceof User) {
			User subsetUser = new User();
			subsetUser.setClearances(((User)retrievedUser).getClearances());
			return subsetUser;				
		} 
		throw new WebApplicationException(Response.Status.NOT_FOUND);		// we won't get clearance info if it's not for self or proxy
	}
		
	
	@GET
	@Path("/groups/{projectName}")
	public UserGroupsResponse getGroupsForUser(@Auth UserDetails user, @PathParam("username") String username, 
			@PathParam("projectName") String projectName, @QueryParam("groupsby") String typeOfGroupMembership) {
				
		UserAbbreviated retrievedUser = getUser(user, username);
		if (retrievedUser instanceof User) {
			List<Group> projectGroups = projectHelper.getGroups(projectName);
			if (projectGroups == null) {		// we didn't find the group at all - otherwise, would get an empty list
				throw new WebApplicationException(Response.Status.BAD_REQUEST);
			}				
			
			UserGroupsResponse response = new UserGroupsResponse();
			for (Group group : projectGroups) {				
				if (groupHelper.isMemberInGroup(group, username, typeOfGroupMembership)) {
					response.groupDns.add(group.dn);
				}
			}			
			return response;			
		} 
		throw new WebApplicationException(Response.Status.NOT_FOUND);		
		
	}
	
}
