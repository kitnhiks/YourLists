package hiks.yourlists.shared;

import com.google.gwt.core.client.GWT;

public class YourListConst {

	public static final String JSON_URL_ITEMLIST = GWT.getHostPageBaseURL()	+ "itemlist/";
	public static final String JSON_VAR_LISTID = "{listid}";
	public static final String JSON_URL_ITEM = JSON_URL_ITEMLIST+JSON_VAR_LISTID+"/item/";
}
