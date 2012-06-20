package hiks.yourlists.server.ressources;

import hiks.yourlists.server.model.Item;
import hiks.yourlists.server.model.ItemList;
import hiks.yourlists.server.model.Sharer;
import hiks.yourlists.shared.Const;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
import javax.ws.rs.core.UriInfo;

import com.google.gwt.user.client.Window;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/itemlist")
public class ItemListRESTService {
	
	protected Logger logger = Logger.getLogger("RestLogger");
	
	@Context
	UriInfo uriInfo;
	@GET
	@Produces("application/json")
	@SuppressWarnings("unchecked")
	/**
	 * Récupère la liste des listes existantes
	 * @return la liste des listes
	 */
	public List<Object> fetchItemLists() {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(ItemList.class);
		return (List<Object>)query.execute();
	}
	
	@GET
	@Path("{id}")
	@Produces("application/json")
	/**
	 * Récupère la liste à partir de son id
	 * @param id
	 * @return la liste
	 */
	public ItemList getItemList(@PathParam("id") Long id) {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		ItemList itemList = pm.getObjectById(ItemList.class, id);
		return itemList;
	}
	
	@GET
	@Path("{id}/items")
	@Produces("application/json")
	/**
	 * Récupère la liste des items à partir de son id
	 * @param id
	 * @return la liste
	 */
	public List<Item> getItems(@PathParam("id") Long id) {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		ItemList itemList = pm.getObjectById(ItemList.class, id);
		return itemList.getItems();
	}
	
	@POST
	@Consumes("application/json")
	/**
	 * Crée une liste
	 * @param itemList
	 * @return
	 */
	public Response createItemList(ItemList itemList){
		itemList.setId(null);
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		pm.makePersistent(itemList);
		return Response.created(URI.create("/"+itemList.getId())).build();
	}
	
	@POST
	@Path("{id}/share")
	@Consumes("application/json")
	public Response shareItemList(Sharer sharer, @PathParam("id") Long id){
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		String result = "";
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(Const.SHARE_MAIL_FROM, Const.SHARE_MAIL_NAME));
			ArrayList<String> mails = sharer.getMails(); 
			int nbMails = mails.size();
			for (int i=0; i<nbMails; i++){
				msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(mails.get(i)));
			}
			msg.setSubject(sharer.getSubject());
			msg.setText(sharer.getBody()+ "\n"+sharer.getUrl());
			Transport.send(msg);
		} catch (AddressException e) {
			logger.log(Level.SEVERE, "Erreur dans l'adresse : "+e.getMessage());
			result = "Erreur dans l'adresse";
		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Erreur dans l'envoie du message : "+e.getMessage());
			result = "Erreur dans l'envoie du message";
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "Erreur d'encoding : "+e.getMessage());
			result = "Erreur d'encoding";
		}
		if ("".equals(result)){
			return Response.ok().build();
		}else{
			return Response.serverError().build();
		}
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json")
	/**
	 * Met à jour une liste (nom de la liste)
	 * @param itemList
	 */
	public void updateItemList(ItemList itemList, @PathParam("id") Long id){
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		tx.begin();
		ItemList itemListToUpdate = pm.getObjectById(ItemList.class
				, id);
		itemListToUpdate.setName(itemList.getName());
		pm.makePersistent(itemListToUpdate);
		tx.commit();
		pm.close();
	}
	
	@DELETE
	@Path("{id}")
	@Produces("application/json")
	/**
	 * Supprime la liste ayant l'id donné
	 * @param id
	 */
	public void deleteItem(@PathParam("id") Long id) {
		PersistenceManager pm = RESTService.pmfInstance.getPersistenceManager();
		pm.deletePersistent(pm.getObjectById(ItemList.class, id));
	}
}