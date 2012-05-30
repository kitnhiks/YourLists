package hiks.yourlists.server.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Item {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String name;

    @Persistent
    private int status;

    @Persistent
    private int priority;

    public Item() {

    }


	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}


	/**
	 * @param key the key to set
	 */
	public void setKey(Key key) {
		this.key = key;
	}

    

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}


	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}


	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}