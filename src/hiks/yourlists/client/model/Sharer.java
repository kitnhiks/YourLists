package hiks.yourlists.client.model;

import java.util.ArrayList;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class Sharer {

	private ArrayList<String> mails;
	private String subject;
	private String body;
	private String url;

	public Sharer(){
		mails = new ArrayList<String>();
		subject = "";
		body = "";
		url = "";
	}
	
	/**
	 * @return the mails
	 */
	public ArrayList<String> getMails() {
		return mails;
	}

	/**
	 * @param mails the mails to set
	 */
	public void setMails(ArrayList<String> mails) {
		this.mails = mails;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	// JSON
	/**
	 * @return la représentation Json du sharer
	 */
	public JSONObject toJson() {
		JSONObject sharerAsJSONObject = new JSONObject();
		JSONValue sharerSubjectJSONValue = new JSONString(this.getSubject());
		sharerAsJSONObject.put("subject", sharerSubjectJSONValue);
		JSONValue sharerBodyJSONValue = new JSONString(this.getBody());
		sharerAsJSONObject.put("body", sharerBodyJSONValue);
		JSONValue sharerUrlJSONValue = new JSONString(this.getUrl());
		sharerAsJSONObject.put("url", sharerUrlJSONValue);
		JSONArray sharerMailsJSONValue = new JSONArray();
		int nbMails = this.getMails().size();
		for (int i=0; i<nbMails; i++){
			sharerMailsJSONValue.set(i, new JSONString(this.getMails().get(i)));
		}
		sharerAsJSONObject.put("mails", sharerMailsJSONValue);
		return sharerAsJSONObject;
	}



}
