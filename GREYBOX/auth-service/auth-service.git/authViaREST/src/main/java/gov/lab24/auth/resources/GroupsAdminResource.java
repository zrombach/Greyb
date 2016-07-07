package gov.lab24.auth.resources;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.groupsResponses.MembersUpdateResponse;
import gov.lab24.auth.core.groupsResponses.PutResponse;
import gov.lab24.auth.db.GroupsCollection;
import gov.lab24.auth.security.GroupMembershipType;
import io.dropwizard.auth.Auth;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.springframework.security.core.userdetails.UserDetails;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("/extras/groups/{groupName}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupsAdminResource extends GroupsResourceSupport {

    @Inject
    public GroupsAdminResource(@GroupsCollection JacksonDBCollection<Project, String> collection) {
        super(collection);
    }

    @PUT
    public Response upsertGroup(@PathParam("groupName") String groupName, Group group) {
        Project project = getProjectByGroupName(groupName);
        if (project == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new PutResponse(false)).build();
        }
        List<Group> groups = project.getProjectGroups();

        boolean found = false;
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).name.equals(groupName)) {
                groups.set(i, group);
                found = true;
            }
        }

        if (!found) {
            groups.add(group);
        }

        return putGroups(groupName, groups);
    }

    @DELETE
    public Response deleteGroup(@PathParam("groupName") String groupName) {
        Project rootProject = getRootProject(groupName);
        if (rootProject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Group> groups = rootProject.getProjectGroups();

        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).name.equals(groupName)) {
                groups.remove(i);
            }
        }

        return putGroups(groupName, groups);
    }

    @POST
    @Path("/members")
    public List<MembersUpdateResponse>  addMembers(@PathParam("groupName") String groupName, @QueryParam("attribute") String typeOfMembership, List<String> members) {
        return updateMembersOfGroup(groupName, typeOfMembership, members, MemberOperation.ADD);
    }

    @DELETE
    @Path("/members")
    public List<MembersUpdateResponse> deleteGroupMembers(@PathParam("groupName") String groupName, @QueryParam("attribute") String typeOfMembership, List<String> members) {
        return updateMembersOfGroup(groupName, typeOfMembership, members, MemberOperation.REMOVE);
    }

    private Response putGroups(String groupName, List<Group> groups) {
        if (collection.update(DBQuery.is("name", parseProjectName(groupName)),
                DBUpdate.set("projectGroups", groups)).getError() == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.serverError().build();
        }
    }

    private Group getGroup(String groupName) {
        Project project = getRootProject(groupName);
        if (project != null) {
            for (Group projectGroup : project.getProjectGroups()) {
                if (projectGroup.getName().equals(groupName)) {
                    includeMemberInformation(projectGroup, true);
                    return projectGroup;
                }
            }
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private List<MembersUpdateResponse> updateMembersOfGroup(String groupName, String typeOfMembership, List<String> members, MemberOperation op) {
        List<MembersUpdateResponse> membersUpdateResponses = null;
        Set<String> updatedMemberList = null;

        Group retrievedGroup = getGroup(groupName);

        GroupMembershipType memberType = checkMembershipType(typeOfMembership);
        String groupFieldName = "";

        switch (memberType) {
            case OWNER:
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
}
