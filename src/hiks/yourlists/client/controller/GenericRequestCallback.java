package hiks.yourlists.client.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;


public abstract class GenericRequestCallback implements RequestCallback {
	private Logger logger = Logger.getLogger("YourListsLogger");
	
	public void onError(Request request, Throwable exception){
		logger.log(Level.SEVERE, request.toString()+" : "+exception.getMessage());
		handleError("Error during request.");
	}

	public void onResponseReceived(Request request,	Response response) {
		handleResponse(response);
	}
	
	public abstract void handleError(String message);
	
	public abstract void handleResponse(Response response);
	
}
