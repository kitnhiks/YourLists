package hiks.yourlists.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import hiks.yourlists.client.model.Item;
import hiks.yourlists.client.model.ItemList;
import hiks.yourlists.client.model.Sharer;
import hiks.yourlists.client.controller.PopupCallback;
import hiks.yourlists.shared.Const;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Hidden;

public class ItemListPanel extends WizardPanel{

	private final int COL_ID = 0;
	private final int COL_NAME = 4;
	private final int COL_POSITION = 3;
	private final int COL_STATUS = 2;
	private final int FILTER_UP = 0;
	private final int FILTER_DOWN = 1;

	private int filter = COL_POSITION;
	private int filterOrder = FILTER_DOWN;

	private int newItemPosition;

	private ItemList itemList;

	private Label itemListNameLabel;
	private FlexTable itemsTable;
	private TextBox newItemNameTextBox;

	private final String itemNameDefaultText = "Your item name...";

	public ItemListPanel(String itemListId, String itemListName){
		super();
		this.getElement().setId("itemListPanel");
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
		itemListNameLabel.getElement().setId("itemListNameLabel");
		this.add(itemListNameLabel);

		// La zone pour ajouter un item
		showAddItemZone();

		// Les éléments de la liste
		showItemsTable();

		// Le menu avec les boutons 
		showMenu();
	}

