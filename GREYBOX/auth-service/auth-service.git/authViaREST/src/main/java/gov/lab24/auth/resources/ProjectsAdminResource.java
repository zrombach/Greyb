package gov.lab24.auth.resources;

import gov.lab24.auth.core.Group;
import gov.lab24.auth.core.Project;
import gov.lab24.auth.db.GroupsCollection;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.google.inject.Inject;

@Path("/extras/projects")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
/**
 * Implementation note: Not worrying about UsageRole for projectsAdmin web calls, as these are not available from 
 *  within the enterprise auth service.  Capabilities exposed here are instead handled via other tools.
 *
 */
public class ProjectsAdminResource {

    @Context UriInfo uriInfo;
    
    Set<Group> groups;
    private JacksonDBCollection<Project, String> collection;
    
    @Inject
    public ProjectsAdminResource(@GroupsCollection org.mongojack.JacksonDBCollection<Project, String> projects) {
        this.collection = projects;
    }
    
    @POST    
    public Response createOrUpdateProject(Project project) {
    	// since the location we'll actually access from is elsewhere for the GET... (ugh)
        URI projectLocation = uriInfo.getBaseUri().resolve("/projects/" + project.name); 
        
        // project name will have already been validated, using validator on object     
        DBCursor<Project> foundProject = collection.find(DBQuery.is("name", project.name));
        if (foundProject != null && foundProject.hasNext()) {
            Project existingProject = foundProject.next();
            // TODO: updating - make sure we're not updating subdocuments - groups
            collection.updateById(existingProject.getId(), project); 
            return Response.ok().contentLocation(projectLocation).build();
        }
        
        // creating
        WriteResult<Project, String> insert = collection.insert(project);
        if (insert.getError() == null) {
            return Response.created(projectLocation).entity(project).build();
        } 

        return Response.notModified().build();
    }

    @GET
    public List<Project> listProjects() {
        DBCursor<Project> cursor = collection.find();
        return cursor.toArray();
/*        List<Project> projects = new ArrayList<Project>(cursor.size());

        while (cursor.hasNext()) {
            projects.add(cursor.next());
        }

        return projects;
*/    }

    @DELETE
    @Path("/{projectname}")
    /**
     * Delete project and thus groups underneath.
     *   Does not delete user accounts
     * @param project
     */
    public Response deleteProject(@PathParam("projectname") String projectName) {
        WriteResult<Project, String> removeResult = collection.remove(DBQuery.is("name", projectName));
        if (removeResult.getN()==0) {
            if (removeResult.getError() != null) {
                return Response.noContent().status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                return Response.noContent().status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.noContent().build();
        }
    }

	public List<Project> listProjectsForUser(String identity) {
		DBCursor<Project> foundProject = collection.find(DBQuery.all("owners", identity));
		return foundProject.toArray();
	}

	public List<Project> listProjectsForUser(String serverIdentity, String projectName) {
		DBCursor<Project> foundProject = collection.find(DBQuery.and(DBQuery.all("owners", serverIdentity), DBQuery.is("name", projectName)));
		return foundProject.toArray();
	}

}
