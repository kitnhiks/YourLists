package hiks.yourlists.client.view;

import java.util.ArrayList;
import java.util.List;

import hiks.yourlists.client.model.Item;
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
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Hidden;

public class ItemListPanel extends VerticalPanel{

	private final int COL_ID = 0;
	private final int COL_NAME = 1;
	private final int COL_PRIORITY = 2;
	private final int COL_STATUS = 3;

	private ItemList itemList;

	private Label itemListNameLabel;
	private FlexTable itemsTable;
	private Label errorMessage;
	private HorizontalPanel newItemPanel;
	private TextBox newItemNameTextBox;
	private RadioButton highPriorityRadioButton;
	private RadioButton lowPriorityRadioButton;
	private RadioButton noPriorityRadioButton;
	private Button createNewItemButton;

	private final String itemNameDefaultText = "Your item name...";

	public ItemListPanel(String itemListId, String itemListName){
		// Récupérer la liste
		httpGetList (itemListId, itemListName);
	}

	// REQUETAGE
	/**
	 * Récupère la liste depuis le service JSON
	 * @param itemListId l'id de la liste à récupérer
	 */
	private void httpGetList(String itemListId, final String itemListName) {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, YourListConst.JSON_URL_ITEMLIST+ itemListId + "/");
		builder.setHeader("Content-Type", "application/json");

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					showError(exception.getMessage());
				}

				public void onResponseReceived(Request request,	Response response) {
					if (response.getStatusCode() == Response.SC_OK) {
						String itemListAsJSONString = response.getText();
						JSONValue itemListAsJSONValue = JSONParser.parse(itemListAsJSONString);
						JSONObject itemListAsJSONObject = itemListAsJSONValue.isObject();

						itemList = new ItemList();

						// List Id
						itemList.setId((long) itemListAsJSONObject.get("id").isNumber().doubleValue());

						// List Name
						itemList.setName(itemListAsJSONObject.get("name").isString().stringValue());
						if (!itemListName.equals(itemList.getName())){
							// TODO: error Handling ...
							showError("404:List not found");
							return;
						}

						// Items
						JSONArray itemsAsJSONArray = itemListAsJSONObject.get("items").isArray();
						JSONObject itemsAsJSONObject; 
						int nbItems = itemsAsJSONArray.size();
						List<Item> items = new ArrayList<Item>(nbItems);
						Item tmpItem;
						for (int i=0; i<nbItems; i++){
							itemsAsJSONObject = itemsAsJSONArray.get(i).isObject();
							tmpItem = new Item();
							tmpItem.setId((long)itemsAsJSONObject.get("key").isObject().get("id").isNumber().doubleValue());
							tmpItem.setName(itemsAsJSONObject.get("name").isString().stringValue());
							tmpItem.setPriority((int)itemsAsJSONObject.get("priority").isNumber().doubleValue());
							tmpItem.setStatus((int)itemsAsJSONObject.get("status").isNumber().doubleValue());
							items.add(tmpItem); 
						}
						itemList.setItems(items);

						showItemListPanel();
					}else{
						// TODO: error Handling ...
						showError(String.valueOf(response.getStatusCode()));
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("RequestException : "+e.getMessage());
		}
	}

	/**
	 * Ajoute l'item via une requête JSON
	 */
	protected void addItem(Item newItem) {

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, YourListConst.JSON_URL_ITEM.replace(YourListConst.JSON_VAR_LISTID, itemList.getId().toString()));
		builder.setHeader("Content-Type", "application/json");

		JSONObject itemAsJSONObject = buildJSONItem(newItem);
		try {
			builder.sendRequest(itemAsJSONObject.toString(), new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					showError(exception.getMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == Response.SC_CREATED) {
						// Clear the form
						clearAddItemZone();

						// Refresh the view
						httpRefreshItemList();
					}else{
						showError(String.valueOf(response.getStatusCode()));
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("RequestException : "+e.getMessage());
		}

	}

	/**
	 * Update l'item via une requête JSON
	 */
	protected void putItem(Item item) {

		RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, YourListConst.JSON_URL_ITEM.replace(YourListConst.JSON_VAR_LISTID, itemList.getId().toString())+item.getId());
		builder.setHeader("Content-Type", "application/json");

		JSONObject itemAsJSONObject = buildJSONItem(item);
		try {
			builder.sendRequest(itemAsJSONObject.toString(), new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					showError(exception.getMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == Response.SC_NO_CONTENT) {
						// Clear the form
						clearAddItemZone();

						// Refresh the view
						httpRefreshItemList();
					}else{
						showError(String.valueOf(response.getStatusCode()));
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("RequestException : "+e.getMessage());
		}

	}


	protected void httpRefreshItemList() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, YourListConst.JSON_URL_ITEMLIST+ itemList.getId() + "/");
		builder.setHeader("Content-Type", "application/json");

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					showError(exception.getMessage());
				}

				public void onResponseReceived(Request request,	Response response) {
					if (response.getStatusCode() == Response.SC_OK) {
						String itemListAsJSONString = response.getText();
						JSONValue itemListAsJSONValue = JSONParser.parse(itemListAsJSONString);
						JSONObject itemListAsJSONObject = itemListAsJSONValue.isObject();

						// Items
						JSONArray itemsAsJSONArray = itemListAsJSONObject.get("items").isArray();
						JSONObject itemsAsJSONObject; 
						int nbItems = itemsAsJSONArray.size();
						List<Item> items = new ArrayList<Item>(nbItems);
						Item tmpItem;
						for (int i=0; i<nbItems; i++){
							itemsAsJSONObject = itemsAsJSONArray.get(i).isObject();
							tmpItem = new Item();
							tmpItem.setId((long)itemsAsJSONObject.get("key").isObject().get("id").isNumber().doubleValue());
							tmpItem.setName(itemsAsJSONObject.get("name").isString().stringValue());
							tmpItem.setPriority((int)itemsAsJSONObject.get("priority").isNumber().doubleValue());
							tmpItem.setStatus((int)itemsAsJSONObject.get("status").isNumber().doubleValue());
							items.add(tmpItem); 
						}
						itemList.setItems(items);

						showItems();
					}else{
						showError(String.valueOf(response.getStatusCode()));
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("RequestException : "+e.getMessage());
		}

	}

	// AFFICHAGE

	/**
	 * Affiche le Panel contenant la liste et ces éléments
	 */
	private void showItemListPanel() {
		// Le champ du nom de la liste
		itemListNameLabel = new Label(itemList.getName());
		this.add(itemListNameLabel);
		this.setCellHorizontalAlignment(itemListNameLabel, HasHorizontalAlignment.ALIGN_CENTER);
		this.setCellVerticalAlignment(itemListNameLabel, HasVerticalAlignment.ALIGN_MIDDLE);

		// La zone pour ajouter un item
		showAddItemZone();


		// Les éléments de la liste
		showItemsTable();

		// Le bouton de refresh de la liste
		Button refreshListButton = new Button("Refresh");
		refreshListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				httpRefreshItemList();
			}
		});
		// TODO : Ajouter le style du bouton
		// TODO : ajouter les index de tabulation
		this.add(refreshListButton);
		this.setCellHorizontalAlignment(refreshListButton, HasHorizontalAlignment.ALIGN_RIGHT);
		this.setCellVerticalAlignment(refreshListButton, HasVerticalAlignment.ALIGN_MIDDLE);

		// Le bouton de retour à la création d'une liste
		Button backToCreateListButton = new Button("Create a new list");
		backToCreateListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("");
			}
		});
		// TODO : Ajouter le style du bouton
		// TODO : ajouter les index de tabulation
		this.add(backToCreateListButton);
		this.setCellHorizontalAlignment(backToCreateListButton, HasHorizontalAlignment.ALIGN_RIGHT);
		this.setCellVerticalAlignment(backToCreateListButton, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	/**
	 * Affiche la zone pour ajouter un item
	 */
	private void showAddItemZone() {
		newItemPanel = new HorizontalPanel();
		// Name
		newItemNameTextBox = new TextBoxWithInnerText(itemNameDefaultText);
		newItemPanel.add(newItemNameTextBox);

		// Priority
		//TODO : replace avec une image qui change quand on click dessus
		highPriorityRadioButton = new RadioButton("priority", "high");
		lowPriorityRadioButton = new RadioButton("priority", "low");
		noPriorityRadioButton = new RadioButton("priority", "none");
		noPriorityRadioButton.setValue(true);

		newItemPanel.add(highPriorityRadioButton);
		newItemPanel.add(lowPriorityRadioButton);
		newItemPanel.add(noPriorityRadioButton);

		// Bouton pour ajouter l'item
		createNewItemButton = new Button("Add");
		createNewItemButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (itemNameDefaultText.equals(newItemNameTextBox.getText())){
					return;
				}
				Item newItem = new Item();
				// Name
				newItem.setName(getNewItemName());

				// Priority
				newItem.setPriority(getNewItemPriority());

				// Status
				newItem.setStatus(0);

				addItem(newItem);
			}
		});

		newItemPanel.add(createNewItemButton);
		this.add(newItemPanel);
	}

	/**
	 * Vide le formulaire d'ajout d'Item
	 */
	private void clearAddItemZone(){
		newItemNameTextBox.setText(itemNameDefaultText);
		noPriorityRadioButton.setValue(true);
	}

	/**
	 * Affiche la zone des items
	 */
	private void showItemsTable(){
		itemsTable = new FlexTable();
		showItems();
		this.add(itemsTable);
		this.setCellHorizontalAlignment(itemsTable, HasHorizontalAlignment.ALIGN_CENTER);
		this.setCellVerticalAlignment(itemsTable, HasVerticalAlignment.ALIGN_MIDDLE);
	}

	/**
	 * Affiche les items
	 */
	private void showItems(){
		int nbItem = itemList.getItems()== null?0:itemList.getItems().size();

		itemsTable.clear();
		itemsTable.setText(0, COL_NAME, "TODO");
		itemsTable.setText(0, COL_PRIORITY, "PRIORITY");
		itemsTable.setText(0, COL_STATUS, "STATUS");
		Item tempItem;

		for (int i=0; i<nbItem; i++){
			tempItem = itemList.getItems().get(i);
			showItem(tempItem, i+1);
		}
	}

	/**
	 * Affiche un item
	 * @param item l'item à afficher
	 * @param row la ligne où ajouter l'item
	 */
	private void showItem(Item item, final int row){

		// Item Id
		Hidden id = new Hidden ();
		id.setValue(String.valueOf(item.getId()));
		itemsTable.setWidget(row, COL_ID, id);

		// Item Name 
		itemsTable.setWidget(row, COL_NAME, new Label(item.getName()));
		itemsTable.getWidget(row, COL_NAME).setStylePrimaryName("item_name");

		// Item Priority
		String priority = String.valueOf(item.getPriority());
		FlowPanel priorityPanel = new FlowPanel();
		priorityPanel.setStylePrimaryName("item_priority");
		priorityPanel.addStyleDependentName(priority);
		Label priorityLabel = new Label(priority);
		priorityPanel.add(priorityLabel);
		itemsTable.setWidget(row, COL_PRIORITY, priorityPanel);

		// Item Status
		CheckBox statusCheckBox = new CheckBox();
		statusCheckBox.setValue(item.getStatus()==1);
		itemsTable.setWidget(row, COL_STATUS, statusCheckBox);
		itemsTable.getWidget(row, COL_NAME).addStyleDependentName(String.valueOf(item.getStatus()));

		statusCheckBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Item rowItem = itemList.getItems().get(row-1);
				CheckBox statusCheckBox = (CheckBox)itemsTable.getWidget(row, COL_STATUS);
				statusCheckBox.setEnabled(false);
				int status = statusCheckBox.getValue()?1:0;
				rowItem.setStatus(status);
				itemsTable.getWidget(row, COL_NAME).addStyleDependentName(String.valueOf(rowItem.getStatus()));
				putItem(rowItem);
				statusCheckBox.setEnabled(true);
			}
		});

		if (item.getStatus()==0){
			priorityLabel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Item rowItem = itemList.getItems().get(row-1);

					int priority = Integer.parseInt(((Label) event.getSource()).getText());
					FlowPanel priorityPanel = (FlowPanel) itemsTable.getWidget(row, COL_PRIORITY);
					priority = (priority+1)%3;
					priorityPanel.addStyleDependentName(String.valueOf(priority));
					rowItem.setPriority(priority);
					putItem(rowItem);
				}
			});
		}

	}

	/**
	 * Affiche un message d'erreur
	 * @param message
	 */
	//TODO : centraliser la gestion d'erreur
	private void showError(String message){
		errorMessage = new Label(message);
		errorMessage.setStylePrimaryName("serverResponseLabelError");
		this.add(errorMessage);
	}

	// TODO : a centraliser
	private void showSpinner(Widget element) {

		Image spinner = new Image("img/loading.gif");
		spinner.setAltText("Loading...");
		//int elementIndex = DOM.getChildIndex(this.getElement(), element.getElement());
		this.insert(spinner, 6);
		this.setCellHorizontalAlignment(spinner, HasHorizontalAlignment.ALIGN_RIGHT);
		this.setCellVerticalAlignment(spinner, HasVerticalAlignment.ALIGN_MIDDLE);
		this.remove(element);
		// TODO : gérer un loader générique qui soit accessible par tous les controleurs
		// TODO : revoir la gestion des spinner sur bouton
		//			ButtonWithWait b = (ButtonWithWait) element;
		//			b.startWaiting();
	}

	// JSON

	private JSONObject buildJSONItem(Item item) {
		JSONObject itemAsJSONObject = new JSONObject();
		JSONValue itemNameJSONValue = new JSONString(item.getName());
		itemAsJSONObject.put("name", itemNameJSONValue);
		JSONValue itemPriorityJSONValue = new JSONNumber(item.getPriority());
		itemAsJSONObject.put("priority", itemPriorityJSONValue);
		JSONValue itemStatusJSONValue = new JSONNumber(item.getStatus());
		itemAsJSONObject.put("status", itemStatusJSONValue);
		return itemAsJSONObject;
	}

	// GETTERS 

	public String getNewItemName(){
		return newItemNameTextBox.getText();
	}

	public int getNewItemPriority(){
		int priority=1;
		if (highPriorityRadioButton.getValue()){
			priority = 0;
		}else if (lowPriorityRadioButton.getValue()){
			priority = 2;
		}
		return priority;
	}
}