	/**
	 * Affiche le Panel contenant le menu avec les boutons
	 * TODO : sortir dans une classe à part
	 */
	private void showMenu(){
		RootPanel menu = RootPanel.get("m");
		menu.clear();

		FlowPanel shareListButton = new FlowPanel();
		shareListButton.getElement().setId("shareListButton");
		shareListButton.setStylePrimaryName("menu_item");
		shareListButton.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				shareList();
			}
		}, ClickEvent.getType());
		shareListButton.add(new Label("Share"));
		menu.add(shareListButton);

		// Le bouton de refresh de la liste
		FlowPanel refreshListButton = new FlowPanel();
		refreshListButton.getElement().setId("refreshListButton");
		refreshListButton.setStylePrimaryName("menu_item");
		refreshListButton.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				httpGetItems();
			}
		}, ClickEvent.getType());
		refreshListButton.add(new Label("Refresh"));
		menu.add(refreshListButton);

		// Le bouton de retour à la création d'une liste
		FlowPanel backToCreateListButton = new FlowPanel();
		backToCreateListButton.getElement().setId("backToCreateListButton");
		backToCreateListButton.setStylePrimaryName("menu_item");
		backToCreateListButton.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("");
			}
		}, ClickEvent.getType());
		backToCreateListButton.add(new Label("New"));
		menu.add(backToCreateListButton);
	}

	/**
	 * Affiche la zone pour ajouter un item
	 */
	private void showAddItemZone() {
		// Name
		newItemNameTextBox = new TextBoxWithInnerTextSubmit(itemNameDefaultText, true){
			@Override
			protected void onSubmit(){
				addNewItem();
			}
		};
		newItemNameTextBox.getElement().setId("newItemNameTextBox");
		this.add(newItemNameTextBox);
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
		// TODO : Replace FlexTable (GridTable ?) ou carrément des FlowPanel (comment récupérer les items ?)
		itemsTable = new FlexTable();
		itemsTable.setBorderWidth(0);
		itemsTable.setCellPadding(0);
		itemsTable.setCellSpacing(0);
		itemsTable.getElement().setId("itemsTable");
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
		newItemNameTextBox.selectAll();
		newItemNameTextBox.setFocus(true);
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
		PanelWithLabelEditable namePanel = new PanelWithLabelEditable(item.getName(), !status){

			@Override
			public void onSubmit() {
				if ("".equals(this.getText())){
					deleteItem(row);
				}else{
					updateItem(row);
				}
			}

		};
		itemsTable.setWidget(row, COL_NAME, namePanel);
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

		itemsTable.getRowFormatter().addStyleName(row,"itemsTable-row");
		itemsTable.getRowFormatter().getElement(row);
	}

	// REQUETAGE
	/**
	 * Récupère la liste depuis le service JSON
	 * @param itemListId l'id de la liste à récupérer
	 * @param itemListName le nom de la liste à récupérer pour vérifier que c'est bien la bonne
	 */
	private void httpGetList(String itemListId, final String itemListName) {
		httpGetJson(Const.JSON_URL_ITEMLIST+ itemListId + "/", new WizardRequestCallback() {
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

					Window.setTitle(itemList.getName()+" - "+Const.PAGE_TITLE);

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
		httpGetJson(Const.JSON_URL_ITEMS.replace(Const.JSON_VAR_LISTID, itemList.getId().toString()), new WizardRequestCallback() {
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
	private void httpAddItem(Item newItem) {
		newItemNameTextBox.setEnabled(false);
		httpPostJson(newItem.toJson(), Const.JSON_URL_ITEM.replace(Const.JSON_VAR_LISTID, itemList.getId().toString()), new WizardRequestCallback() {
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
	private void httpUpdateItem(Item item) {

		httpPutJson(item.toJson(), Const.JSON_URL_ITEM.replace(Const.JSON_VAR_LISTID, itemList.getId().toString())+item.getId(), new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_NO_CONTENT) {
					// Refresh the view
					httpGetItems();
				}else{
					showError("Erreur lors de la mise à jour de l'item");
					logger.log(Level.SEVERE, response.getStatusCode()+" : "+response.getStatusText());

				}
			}
		});
	}

	/**
	 * Déclenche un partage par mail
	 * @param sharer
	 */
	private void httpShareList(Sharer sharer) {
		httpPostJson(sharer.toJson(), Const.JSON_URL_ITEMLIST+itemList.getId()+"/share", new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_OK) {
					showMessage("Mail envoyé");
				}else{
					showError("Erreur lors de l'envoie du partage");
					logger.log(Level.SEVERE, response.getStatusCode()+" : "+response.getStatusText());

				}
			}
		});

	}


	private void httpDeleteItem(Item item) {
		httpDeleteJson(Const.JSON_URL_ITEM.replace(Const.JSON_VAR_LISTID, itemList.getId().toString())+item.getId(), new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
				if (response.getStatusCode() == Response.SC_NO_CONTENT) {
					// Refresh the view
					httpGetItems();
				}else{
					showError("Erreur lors de la suppression de l'item");
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

	public Long getItemId(int row){
		return Long.valueOf(((Hidden)itemsTable.getWidget(row, COL_ID)).getValue());
	}

	public String getItemName(int row){
		return ((PanelWithLabelEditable)itemsTable.getWidget(row, COL_NAME)).getText();
	}

	public int getItemPosition(int row){
		return Integer.valueOf(((Label)itemsTable.getWidget(row, COL_POSITION)).getText());
	}

	public int getItemStatus(int row){
		return ((CheckBox)itemsTable.getWidget(row, COL_STATUS)).getValue()?1:0;
	}

	// UTILS 
	protected void addNewItem(){
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

	protected void updateItem(int row){
		Item newItem = new Item();
		// Id
		newItem.setId(getItemId(row));

		// Name
		newItem.setName(getItemName(row));

		// Position
		newItem.setPosition(getItemPosition(row));

		// Status
		newItem.setStatus(getItemStatus(row));

		httpUpdateItem(newItem);
	}


	protected void deleteItem(int row) {
		Item item = new Item();
		// Id
		item.setId(getItemId(row));

		httpDeleteItem(item);
	}


	protected void shareList(){
		new PopupShareMail("Share with your friends", new PopupCallback(){
			@Override
			public void handleReturnValue(String returnValue) {
				if ("".equals(returnValue)){
					showError("Aucune adresse renseignée, aucun mail n'a été envoyé");
				}else{
					String[] mailsTable = returnValue.split(",");
					int nbMails = mailsTable.length;
					ArrayList<String> mails = new ArrayList<String>();
					for (int i = 0; i<nbMails; i++){
						mails.add(mailsTable[i].trim());
					}
					Sharer sharer = new Sharer ();
					sharer.setSubject(Const.SHARE_MAIL_SUBJECT.replace(Const.SHARE_VAR_LIST_NAME, itemList.getName()));
					sharer.setBody(Const.SHARE_MAIL_BODY);
					sharer.setMails(mails);
					sharer.setUrl(Window.Location.getHref());// TODO : peut être remplacer par une chaine en dur ?
					httpShareList(sharer);
				}
			}
		});
	}
}