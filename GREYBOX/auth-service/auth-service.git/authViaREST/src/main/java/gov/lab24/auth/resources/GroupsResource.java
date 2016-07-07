package gov.lab24.auth.resources;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.groupsResponses.DeleteResponse;
import gov.lab24.auth.core.groupsResponses.MemberInGroupGetResponse;
import gov.lab24.auth.core.groupsResponses.MembersGetResponse;
import gov.lab24.auth.core.groupsResponses.MembersList;
import gov.lab24.auth.core.groupsResponses.MembersUpdateResponse;
import gov.lab24.auth.core.groupsResponses.PutResponse;
import gov.lab24.auth.db.GroupsCollection;
import gov.lab24.auth.security.GroupMembershipType;
import gov.lab24.auth.security.UsageRole;
import io.dropwizard.auth.Auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Path("/groups/{groupname}")
@Consumes({MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Singleton
public class GroupsResource extends GroupsResourceSupport {

	private static String roleServer = "ROLE_SERVER";

	@Inject
	public GroupsResource(@GroupsCollection org.mongojack.JacksonDBCollection<Project, String> groups) {
		super(groups);
	}

	@GET
	@Path("info")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Group getGroup(@Auth UserDetails requestingUser, @PathParam("groupname") String groupName,
			@QueryParam("expanded") boolean doReturnExpandedUsersLists) {

		// TODO: Refine this even further, to use elemMatch in the DBProjection
		// - having difficulty making that work with MongoJack
		// e.g. > db.projects.find( {name: "project1"}, { projectGroups:
		// {$elemMatch: { "name": "project1!foo"}}});

		Project project = getRootProject(groupName);
		if (project != null) {
			for (Group projectGroup : project.getProjectGroups()) {
				if (projectGroup.getName().equals(groupName)) {

					// check whether we can return the group:
					// reader or editor of project, or group admin or owner of
					// the specific group
					UsageRole checkGroupUsageRole = checkGroupUsageRole(requestingUser, projectGroup);
					if (checkGroupUsageRole.equals(UsageRole.PROXY) || checkGroupUsageRole.equals(UsageRole.OTHER)) {
						if (isProjectEditor(requestingUser, project) || isProjectReader(requestingUser, project)
								|| isGroupAdmin(requestingUser, projectGroup)
								|| isGroupOwner(requestingUser, projectGroup)) {
							includeMemberInformation(projectGroup, doReturnExpandedUsersLists);
							return projectGroup;
						}
					}
					throw new WebApplicationException(Response.Status.FORBIDDEN);
				}
			}
		}
		throw new WebApplicationException(Response.Status.NOT_FOUND);

	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Group getGroupRootPath(@Auth UserDetails requestingUser, @PathParam("groupname") String groupName,
			@QueryParam("expanded") boolean doReturnExpandedUsersLists) {
		return getGroup(requestingUser, groupName, doReturnExpandedUsersLists);
	}

//	private String parseProjectName(String groupName) {
//		int firstSegment = groupName.indexOf("!");
//
//		return groupName.substring(0, firstSegment);
//	}

	@PUT
	@Path("info")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateGroup(@Auth UserDetails requestingUser, @PathParam("groupname") String groupName, Group group) {

		Project updatingProject = getRootProject(groupName);
		String projectName = parseProjectName(groupName);
		if (updatingProject == null) {
			// we know the group doesn't exist, based on how we execute the
			// getRootProject call...
			return Response.status(Response.Status.NOT_FOUND).entity(new PutResponse(false)).build();
		}
		List<Group> groups = updatingProject.getProjectGroups();

		// now, find the Group to update - we're literally going to replace the
		// item in the index
		// TODO: deal with projectGroups, owners, administrators, members... -
		// only replacing root object, not project groups
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).name.equals(groupName)) {

				Group projectGroup = groups.get(i);
				UsageRole checkGroupUsageRole = checkGroupUsageRole(requestingUser, projectGroup);
				if (checkGroupUsageRole.equals(UsageRole.PROXY)
						&& (isProjectEditor(requestingUser, updatingProject)
								|| isGroupAdmin(requestingUser, projectGroup) || isGroupOwner(requestingUser,
									projectGroup))) {

					groups.set(i, group);
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity(new PutResponse(false)).build();
				}
				break;
			}
		}
		WriteResult<Project, String> updateResult = collection.update(DBQuery.is("name", projectName),
				DBUpdate.set("projectGroups", groups));
		if (updateResult.getError() == null) {
			// TODO: this doesn't make sense. NoContent (204) should have no
			// content. However, this is what the spec seems to imply happens.
			return Response.status(Response.Status.NO_CONTENT).entity(new PutResponse(true)).build();
		}

		return Response.serverError().entity(new PutResponse(false)).build();
	}

	@PUT
	public Response updateGroupRootPath(@Auth UserDetails requestingUser, @PathParam("groupname") String groupName,
			Group group) {
		return updateGroup(requestingUser, groupName, group);
	}

	@POST
	@Path("info")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Group createGroup(@Auth UserDetails user, @PathParam("groupname") String groupName, Group group) {
		Project projectToUpdate = getProjectByGroupName(groupName);
		if (projectToUpdate == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		UsageRole checkGroupUsageRole = checkGroupUsageRole(user, group);
		if (checkGroupUsageRole.equals(UsageRole.PROXY) && isProjectEditor(user, projectToUpdate)) {

			// check to see if group already exists : using name as equivalency
			List<Group> projectGroups = projectToUpdate.getProjectGroups();
			for (Group groupEntry : projectGroups) {
				if (groupEntry.name.equals(group.name)) {
					throw new WebApplicationException(Response.Status.CONFLICT);
				}
			}
			projectToUpdate.getProjectGroups().add(group);

			WriteResult<Project, String> updateResult = collection.updateById(projectToUpdate.getId(), projectToUpdate);

			String error = updateResult.getError();
			if (error == null) {
				return group;
			}
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		} else {
			if (!checkGroupUsageRole.equals(UsageRole.PROXY)) {
				throw new WebApplicationException(Response.Status.FORBIDDEN);
			}
		}
		throw new WebApplicationException(Response.Status.NOT_FOUND); // spec
																		// declares
																		// that
																		// 404
																		// is
																		// also
																		// returned
																		// if
																		// lacking
																		// read/write
																		// permission
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Group createGroupRoot(@Auth UserDetails user, @PathParam("groupname") String groupName, Group group) {
		return createGroup(user, groupName, group);
	}

	@DELETE
	@Path("info")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteGroup(@Auth UserDetails user, @PathParam("groupname") String groupName) {

		Project updatingProject = getRootProject(groupName);
		String projectName = parseProjectName(groupName);
		if (updatingProject == null) {
			// we know the group doesn't exist, based on how we execute the
			// getRootProject call...
			return Response.status(Response.Status.NOT_FOUND).entity(new DeleteResponse(false)).build();
			// throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		List<Group> groups = updatingProject.getProjectGroups();

		// now, find the Group to update - we're literally going to replace the
		// item in the index
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).name.equals(groupName)) {
				Group projectGroup = groups.get(i);
				UsageRole checkGroupUsageRole = checkGroupUsageRole(user, projectGroup);
				if (checkGroupUsageRole.equals(UsageRole.PROXY) && isProjectEditor(user, updatingProject)) {
					groups.remove(i);
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity(new DeleteResponse(false)).build();
					// throw new
					// WebApplicationException(Response.Status.FORBIDDEN);
				}
				break;
			}
		}
		WriteResult<Project, String> updateResult = collection.update(DBQuery.is("name", projectName),
				DBUpdate.set("projectGroups", groups));
		if (updateResult.getError() == null) {
			return Response.ok(new DeleteResponse(true)).build();
		}

		return Response.serverError().entity(new DeleteResponse(false)).build();
		// throw new
		// WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
	}

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteGroupRoot(@Auth UserDetails user, @PathParam("groupname") String groupName) {
		return deleteGroup(user, groupName);
	}

	/*
	 * @GET
	 *
	 * @Path("dn") public Group getDnOfGroup(@PathParam("groupname") String
	 * groupName) {
	 *
	 * //TODO: 400 for invalid group name - what's an invalid group name?
	 * //TODO: retrieve from repository - return 404 if not found Group group =
	 * new Group(); group.dn = "foo";
	 *
	 * return group; }
	 */

	@GET
	@Path("members")
	public MembersGetResponse listMembersOfGroup(@Auth UserDetails requestingUser,
			@PathParam("groupname") String groupName, @QueryParam("attribute") String typeOfMembership) {

		Group retrievedGroup = getGroup(requestingUser, groupName, false);
		GroupMembershipType memberType = checkMembershipType(typeOfMembership);

		UsageRole checkGroupUsageRole = checkGroupUsageRole(requestingUser, retrievedGroup);
		if ( ( checkGroupUsageRole.equals(UsageRole.OTHER) || checkGroupUsageRole.equals(UsageRole.PROXY))
				&& ( isGroupAdmin(requestingUser, retrievedGroup) || isGroupOwner(requestingUser, retrievedGroup) 
						|| isProjectEditor(requestingUser, retrievedGroup) || isProjectReader(requestingUser, retrievedGroup))) {
			
		}
		MembersGetResponse membersResponse = new MembersGetResponse();
		membersResponse.group = groupName;
		membersResponse.groupAttribute = memberType.toString().toLowerCase();

		Set<String> members = listMembersOfGroupInternal(retrievedGroup, memberType);
		membersResponse.members = members;

		return membersResponse;
	}

	@PUT
	@Path("members")
	public List<MembersUpdateResponse> replaceMembersOfGroup(@Auth UserDetails requestingUser,
			@PathParam("groupname") String groupName, @QueryParam("attribute") String typeOfMembership,
			List<String> members) {
		return updateMembersOfGroup(requestingUser, groupName, typeOfMembership, members, MemberOperation.REPLACE);
	}

	@POST
	@Path("members")
	public List<MembersUpdateResponse> addMembersToGroup(@Auth UserDetails requestingUser,
			@PathParam("groupname") String groupName, @QueryParam("attribute") String typeOfMembership,
			List<String> members) {
		return updateMembersOfGroup(requestingUser, groupName, typeOfMembership, members, MemberOperation.ADD);
	}

	@DELETE
	@Path("members")
	public List<MembersUpdateResponse> deleteGroupMembers(@Auth UserDetails requestingUser,
			@PathParam("groupname") String groupName, @QueryParam("attribute") String typeOfMembership,
			List<String> members) {
		return updateMembersOfGroup(requestingUser, groupName, typeOfMembership, members, MemberOperation.REMOVE);
	}

	private List<MembersUpdateResponse> updateMembersOfGroup(UserDetails requestingUser, String groupName,
			String typeOfMembership, List<String> members, MemberOperation op) {
		List<MembersUpdateResponse> membersUpdateResponses = null;
		Set<String> updatedMemberList = null;

		Group retrievedGroup = getGroup(requestingUser, groupName, true);

		UsageRole checkGroupUsageRole = checkGroupUsageRole(requestingUser, retrievedGroup);
		// Simpler and safer to list conditions to move forward, and then 'else'
		// to throw exception, rather than wrap all logic
		if (checkGroupUsageRole.equals(UsageRole.PROXY)
				&& (isGroupAdmin(requestingUser, retrievedGroup) || isGroupOwner(requestingUser, retrievedGroup) || isProjectEditor(requestingUser, retrievedGroup))) {
			// move forward..
		} else {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}

		GroupMembershipType memberType = checkMembershipType(typeOfMembership);
		String groupFieldName = "";

		switch (memberType) {
		case OWNER:

			// additional case: only group owners can add, change, or remove
			// other group owners
			if (!isGroupOwner(requestingUser, retrievedGroup)) {
				throw new WebApplicationException(Response.Status.FORBIDDEN);
			}

			membersUpdateResponses = updateList(retrievedGroup.owners, members, op);
			updatedMemberList = retrievedGroup.owners;
			groupFieldName = "owners";
			break;
		case ADMINISTRATOR:
			membersUpdateResponses = updateList(retrievedGroup.administrators, members, op);
			updatedMemberList = retrievedGroup.administrators;
			groupFieldName = "administrators";
			break;
		case MEMBER:
			membersUpdateResponses = updateList(retrievedGroup.members, members, op);
			updatedMemberList = retrievedGroup.members;
			groupFieldName = "members";
			break;
		case OWNERORADMINISTRATOR:
			// no-op: not a potential for this flow
			break;
		default:
			break;
		}

		WriteResult<Project, String> updateResult = collection.update(
				DBQuery.elemMatch("projectGroups", DBQuery.is("name", groupName)),
				DBUpdate.set("projectGroups.$." + groupFieldName, updatedMemberList));

		if (updateResult.getError() == null) {
			for (MembersUpdateResponse response : membersUpdateResponses) {
				response.groupName = groupName;
				response.groupAttribute = memberType.toString().toLowerCase();
			}
			return membersUpdateResponses;
		} else {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("members/{membername}")
	public MemberInGroupGetResponse isMemberInGroup(@Auth UserDetails requestingUser,
			@PathParam("groupname") String groupName, @PathParam("membername") String memberName,
			@QueryParam("attribute") String typeOfMembership) {

		Group group = getGroup(requestingUser, groupName, true);
		boolean result = isMemberInGroup(group, memberName, typeOfMembership);

		// now, return formated response
		MemberInGroupGetResponse response = new MemberInGroupGetResponse(result);
		return response;
	}

	boolean isMemberInGroup(Group group, String username, String typeOfMembership) {
		GroupMembershipType memberType = checkMembershipType(typeOfMembership);
		return isMemberInGroup(group, username, memberType);
	}

	boolean isMemberInGroup(Group group, String username, GroupMembershipType memberType) {
		return listMembersOfGroupInternal(group, memberType).contains(username);
	}

	@POST
	@Path("members/{membername}")
	public void addIndividualToGroup(@QueryParam("attribute") String typeOfMembership) {

		// make sure sure exists in users collection

	}

	@DELETE
	@Path("members/{membername}")
	public void deleteIndividualFromGroup(@QueryParam("attribute") String typeOfMembership) {

	}

//	private Project getRootProject(String groupName) {
//		// find the project, then retrieve the group from within it...
//		DBCursor<Project> dbCursor = collection.find().elemMatch("projectGroups", DBQuery.is("name", groupName));
//
//		// TODO: Refine this even further, to use elemMatch in the DBProjection
//		// - having difficulty making that work with MongoJack
//		// e.g. > db.projects.find( {name: "project1"}, { projectGroups:
//		// {$elemMatch: { "name": "project1!foo"}}});
//
//		if (dbCursor.hasNext()) {
//			Project curr = dbCursor.next();
//			return curr;
//		} else {
//			return null;
//		}
//
//	}

	public Set<Group> getAllGroupsForUserWithMembershipType(String userId, GroupMembershipType memberType) {

		Set<Group> groupsForUser = new HashSet<Group>();

		if (memberType.equals(GroupMembershipType.OWNERORADMINISTRATOR)) {
			groupsForUser.addAll(getAllGroupsForUserWithMembershipType(userId, GroupMembershipType.OWNER));
			groupsForUser.addAll(getAllGroupsForUserWithMembershipType(userId, GroupMembershipType.ADMINISTRATOR));
			return groupsForUser;
		}

		// The below will return full project.. I just want to get the
		// projectGroups within it that match...
		// see:
		// http://stackoverflow.com/questions/10043965/how-to-get-a-specific-embedded-document-inside-a-mongodb-collection
		// (1) verify by having multiple groups associated with a collection
		// that the below does work
		// db.projects.find({"projectGroups.members": 'baz'},
		// {'projectGroups.$':1}).pretty()
		// OUTCOME: this doesn't work - would only return 1st group that
		// matched, not multiple groups if they match
		// TODO: better handle via $redact in Mongo 2.6+
		// (2) convert to collection.find() syntax.
		// DBCursor<Project> dbCursor =
		// collection.find().elemMatch("projectGroups",
		// DBQuery.is(memberType.fieldName(), userId));
		DBCursor<Project> dbCursor = collection.find(DBQuery.elemMatch("projectGroups",
				DBQuery.is(memberType.fieldName(), userId)));

		// now, filter out groups that don't actually include that type of
		// member (since returned projects)
		Project project = dbCursor.curr();
		while (project != null) {

			List<Group> groupsInProject = project.getProjectGroups();
			for (Group group : groupsInProject) {
				if (isMemberInGroup(group, userId, memberType)) {
					groupsForUser.add(group);
				}
				;
			}
			project = dbCursor.next();
		}
		return groupsForUser;
	}

	// ////////////////////////////
	// Permissions management
	// ////////////////////////////
	/**
	 * Very simplistic checks here, as group deals with permissions cross-checks
	 * that vary a bit from call to call
	 * 
	 * @param userRequesting
	 * @param group
	 * @return
	 */
	public UsageRole checkGroupUsageRole(UserDetails userRequesting, Group group) {

		if (isServer(userRequesting)) {
			return UsageRole.PROXY;
		} else {
			return UsageRole.OTHER; // no concept of SELF where groups are
									// involved
		}
	}

	public boolean isProjectReader(UserDetails userRequesting, Project project) {

		List<String> readers = project.getReaders();
		if (readers != null) {
			return readers.contains(userRequesting.getUsername());
		}
		return false;
	}

	public boolean isProjectReader(UserDetails userRequesting, Group group) {		
		Project project = getRootProject(group.getName());
		return isProjectReader(userRequesting, project);
	}

	public boolean isProjectEditor(UserDetails userRequesting, Project project) {
		List<String> editors = project.getEditors();
		if (editors != null) {
			return editors.contains(userRequesting.getUsername());
		}
		return false;
	}
	
	public boolean isProjectEditor(UserDetails userRequesting, Group group) {
		
		Project project = getRootProject(group.getName());
		return isProjectEditor(userRequesting, project);
	}

	private boolean isServer(UserDetails userRequesting) {
		Collection<? extends GrantedAuthority> authorities = userRequesting.getAuthorities();
		for (GrantedAuthority authority : authorities) {
			if (authority.getAuthority().equals(roleServer)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGroupAdmin(UserDetails requestingUser, Group group) {
		Set<String> administrators = group.administrators;
		if (administrators != null) {
			return administrators.contains(requestingUser.getUsername());
		}
		return false;
	}

	public boolean isGroupOwner(UserDetails requestingUser, Group group) {
		Set<String> owners = group.owners;
		if (owners != null) {
			return owners.contains(requestingUser.getUsername());
		}
		return false;
	}
}
