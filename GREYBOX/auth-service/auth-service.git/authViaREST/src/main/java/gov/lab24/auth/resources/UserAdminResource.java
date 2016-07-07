package gov.lab24.auth.resources;

import gov.lab24.auth.core.User;
import gov.lab24.auth.db.UsersCollection;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;

/**
 * Any items not in the original auth service implementation, but provided as
 * niceties for interacting with the stubbed data.
 *
 * Typically, delete, create, listings...
 *
 */
@Path("/extras/users")
@Produces({ MediaType.APPLICATION_JSON })
public class UserAdminResource {

	private JacksonDBCollection<User, String> collection;

	@Inject
	public UserAdminResource(@UsersCollection org.mongojack.JacksonDBCollection<User, String> users) {
		this.collection = users;
	}

	@PUT
	@Path("/{username}")
	@Consumes(value = MediaType.APPLICATION_JSON)
	public User saveUser(@Valid User userToSave) {

		// TODO: validate that user to save = dn of user on path
		WriteResult<User, String> writeResult = collection.update(DBQuery.is("dn", userToSave.getDn()), userToSave,
				true, false, WriteConcern.NORMAL);

		String error = writeResult.getError();
		if (error == null) {
			return userToSave;
		}
		return null;
	}

	@GET
	public List<User> listUsers() {

		DBCursor<User> cursor = collection.find();
		int count = cursor.size();
		List<User> users = new ArrayList<User>(count);
		User nextUser;
		while (cursor.hasNext()) {
			nextUser = cursor.next();
			users.add(nextUser);
		}
		return users;
	}

	@DELETE
	@Path("/{username}")
	public Response deleteUser(@PathParam("username") String username) {

		User findAndRemove = collection.findAndRemove(DBQuery.is("dn", username));
		if (findAndRemove == null) {
			return Response.noContent().status(Response.Status.NOT_FOUND).build();
		}
		return Response.noContent().build();

	}

}
