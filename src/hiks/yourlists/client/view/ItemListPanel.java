package hiks.yourlists.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hiks.yourlists.client.model.Item;
import hiks.yourlists.client.model.ItemList;
import hiks.yourlists.shared.YourListConst;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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
	private final int COL_POSITION = 2;
	private final int COL_STATUS = 3;
	private final int FILTER_UP = 0;
	private final int FILTER_DOWN = 1;

	private int filter = COL_POSITION;
	private int filterOrder = FILTER_DOWN;
	
	private ItemList itemList;

	private Label itemListNameLabel;
	private FlexTable itemsTable;
	private Label errorMessage;
	private HorizontalPanel newItemPanel;
	private TextBox newItemNameTextBox;
	private Button createNewItemButton;
	private FlowPanel newItemPositionPanel;
	private Label newItemPositionLabel;

	private final String itemNameDefaultText = "Your item name...";

	public ItemListPanel(String itemListId, String itemListName){
		// Récupérer la liste
		httpGetList (itemListId, itemListName);
	}

	// REQUETAGE
	/**
	 * Récupère la liste depuis le service JSON
	 * @param itemListId l'id de la liste à récupérer
	 * @param itemListName le nom de la liste à récupérer pour vérifier que c'est bien la bonne
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
						setItems(itemListAsJSONObject.get("items").isArray());

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
	 * Récupère les items de la liste depuis le service JSON
	 * @param itemListId l'id de la liste à récupérer
	 */
	private void httpGetItems() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, YourListConst.JSON_URL_ITEMS.replace(YourListConst.JSON_VAR_LISTID, itemList.getId().toString()));
		builder.setHeader("Content-Type", "application/json");

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					showError(exception.getMessage());
				}

				public void onResponseReceived(Request request,	Response response) {
					if (response.getStatusCode() == Response.SC_OK) {
						String itemsAsJSONString = response.getText();
						JSONValue itemsAsJSONValue = JSONParser.parse(itemsAsJSONString);

						// Items
						setItems(itemsAsJSONValue.isArray());

						showItems();
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
	protected void httpAddItem(Item newItem) {

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
						newItemNameTextBox.selectAll();
						newItemNameTextBox.setFocus(true);

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
	protected void httpUpdateItem(Item item) {

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
						setItems(itemListAsJSONObject.get("items").isArray());

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
		newItemPanel.setCellHorizontalAlignment(newItemNameTextBox, HasHorizontalAlignment.ALIGN_CENTER);
		newItemPanel.setCellVerticalAlignment(newItemNameTextBox, HasVerticalAlignment.ALIGN_MIDDLE);
		newItemNameTextBox.addKeyDownHandler(new KeyDownHandler(){
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
					addNewItem();
				}
			}
			
		});
		
		// Position
		//TODO : replace avec une image qui change quand on click dessus
		// TODO : en faire un objet
		newItemPositionPanel = new FlowPanel();
		newItemPositionPanel.setStylePrimaryName("item_position");
		newItemPositionPanel.addStyleDependentName("1");
		newItemPositionLabel = new Label("1");
		newItemPositionPanel.add(newItemPositionLabel);
		newItemPanel.add(newItemPositionPanel);
		newItemPanel.setCellHorizontalAlignment(newItemPositionPanel, HasHorizontalAlignment.ALIGN_CENTER);
		newItemPanel.setCellVerticalAlignment(newItemPositionPanel, HasVerticalAlignment.ALIGN_MIDDLE);
		newItemPositionLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int newItemPosition = Integer.parseInt(((Label) event.getSource()).getText());
				newItemPosition = (newItemPosition+1)%3;
				newItemPositionPanel.removeStyleDependentName(newItemPositionLabel.getText());
				newItemPositionPanel.addStyleDependentName(String.valueOf(newItemPosition));
				newItemPositionLabel.setText(String.valueOf(newItemPosition));
			}
		});
		

		// Bouton pour ajouter l'item
		createNewItemButton = new Button("Add");
		createNewItemButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addNewItem();
			}
		});
		newItemPanel.add(createNewItemButton);
		newItemPanel.setCellHorizontalAlignment(createNewItemButton, HasHorizontalAlignment.ALIGN_CENTER);
		newItemPanel.setCellVerticalAlignment(createNewItemButton, HasVerticalAlignment.ALIGN_MIDDLE);
		
		this.add(newItemPanel);
	}

	private void addNewItem(){
		if (itemNameDefaultText.equals(newItemNameTextBox.getText())){
			return;
		}
		Item newItem = new Item();
		// Name
		newItem.setName(getNewItemName());

		// Position
		newItem.setPosition(getNewItemPosition());

		// Status
		newItem.setStatus(0);

		httpAddItem(newItem);
	}
	
	/**
	 * Vide le formulaire d'ajout d'Item
	 */
	private void clearAddItemZone(){
		newItemNameTextBox.setText(itemNameDefaultText);
		newItemPositionPanel.removeStyleDependentName(newItemPositionLabel.getText());
		newItemPositionPanel.addStyleDependentName("1");
		newItemPositionLabel.setText("1");
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
		// TODO : Faire des objets de type Titre
		itemsTable.setWidget(0, COL_NAME, new Label("TODO"));
		itemsTable.getWidget(0, COL_NAME);
		itemsTable.setWidget(0, COL_POSITION, new Label("POSITION"));
		itemsTable.setWidget(0, COL_STATUS, new Label("STATUS"));
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

		// Item Position
		String position = String.valueOf(item.getPosition());
		FlowPanel positionPanel = new FlowPanel();
		positionPanel.setStylePrimaryName("item_position");
		positionPanel.addStyleDependentName(position);
		Label positionLabel = new Label(position);
		positionPanel.add(positionLabel);
		itemsTable.setWidget(row, COL_POSITION, positionPanel);
		
		if (item.getStatus()==0){
			positionLabel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Item rowItem = itemList.getItems().get(row-1);

					int position = Integer.parseInt(((Label) event.getSource()).getText());
					FlowPanel positionPanel = (FlowPanel) itemsTable.getWidget(row, COL_POSITION);
					positionPanel.removeStyleDependentName(String.valueOf(position));
					position = (position+1)%3;
					positionPanel.addStyleDependentName(String.valueOf(position));
					rowItem.setPosition(position);
					httpUpdateItem(rowItem);
				}
			});
		}else{
			positionPanel.addStyleName("disabled");
		}

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
				itemsTable.getWidget(row, COL_POSITION).addStyleName("disabled");
				httpUpdateItem(rowItem);
				statusCheckBox.setEnabled(true);
			}
		});

		

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
		JSONValue itemPositionJSONValue = new JSONNumber(item.getPosition());
		itemAsJSONObject.put("position", itemPositionJSONValue);
		JSONValue itemStatusJSONValue = new JSONNumber(item.getStatus());
		itemAsJSONObject.put("status", itemStatusJSONValue);
		return itemAsJSONObject;
	}

	// GETTERS 

	public String getNewItemName(){
		return newItemNameTextBox.getText();
	}

	public int getNewItemPosition(){
		return Integer.parseInt(newItemPositionLabel.getText());
	}
	
	private void setItems(JSONArray itemsAsJSONArray){
		JSONObject itemsAsJSONObject;
		int nbItems = itemsAsJSONArray.size();
		List<Item> items = new ArrayList<Item>(nbItems);
		Item tmpItem;
		for (int i=0; i<nbItems; i++){
			itemsAsJSONObject = itemsAsJSONArray.get(i).isObject();
			tmpItem = new Item();
			tmpItem.setId((long)itemsAsJSONObject.get("key").isObject().get("id").isNumber().doubleValue());
			tmpItem.setName(itemsAsJSONObject.get("name").isString().stringValue());
			tmpItem.setPosition((int)itemsAsJSONObject.get("position").isNumber().doubleValue());
			tmpItem.setStatus((int)itemsAsJSONObject.get("status").isNumber().doubleValue());
			items.add(tmpItem); 
		}
		// TODO : gérer les autres colonnes que priorité (Collections.sort ?)
		Collections.sort(items, new Comparator<Item>(){
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.getPosition()>o2.getPosition()){
					return 1;
				}else if (o1.getPosition()<o2.getPosition()){
					return -1;
				}else{
					return new Long(o1.getId()).compareTo(new Long(o2.getId()));
				}
			}
		});
		
		itemList.setItems(items);
	}
}