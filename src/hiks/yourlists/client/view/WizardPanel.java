package hiks.yourlists.client.view;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import hiks.yourlists.client.controller.GenericRequestCallback;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class WizardPanel extends FlowPanel{
	
	protected Logger logger = Logger.getLogger("YourListsLogger");
	protected Spinner spinner;
	private Label errorLabel, messageLabel;

	public WizardPanel(){
		spinner = new Spinner(this);
		showWizardPanel();
	}

	// REQUETAGE
	
	private void httpRequestJson(Method httpMethod, JSONObject jsonObject, String url, WizardRequestCallback callback){
		logger.log(Level.FINEST, "lance la requête "+url);
		RequestBuilder builder = new RequestBuilder(httpMethod, url);
		builder.setHeader("Content-Type", "application/json");
		try {
			spinner.startSpinner();
			builder.sendRequest(jsonObject==null?null:jsonObject.toString(), callback);
		} catch (RequestException e) {
			logger.log(Level.SEVERE, e.getMessage());
			showError("Error while sending request.");
			spinner.stopSpinner();
		}
	}
	
	/**
	 * Lance une requête http GET
	 * @param url
	 * @param callback
	 */
	protected void httpGetJson(String url, WizardRequestCallback callback) {
		httpRequestJson(RequestBuilder.GET, null, url, callback);
	}
	
	/**
	 * Lance une requête http POST pour récupérer du JSON
	 * @param jsonObject les données à poster au format JSON
	 * @param url
	 * @param callback
	 */
	protected void httpPostJson(JSONObject jsonObject, String url, WizardRequestCallback callback) {
		httpRequestJson(RequestBuilder.POST, jsonObject, url, callback);
	}

	/**
	 * Lance une requête http PUT pour récupérer du JSON
	 * @param jsonObject les données à mettre à jour au format JSON
	 * @param url
	 * @param callback
	 */
	protected void httpPutJson(JSONObject jsonObject, String url, WizardRequestCallback callback) {
		httpRequestJson(RequestBuilder.PUT, jsonObject, url, callback);
	}

	// AFFICHAGE
	
	private void showWizardPanel(){

		// Zone des message d'erreur
		errorLabel = new Label();
		errorLabel.getElement().setId("errorLabel");
		this.add(errorLabel);
		errorLabel.setVisible(false);
		
		// Zone des message
		messageLabel = new Label();
		messageLabel.getElement().setId("messageLabel");
		this.add(messageLabel);
		messageLabel.setVisible(false);
	}

	protected void showError(String message){
		// TODO : faire un errorPanel
		errorLabel.setText("Error ("+new Date().getTime()+") :"+message);
		errorLabel.setVisible(true);
		// TODO : virer le errorPanel après X seconds
	}
	
	protected void showMessage(String message){
		// TODO : faire un messagePanel
		messageLabel.setText("Message ("+new Date().getTime()+") :"+message);
		messageLabel.setVisible(true);
		// TODO : virer le messageLabel après click
	}
	
	protected void show404(String message){
		showError("404 : "+message);
	}

	// CALLBACK
	
	abstract class WizardRequestCallback extends GenericRequestCallback {

		@Override
		public void handleError(String message) {
			logger.log(Level.FINEST, "erreur de la requête ");
			showError(message);
			spinner.stopSpinner();
		}

		@Override
		public void handleResponse(Response response) {
			logger.log(Level.FINEST, "réponse de la requête");
			switch (response.getStatusCode()){
			case Response.SC_NOT_FOUND: // 404
				showError("Erreur requete not found");
				break;
			}
			showResponse(response);
			spinner.stopSpinner();
		}
		
		public abstract void showResponse(Response response);
	}
}