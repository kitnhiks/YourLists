package hiks.yourlists.client.view;

import hiks.yourlists.client.model.ItemList;
import hiks.yourlists.shared.YourListConst;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ItemListCreationPanel extends VerticalPanel{

	private final String itemListNameDefaultText = "Your list name...";
	private HorizontalPanel itemListNamePanel;
	private TextBox itemListNameTextBox;
	private Label errorMessage;
	private Button createListButton;

	public ItemListCreationPanel(){

		// TODO : refacto clean up 
		itemListNamePanel = new HorizontalPanel();
		// Le champ du nom de la liste
		itemListNameTextBox = new TextBoxWithInnerText(itemListNameDefaultText);
		itemListNamePanel.add(itemListNameTextBox);
		itemListNamePanel.setCellHorizontalAlignment(itemListNameTextBox, HasHorizontalAlignment.ALIGN_CENTER);
		itemListNamePanel.setCellVerticalAlignment(itemListNameTextBox, HasVerticalAlignment.ALIGN_MIDDLE);

		this.add(itemListNamePanel);
		this.setCellHorizontalAlignment(itemListNamePanel, HasHorizontalAlignment.ALIGN_CENTER);
		this.setCellVerticalAlignment(itemListNamePanel, HasVerticalAlignment.ALIGN_MIDDLE);

		// Le bouton de création de la liste
		createListButton = new ButtonWithWait("Create");		
		createListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (itemListNameDefaultText.equals(itemListNameTextBox.getText())){
					return;
				}
				httpCreateListe();
			}
		});
		// TODO : Ajouter le style du bouton
		// TODO : ajouter les index de tabulation
		this.add(createListButton);
		this.setCellHorizontalAlignment(createListButton, HasHorizontalAlignment.ALIGN_RIGHT);
		this.setCellVerticalAlignment(createListButton, HasVerticalAlignment.ALIGN_MIDDLE);
	}

	// REQUETAGE

	protected void httpCreateListe() {

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, YourListConst.JSON_URL_ITEMLIST);
		builder.setHeader("Content-Type", "application/json");

		ItemList itemList = new ItemList();
		itemList.setName(itemListNameTextBox.getText());

		JSONObject itemListAsJSONObject = buildJSONItemList(itemList);
		try {
			builder.sendRequest(itemListAsJSONObject.toString(), 
					new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					showError(exception.getMessage());
				}

				public void onResponseReceived(Request request,	Response response) {
					if (response.getStatusCode() == Response.SC_CREATED) {
						// retrieve the id from the created url
						String[] urlToken = response.getHeader("Location").split("/");
						String listId = urlToken[urlToken.length-1];
						History.newItem(URL.encode(itemListNameTextBox.getText())+"-"+listId);
					}else{
						showError(String.valueOf(response.getStatusCode()));
					}

				}
			});
			showSpinner(createListButton);
		} catch (RequestException e) {
			System.out.println("RequestException");
		}
	}

	// AFFICHAGE

	// TODO : a centraliser
	private void showSpinner(Widget element) {

		Image spinner = new Image("img/loading.gif");
		spinner.setAltText("Loading...");
		//int elementIndex = DOM.getChildIndex(this.getElement(), element.getElement());
		this.insert(spinner, 2);
		this.setCellHorizontalAlignment(spinner, HasHorizontalAlignment.ALIGN_RIGHT);
		this.setCellVerticalAlignment(spinner, HasVerticalAlignment.ALIGN_MIDDLE);
		this.remove(element);
		// TODO : gérer un loader générique qui soit accessible par tous les controleurs
		// TODO : revoir la gestion des spinner sur bouton
		//		ButtonWithWait b = (ButtonWithWait) element;
		//		b.startWaiting();
	}

	private void showError(String message){
		errorMessage = new Label(message);
		errorMessage.setStylePrimaryName("serverResponseLabelError");
		itemListNamePanel.add(errorMessage);
	}

	// JSON

	private JSONObject buildJSONItemList(ItemList itemList) {
		JSONObject itemListAsJSONObject = new JSONObject();
		JSONValue itemListNameJSONValue = new JSONString(itemList.getName());
		itemListAsJSONObject.put("name", itemListNameJSONValue);
		return itemListAsJSONObject;
	}
}
