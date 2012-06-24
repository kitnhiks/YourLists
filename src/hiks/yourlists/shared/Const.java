package hiks.yourlists.shared;

import com.google.gwt.core.client.GWT;

public class Const {

	public static final String JSON_URL_ITEMLIST = GWT.getHostPageBaseURL()	+ "itemlist/";
	public static final String JSON_VAR_LISTID = "{listid}";
	public static final String JSON_URL_ITEM = JSON_URL_ITEMLIST+JSON_VAR_LISTID+"/item/";
	public static final String JSON_URL_ITEMS = JSON_URL_ITEMLIST+JSON_VAR_LISTID+"/items/";
	
	public static final String PAGE_TITLE = "DoYourList | Kit n Hiks";

	public static final String SHARE_VAR_LIST_NAME = "<listname>";
	public static final String SHARE_MAIL_SUBJECT = "La liste "+SHARE_VAR_LIST_NAME;
	public static final String SHARE_MAIL_BODY = "Bonjour,\nOn vous a envoyé une liste";
	public static final String SHARE_MAIL_FROM = "kitnhiks@gmail.com";
	public static final String SHARE_MAIL_NAME = "Kit'N Hiks";
}
