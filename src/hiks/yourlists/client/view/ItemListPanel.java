package hiks.yourlists.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import hiks.yourlists.client.model.Item;
import hiks.yourlists.client.model.ItemList;
import hiks.yourlists.shared.YourListConst;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Hidden;

public class ItemListPanel extends WizardPanel{

	private final int COL_ID = 0;
	private final int COL_NAME = 1;
	private final int COL_POSITION = 2;
	private final int COL_STATUS = 3;
	private final int FILTER_UP = 0;
	private final int FILTER_DOWN = 1;

	private int filter = COL_POSITION;
	private int filterOrder = FILTER_DOWN;

	private int newItemPosition;

	private ItemList itemList;

	private Label itemListNameLabel;
	private FlexTable itemsTable;
	private Label errorMessage;
	private HorizontalPanel newItemPanel;
	private TextBox newItemNameTextBox;
	private Button createNewItemButton;

	private final String itemNameDefaultText = "Your item name...";

	public ItemListPanel(String itemListId, String itemListName){
		super();
		// Récupérer la liste
		httpGetList (itemListId, itemListName);
	}


	// AFFICHAGE

	/**
	 * Affiche le Panel contenant la liste et ces éléments
	 */
	private void showItemListPanel() {
		// Le champ du nom de la liste
		itemListNameLabel = new Label(itemList.getName());
		this.add(itemListNameLabel);

		// La zone pour ajouter un item
		showAddItemZone();


		// Les éléments de la liste
		showItemsTable();

		// Le bouton de refresh de la liste
		Button refreshListButton = new Button("Refresh");
		refreshListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				httpGetItems();
			}
		});
		// TODO : Ajouter le style du bouton
		// TODO : ajouter les index de tabulation
		this.add(refreshListButton);

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

	}

	/**
	 * Affiche la zone pour ajouter un item
	 */
	private void showAddItemZone() {
		newItemPanel = new HorizontalPanel();

		// Name
		newItemNameTextBox = new TextBoxWithInnerTextSubmit(itemNameDefaultText, true){
			@Override
			protected void onSubmit(){
				addNewItem();
			}
		};
		newItemPanel.add(newItemNameTextBox);

		// Position

		// Bouton pour ajouter l'item
		// TODO : remove bouton
		//		createNewItemButton = new Button("Add");
		//		createNewItemButton.addClickHandler(new ClickHandler(){
		//			@Override
		//			public void onClick(ClickEvent event) {
		//				addNewItem();
		//			}
		//		});
		//		newItemPanel.add(createNewItemButton);

		this.add(newItemPanel);
	}

	/**
	 * Vide le formulaire d'ajout d'Item
	 */
	private void clearAddItemZone(){
		newItemNameTextBox.setText(itemNameDefaultText);
	}

	/**
	 * Affiche la zone des items
	 */
	private void showItemsTable(){
		itemsTable = new FlexTable();
		showItems();
		this.add(itemsTable);
	}

	/**
	 * Affiche les items
	 */
	private void showItems(){
		int nbItem = itemList.getItems()== null?0:itemList.getItems().size();

		itemsTable.clear();
		Item tempItem;

		for (int i=0; i<nbItem; i++){
			tempItem = itemList.getItems().get(i);
			showItem(tempItem, i+1);
		}
		newItemPosition = nbItem+1;
	}

	/**
	 * Affiche un item
	 * @param item l'item à afficher
	 * @param row la ligne où ajouter l'item
	 */
	private void showItem(Item item, final int row){

		boolean status = item.getStatus()==1;

		// Item Id
		Hidden id = new Hidden ();
		id.setValue(String.valueOf(item.getId()));
		itemsTable.setWidget(row, COL_ID, id);

		// Item Name 
		itemsTable.setWidget(row, COL_NAME, new Label(item.getName()));
		itemsTable.getWidget(row, COL_NAME).setStylePrimaryName("item_name");

		// Item Position
		itemsTable.setWidget(row, COL_POSITION, new Label(String.valueOf(item.getPosition())));
		itemsTable.getWidget(row, COL_POSITION).setStylePrimaryName("item_position");
		itemsTable.getWidget(row, COL_POSITION).setStyleDependentName("disable", status);

		// Item Status
		CheckBox statusCheckBox = new CheckBox();
		statusCheckBox.setValue(status);
		itemsTable.setWidget(row, COL_STATUS, statusCheckBox);
		itemsTable.getWidget(row, COL_NAME).setStyleDependentName("disable", status);

		statusCheckBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Item rowItem = itemList.getItems().get(row-1);
				CheckBox statusCheckBox = (CheckBox)itemsTable.getWidget(row, COL_STATUS);
				statusCheckBox.setEnabled(false);
				boolean status = statusCheckBox.getValue();
				rowItem.setStatus(status?1:0);
				itemsTable.getWidget(row, COL_NAME).setStyleDependentName("disable", status);
				itemsTable.getWidget(row, COL_POSITION).setStyleDependentName("disable", status);
				httpUpdateItem(rowItem);
				statusCheckBox.setEnabled(true);
			}
		});
	}

	// REQUETAGE
	/**
	 * Récupère la liste depuis le service JSON
	 * @param itemListId l'id de la liste à récupérer
	 * @param itemListName le nom de la liste à récupérer pour vérifier que c'est bien la bonne
	 */
	private void httpGetList(String itemListId, final String itemListName) {
		httpGetJson(YourListConst.JSON_URL_ITEMLIST+ itemListId + "/", new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_OK) {
					String itemListAsJSONString = response.getText();
					JSONValue itemListAsJSONValue = JSONParser.parseStrict(itemListAsJSONString);
					JSONObject itemListAsJSONObject = itemListAsJSONValue.isObject();

					itemList = new ItemList();

					// List Id
					itemList.setId((long) itemListAsJSONObject.get("id").isNumber().doubleValue());

					// List Name
					itemList.setName(itemListAsJSONObject.get("name").isString().stringValue());
					if (!itemListName.equals(itemList.getName())){
						// TODO: error Handling ...
						show404("List not found");
						return;
					}

					Window.setTitle(itemList.getName()+" - "+YourListConst.PAGE_TITLE);

					// Items
					setItems(itemListAsJSONObject.get("items").isArray());

					showItemListPanel();
				}else{
					showError("Erreur lors de la récupération de la liste");
					logger.log(Level.SEVERE, response.getStatusCode()+" : "+response.getStatusText());
				}

			}
		});
	}

	/**
	 * Récupère les items de la liste depuis le service JSON
	 * @param itemListId l'id de la liste à récupérer
	 */
	private void httpGetItems() {
		httpGetJson(YourListConst.JSON_URL_ITEMS.replace(YourListConst.JSON_VAR_LISTID, itemList.getId().toString()), new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_OK) {
					String itemsAsJSONString = response.getText();
					JSONValue itemsAsJSONValue = JSONParser.parseStrict(itemsAsJSONString);

					// Items
					setItems(itemsAsJSONValue.isArray());

					showItems();
				}else{
					showError("Erreur lors de la récupération des items");
					logger.log(Level.SEVERE, response.getStatusCode()+" : "+response.getStatusText());
				}
			}
		});
	}

	/**
	 * Ajoute l'item via une requête JSON
	 */
	protected void httpAddItem(Item newItem) {
		newItemNameTextBox.setEnabled(false);
		httpPostJson(newItem.toJson(), YourListConst.JSON_URL_ITEM.replace(YourListConst.JSON_VAR_LISTID, itemList.getId().toString()), new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_CREATED) {
					// Clear the form
					clearAddItemZone();
					newItemNameTextBox.selectAll();
					newItemNameTextBox.setFocus(true);

					// Refresh the view
					httpGetItems();
				}else{
					showError("Erreur lors de l'ajout de l'item");
					logger.log(Level.SEVERE, response.getStatusCode()+" : "+response.getStatusText());
				}
				newItemNameTextBox.setEnabled(true);
			}
		});
	}

	/**
	 * Update l'item via une requête JSON
	 */
	protected void httpUpdateItem(Item item) {

		httpPutJson(item.toJson(), YourListConst.JSON_URL_ITEM.replace(YourListConst.JSON_VAR_LISTID, itemList.getId().toString())+item.getId(), new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_NO_CONTENT) {
					// Clear the form
					clearAddItemZone();

					// Refresh the view
					httpGetItems();
				}else{
					showError("Erreur lors de la mise à jour de l'item");
					logger.log(Level.SEVERE, response.getStatusCode()+" : "+response.getStatusText());

				}
			}
		});
	}

	// GETTERS 

	public String getNewItemName(){
		return newItemNameTextBox.getText();
	}

	public int getNewItemPosition(){
		return newItemPosition;
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

	// UTILS 
	private void addNewItem(){
		if (!itemNameDefaultText.equals(getNewItemName())){
			Item newItem = new Item();
			// Name
			newItem.setName(getNewItemName());

			// Position
			newItem.setPosition(getNewItemPosition());

			// Status
			newItem.setStatus(0);

			httpAddItem(newItem);
		}
	}
}