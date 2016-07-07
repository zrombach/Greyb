package gov.lab24.auth.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.http.MediaType;

@Path("/extras/schemas")
public class SchemaResource {

	private Response getSchemaResponse(String schemaPath) {
		InputStream stream = this.getClass().getResourceAsStream(schemaPath);			
		if (stream !=null) {
			return Response.ok(stream).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path("/{type}/{action}")		
	public Response getUserInfoSchema(@PathParam("type") String resourceType, @PathParam("action") String resourceAction ) {

		return getSchemaResponse("/schemas/" + resourceType + "-" + resourceAction + ".json");
	}
	

}