package hiks.yourlists.client.model;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class Item {

    private Long id;

    private String name;

    private int status;

    private int position;

    public Item() {

    }

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	// JSON
	/**
	 * @return la représentation Json de l'item
	 */
	public JSONObject toJson() {
		JSONObject itemAsJSONObject = new JSONObject();
		JSONValue itemNameJSONValue = new JSONString(this.getName());
		itemAsJSONObject.put("name", itemNameJSONValue);
		JSONValue itemPositionJSONValue = new JSONNumber(this.getPosition());
		itemAsJSONObject.put("position", itemPositionJSONValue);
		JSONValue itemStatusJSONValue = new JSONNumber(this.getStatus());
		itemAsJSONObject.put("status", itemStatusJSONValue);
		return itemAsJSONObject;
	}

}
