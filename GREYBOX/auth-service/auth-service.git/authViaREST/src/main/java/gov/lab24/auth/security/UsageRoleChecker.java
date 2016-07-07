package gov.lab24.auth.security;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.resources.GroupsResource;
import gov.lab24.auth.resources.ProjectsAdminResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UsageRoleChecker {

	private GroupsResource groupHelper;
	private ProjectsAdminResource projectHelper;

	@Inject
	UsageRoleChecker(GroupsResource groupResource, ProjectsAdminResource projectsResource) {
		this.groupHelper = groupResource;
		this.projectHelper = projectsResource;
	}

	private static String roleServer = "ROLE_SERVER";
	
	/**
	 * Use to check User info ONLY..  Not groups affiliated with user.
	 * @param userRequesting - user using the system
	 * @param userInfoRequested - info about which user
	 * @return
	 */
	public UsageRole getUsageRoleForUserRecords(UserDetails userRequesting, String userInfoRequested) {
		
		String requestingUserName = userRequesting.getUsername();
		
		if (requestingUserName.equals(userInfoRequested)) {
			return UsageRole.SELF;		// self is highest privilege, so won't check server access for user info
		} else  {
			
			if (isServer(userRequesting)) {			
				return UsageRole.PROXY;
			} 				
		}
		return UsageRole.OTHER;
		
	}
	
	public UsageRole getUsageRoleForProjectRecords(UserDetails userRequesting, String projectName) {
		
		List<Project> projects = projectHelper.listProjectsForUser(userRequesting.getUsername(), projectName);
		if (projects.size() == 0) {
			return UsageRole.NONE;
		} else {
			return getUsageRoleForProjectRecords(userRequesting, projects.get(0));
		}
	}
	
	public UsageRole getUsageRoleForProjectRecords(UserDetails userRequesting, Project project) {

		// if user is owner, we believe we're good
		if (project.getOwners() != null && project.getOwners().contains(userRequesting.getUsername())) {
			return UsageRole.PROXY;
		}
		
		if (isServer(userRequesting)) {
			if (project.getServers() != null && project.getServers().contains(userRequesting.getUsername())) {
				return UsageRole.PROXY;
			} 
		} else {
		
			List<String> privilegedUsers = new ArrayList<String>();
			privilegedUsers.addAll(project.getEditors());
			privilegedUsers.addAll(project.getReaders());
			
			if (privilegedUsers.contains(userRequesting.getUsername())) {
				return UsageRole.PROXY;
			}			
		}
		return  UsageRole.NONE;
	}
	
	/**
	 * Very simplistic checks here, as group deals with permissions cross-checks that vary a bit from call to call
	 * @param userRequesting
	 * @param group
	 * @return
	 */
	public UsageRole getUsageRoleForGroupRecords(UserDetails userRequesting, Group group) {
		
		if (isServer(userRequesting)) {
			return UsageRole.PROXY;
		} else {
			return UsageRole.OTHER;		// no concept of SELF where groups are involved
		}
	}
	
	public boolean isProjectReader(UserDetails userRequesting, Project project) {
		
		return project.getReaders().contains(userRequesting.getUsername());
		
	}
	
	public boolean isProjectEditor(UserDetails userRequesting, Project project) {
		return project.getEditors().contains(userRequesting.getUsername());
	}
	
	public static boolean isServer(UserDetails userRequesting) {
		Collection<? extends GrantedAuthority> authorities = userRequesting.getAuthorities();
		for (GrantedAuthority authority: authorities) {
			if (authority.getAuthority().equals(roleServer)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGroupAdmin(UserDetails requestingUser, Group group) {
		return group.administrators.contains(requestingUser.getUsername());
	}

	public boolean isGroupOwner(UserDetails requestingUser, Group group) {
		return group.owners.contains(requestingUser.getUsername());
	}
}
