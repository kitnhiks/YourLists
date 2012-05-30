package hiks.yourlists.server.ressources;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public class RESTService {
	public static final PersistenceManagerFactory pmfInstance = 
			JDOHelper.getPersistenceManagerFactory("transactions-optional");
}