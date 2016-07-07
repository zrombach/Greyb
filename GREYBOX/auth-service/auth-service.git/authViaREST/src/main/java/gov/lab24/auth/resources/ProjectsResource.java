package gov.lab24.auth.resources;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.ProjectsGroupResponse;
import gov.lab24.auth.db.GroupsCollection;
import gov.lab24.auth.security.UsageRole;
import gov.lab24.auth.security.UsageRoleChecker;
import io.dropwizard.auth.Auth;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mongojack.DBCursor;
import org.mongojack.DBProjection;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Path("/projects/{projectname}")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Singleton
public class ProjectsResource {

	Set<Group> groups;
	private JacksonDBCollection<Project, String> collection;
	private UsageRoleChecker roleChecker;
	
	
	@Inject
	public ProjectsResource(@GroupsCollection org.mongojack.JacksonDBCollection<Project, String> projects, UsageRoleChecker roleChecker) {
		this.collection = projects;
		this.roleChecker = roleChecker;
	}
	
	@Path("/group")
	@GET
	public ProjectsGroupResponse getGroupNames(@Auth UserDetails user, @PathParam("projectname") String projectName) {
		
		if (Project.nameCheckFails(projectName)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);	
		}
				
		DBCursor<Project> dbCursor = collection.find(DBQuery.is("name", projectName), DBProjection.include("projectGroups.name", "owners", "readers", "editors", "servers"));
 
		if (dbCursor.hasNext()) {
			Project project = dbCursor.next();
			UsageRole roleCheck = roleChecker.getUsageRoleForProjectRecords(user,  project);
			if (roleCheck == UsageRole.NONE) {
				throw new WebApplicationException(Response.Status.FORBIDDEN);
			}
			ProjectsGroupResponse response = new ProjectsGroupResponse();
			response.groups = new HashSet<String>();
			
			List<Group> projectGroups = project.getProjectGroups();
			for (Group group : projectGroups) {
				response.groups.add(group.name);	// TODO: determine relationship between dn and name - holding to name for the moment for mocking purposes
			}
			return response;
		} 
		throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);	
	}
	
	
	/**
	 * Not exposed as part of API - helper for interactions with calls from UserResource.  
	 *  Implementation decision to keep collection outside of UserResource
	 */
	List<Group> getGroups(String projectName) {
		DBCursor<Project> dbCursor = collection.find(DBQuery.is("name", projectName));
		
		if (dbCursor.hasNext()) {
			Project project = dbCursor.next();
			return project.getProjectGroups();
		} else {
			return null;
		}
	}
}
