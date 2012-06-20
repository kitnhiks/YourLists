package hiks.yourlists.server.model;

import java.util.ArrayList;

public class Sharer {

	private ArrayList<String> mails;
	private String subject;
	private String body;
	private String url;

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
	
	
}
