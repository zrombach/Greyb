package gov.lab24.auth.resources;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.core.groupsResponses.MembersList;
import gov.lab24.auth.core.groupsResponses.MembersUpdateResponse;
import gov.lab24.auth.security.GroupMembershipType;
import org.mongojack.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * Helper class with common functionality for groups - to be extended by groups related resources
 *
 * TODO: this could more useful as an abstract class where the auth checks are abstract methods since that is the main difference between the group resource classes - as it stands things are not very DRY, particularly for the methods related to member management
 */
public class GroupsResourceSupport {
    protected JacksonDBCollection<Project, String> collection;

    @Context
    UriInfo uriInfo;

    protected enum MemberOperation {
        ADD, REPLACE, REMOVE
    }

    public GroupsResourceSupport(JacksonDBCollection<Project, String> collection) {
        this.collection = collection;
    }

    /**
     * @param groupName the full groupName, e.g. foo!bar
     * @return the project that corresponds to the groupName
     * and has a group with matching name, e.g. for foo!bar
     * will return the project named foo that has a group named bar.
     * Will return null if no matching project is found
     */
    protected Project getRootProject(String groupName) {
        // find the project, then retrieve the group from within it...
        DBCursor<Project> dbCursor = collection.find().elemMatch("projectGroups", DBQuery.is("name", groupName));

        // TODO: Refine this even further, to use elemMatch in the DBProjection
        // - having difficulty making that work with MongoJack
        // e.g. > db.projects.find( {name: "project1"}, { projectGroups:
        // {$elemMatch: { "name": "project1!foo"}}});

        if (dbCursor.hasNext()) {
            return dbCursor.next();
        } else {
            return null;
        }
    }

    /**
     * @param groupName the full groupName, e.g. foo!bar
     * @return the project that corresponds to the groupName, e.g for foo!bar
     * the project with name foo. Will return null if no matching project is found
     */
    protected Project getProjectByGroupName(String groupName) {
        DBCursor<Project> result = collection.find(DBQuery.is("name", parseProjectName(groupName)));

        if (result.hasNext()) {
            return result.next();
        } else {
            return null;
        }
    }

    /**
     * @param groupName the full groupName, e.g. foo!bar
     * @return the project name part, e.g. for foo!bar, returns foo
     */
    protected String parseProjectName(String groupName) {
        return groupName.substring(0, groupName.indexOf("!"));
    }

    protected List<MembersUpdateResponse> updateList(Set<String> listOfUsers, List<String> users, MemberOperation op) {

        if (listOfUsers == null) {
            listOfUsers = new HashSet<>(users.size());
        }
        List<MembersUpdateResponse> membersResponse = new ArrayList<>(users.size());

        Iterator<String> iterator = users.iterator();
        String username;
        int i = 0;

        while (iterator.hasNext()) {
            username = iterator.next();
            membersResponse.add(i, new MembersUpdateResponse());
            membersResponse.get(i).members = username;

            if (!isKnownUser(username)) {
                membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.NOT_FOUND;
                if (op == MemberOperation.REMOVE) {
                    // remove it anyway, to put us in a good state
                    listOfUsers.remove(username);
                }
            }
            switch (op) {
                case ADD:
                    if (listOfUsers.add(username)) {
                        membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.ADDED;
                    } else {
                        membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.ALREADY_ADDED;
                    }
                    break;

                case REMOVE:
                    if (listOfUsers.remove(username)) {
                        membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.REMOVED;
                    } else {
                        membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.ALREADY_REMOVED;
                    }
                    break;

                case REPLACE:
                    if (listOfUsers.contains(username)) {
                        membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.ALREADY_ADDED;
                    } else {
                        membersResponse.get(i).disposition = MembersUpdateResponse.Disposition.ADDED;
                    }
                    break; // handled above
            }
        }
        if (op == MemberOperation.REPLACE) {
            listOfUsers.clear();
            listOfUsers.addAll(users);
        }
        return membersResponse;
    }

    protected boolean isKnownUser(String username) {
        // TODO: check username against user collection
        return true;
    }

    protected GroupMembershipType checkMembershipType(String typeOfMembership) {
        if (typeOfMembership != null) {
            if (typeOfMembership.equals("administrator")) {
                return GroupMembershipType.ADMINISTRATOR;
            } else if (typeOfMembership.equals("owner")) {
                return GroupMembershipType.OWNER;
            } else if (typeOfMembership.equals("owneroradministrator")) {
                return GroupMembershipType.OWNERORADMINISTRATOR;
            }
        }
        return GroupMembershipType.MEMBER;
    }

    protected Group includeMemberInformation(Group projectGroup, boolean doReturnExpandedUsersList) {
        String path = "/groups/" + projectGroup.name + "/members";

        projectGroup.ownersUri = uriInfo.getBaseUri().resolve(path + "?attribute=owner").toString();
        projectGroup.ownerList = new MembersList();
        projectGroup.ownerList.listUri = projectGroup.ownersUri;

        projectGroup.administratorsUri = uriInfo.getBaseUri().resolve(path + "?attribute=administrator").toString();
        projectGroup.administratorList = new MembersList();
        projectGroup.administratorList.listUri = projectGroup.administratorsUri;

        projectGroup.membersUri = uriInfo.getBaseUri().resolve(path).toString(); // default
        // attribute
        // is
        // member
        projectGroup.memberList = new MembersList();
        projectGroup.memberList.listUri = projectGroup.membersUri;

        if (doReturnExpandedUsersList) {
            Set<String> users = listMembersOfGroupInternal(projectGroup, GroupMembershipType.MEMBER);
            Set<String> owners = listMembersOfGroupInternal(projectGroup, GroupMembershipType.OWNER);
            Set<String> administrators = listMembersOfGroupInternal(projectGroup, GroupMembershipType.ADMINISTRATOR);

            projectGroup.administratorList.user = administrators;
            projectGroup.ownerList.user = owners;
            projectGroup.memberList.user = users;
        }
        return projectGroup;
    }

    protected Set<String> listMembersOfGroupInternal(Group projectGroup, GroupMembershipType memberType) {

        Set<String> members = new HashSet<>();

        switch (memberType) {
            case MEMBER:
                if (projectGroup.members == null) {
                    projectGroup.members = members;
                }
                return projectGroup.members;
            case OWNER:
                if (projectGroup.owners == null) {
                    projectGroup.owners = members;
                }
                return projectGroup.owners;
            case ADMINISTRATOR:
                if (projectGroup.administrators == null) {
                    projectGroup.administrators = members;
                }
                return projectGroup.administrators;
            case OWNERORADMINISTRATOR:
                members.addAll(projectGroup.owners);
                members.addAll(projectGroup.administrators);
        }

        return members;
    }
}
