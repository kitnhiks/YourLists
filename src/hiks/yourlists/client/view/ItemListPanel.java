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

public class ItemListPanel extends VerticalPanel{

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
		itemsTable.setText(0, 0, "TODO");
		itemsTable.setText(0, 1, "PRIORITY");
		itemsTable.setText(0, 2, "STATUS");
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
	private void showItem(Item item, int row){
		// Item Name 
		itemsTable.setWidget(row, 0, new Label(item.getName()));

		// Item Priority
		String priority = Integer.toString(item.getPriority());
		FlowPanel priorityPanel = new FlowPanel();
		priorityPanel.setStylePrimaryName("priority");
		priorityPanel.addStyleDependentName(priority);
		priorityPanel.add(new Label(priority));
		itemsTable.setWidget(row, 1, priorityPanel);
		itemsTable.getWidget(row, 0).setStylePrimaryName("itemPriority-"+item.getPriority());

		// Item Status
		CheckBox statusCheckBox = new CheckBox();
		statusCheckBox.setValue(item.getStatus()==1);
		itemsTable.setWidget(row, 2, statusCheckBox);
		itemsTable.getWidget(row, 0).setStylePrimaryName("itemStatus-"+item.getStatus());

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
		int priority=2;
		if (highPriorityRadioButton.getValue()){
			priority = 1;
		}else if (lowPriorityRadioButton.getValue()){
			priority = 3;
		}
		return priority;
	}
}
/*
private void showItemPanel(){
VerticalPanel verticalPanel = new VerticalPanel();

HorizontalPanel menu = new HorizontalPanel();

Label titre = new Label();
titre.setText("Liste des items");
menu.add(titre);
menu.setCellVerticalAlignment(titre, HasVerticalAlignment.ALIGN_MIDDLE);

Button add = new Button("Ajouter");
add.addClickHandler(new ClickHandler() {

	@Override
	public void onClick(ClickEvent event) {
		detailItemPanel.setName("");
		detailItemPanel.setPriority("");
		detailItemPanel.setStatus("");
		detailItemPanel.setId(null);
		detailItemPanel.show();
	}
});
menu.add(add);
menu.setCellVerticalAlignment(add, HasVerticalAlignment.ALIGN_MIDDLE);

verticalPanel.add(menu);

verticalPanel.add(itemsTable);

detailItemPanel.getSave().addClickHandler(new ClickHandler() {

	@Override
	public void onClick(ClickEvent event) {
		if (detailItemPanel.getId() != null) {
			updateItem();
		} else {
			saveItem();
		}
		detailItemPanel.hide();
	}
});

loadItem();

RootPanel.get("wizard").add(verticalPanel);
}

private void updateItem() {
RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT,
		JSON_URL_ITEM);
builder.setHeader("Content-Type", "application/json");

JSONObject itemAsJSONObject = buildJSONItem();

try {
	builder.sendRequest(itemAsJSONObject.toString(),
			new RequestCallback() {
		public void onError(Request request, Throwable exception) {

		}

		public void onResponseReceived(Request request,
				Response response) {
			loadItem();
		}
	});
} catch (RequestException e) {
	System.out.println("RequestException");
}
}

private void saveItem() {
RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
		JSON_URL_ITEM);
builder.setHeader("Content-Type", "application/json");

JSONObject itemAsJSONObject = buildJSONItem();
try {
	builder.sendRequest(itemAsJSONObject.toString(),
			new RequestCallback() {
		public void onError(Request request, Throwable exception) {

		}

		public void onResponseReceived(Request request,
				Response response) {
			loadItem();
		}
	});
} catch (RequestException e) {
	System.out.println("RequestException");
}
}

private void loadItem() {
RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
		JSON_URL_ITEM);
builder.setHeader("Content-Type", "application/json");

try {
	builder.sendRequest(null, new RequestCallback() {
		public void onError(Request request, Throwable exception) {

		}

		public void onResponseReceived(Request request,
				Response response) {
			if (response.getStatusCode() == Response.SC_OK) {
				itemsTable.removeAllRows();
				String itemsAsJSONString = response.getText();
				JSONValue itemsAsJSONValue = JSONParser
						.parse(itemsAsJSONString);
				JSONArray itemsAsJSONArray = itemsAsJSONValue
						.isArray();
				for (int i = 0; i < itemsAsJSONArray.size(); i++) {
					JSONValue itemAsJSONValue = itemsAsJSONArray
							.get(i);
					JSONObject itemAsJSONObject = itemAsJSONValue
							.isObject();
					itemsTable.setText(i, 0, itemAsJSONObject
							.get("name").isString().stringValue());
					itemsTable.setText(i, 1, String.valueOf((int)itemAsJSONObject
							.get("status").isNumber().doubleValue()));
					itemsTable.setText(i, 2, String.valueOf((int)itemAsJSONObject
							.get("priority").isNumber().doubleValue()));
					final long id = (long) itemAsJSONObject
							.get("id").isNumber().doubleValue();
					Button detail = new Button("Detail");
					detail.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							loadDetailItem(id);
						}
					});
					itemsTable.setWidget(i, 3, detail);
					Button supprimer = new Button("Supprimer");
					supprimer.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							deleteItem(id);
						}
					});
					itemsTable.setWidget(i, 4, supprimer);
				}
			}
		}
	});
} catch (RequestException e) {
	System.out.println("RequestException");
}
}

private void deleteItem(long id) {
String detailUrl = JSON_URL_ITEM + id + "/";
RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE,
		detailUrl);
builder.setHeader("Content-Type", "application/json");
try {
	builder.sendRequest(null, new RequestCallback() {
		public void onError(Request request, Throwable exception) {
		}

		public void onResponseReceived(Request request,
				Response response) {
			loadItem();
		}
	});
} catch (RequestException e) {
	System.out.println("RequestException");
}
}

private void loadDetailItem(long id) {
String detailUrl = JSON_URL_ITEM + id + "/";
RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, detailUrl);
builder.setHeader("Content-Type", "application/json");
try {
	builder.sendRequest(null, new RequestCallback() {
		public void onError(Request request, Throwable exception) {
		}

		public void onResponseReceived(Request request,
				Response response) {
			if (response.getStatusCode() == Response.SC_OK) {
				String itemsAsJSONString = response.getText();
				JSONValue itemAsJSONValue = JSONParser
						.parse(itemsAsJSONString);
				JSONObject itemAsJSONObject = itemAsJSONValue
						.isObject();
				detailItemPanel.setName(itemAsJSONObject.get(
						"name").isString().stringValue());
				detailItemPanel.setPriority(String.valueOf((int)itemAsJSONObject.get(
						"priority").isNumber().doubleValue()));
				detailItemPanel.setStatus(String.valueOf((int)itemAsJSONObject.get(
						"status").isNumber().doubleValue()));

				final long id = (long) itemAsJSONObject.get("id")
						.isNumber().doubleValue();
				detailItemPanel.setId(id);
				detailItemPanel.show();
			}
		}
	});
} catch (RequestException e) {
	System.out.println("RequestException");
}
}

private JSONObject buildJSONItem() {
return null;
}*/