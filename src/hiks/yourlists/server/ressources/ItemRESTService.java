package hiks.yourlists.server.ressources;

import hiks.yourlists.server.model.Item;
import hiks.yourlists.server.model.ItemList;

import java.net.URI;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.datanucleus.exceptions.NucleusObjectNotFoundException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/itemlist/{listid}/item")
public class ItemRESTService {

	@Context
	UriInfo uriInfo;
	@GET
	@Produces("application/json")
	@SuppressWarnings("unchecked")
	public List<Object> fetchItems() {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(Item.class);
		return (List<Object>)query.execute();
	}

	@GET
	@Path("{id}")
	@Produces("application/json")
	public Item getItem(@PathParam("id") Long id) {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		return pm.getObjectById(Item.class, KeyFactory.createKey(Item.class.getSimpleName(), id));
	}

	@POST
	@Consumes("application/json")
	public Response addItemToList(Item item, @PathParam("listid") Long listid){
		item.setKey(null);
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			ItemList parentList = pm.getObjectById(ItemList.class, listid);
			parentList.addItem(item);
			pm.makePersistent(parentList);
			tx.commit();
		} catch (JDOObjectNotFoundException e){
			return Response.status(Status.NOT_FOUND).build();
		}finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
		return Response.created(URI.create("/"+item.getKey().getId())).build();
	}

	@PUT
	@Consumes("application/json")
	public void updateItem(Item item, @PathParam("id") Long id){
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			Item itemToUpdate = pm.getObjectById(Item.class, KeyFactory.createKey(Item.class.getSimpleName(), id));
			itemToUpdate.setName(item.getName());
			itemToUpdate.setPriority(item.getPriority());
			itemToUpdate.setStatus(item.getStatus());
			pm.makePersistent(itemToUpdate);
			tx.commit();
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}

	}

	@DELETE
	@Path("{id}")
	@Produces("application/json")
	public void deleteItem(@PathParam("id") Long id) {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		pm.deletePersistent(pm.getObjectById(Item.class, KeyFactory.createKey(Item.class.getSimpleName(), id)));
	}
}