package hiks.yourlists.client.view;

import java.util.logging.Level;

import hiks.yourlists.client.model.ItemList;
import hiks.yourlists.shared.Const;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class ItemListCreationPanel extends WizardPanel{

	private final String itemListNameDefaultText = "Your list name...";
	private TextBox itemListNameTextBox;
	private Label errorMessage;
	private ButtonWithWait createListButton;

	public ItemListCreationPanel(){
		super();
		logger.log(Level.FINEST, "Chargement du panel de création...");
		Window.setTitle(Const.PAGE_TITLE);
		this.getElement().setId("itemListCreationPanel");
		showItemListCreationPanel();
		logger.log(Level.FINEST, "Panel de création chargé");
	}
	
	// AFFICHAGE
	public void showItemListCreationPanel(){
		
		// Le champ du nom de la liste
		FlowPanel itemListNameTextBoxPanel = new FlowPanel();
		itemListNameTextBox = new TextBoxWithInnerTextSubmit(itemListNameDefaultText, false){
			@Override
			protected void onSubmit(){
				addNewList();
			}
		};
		itemListNameTextBox.getElement().setId("itemListNameTextBox");
		itemListNameTextBoxPanel.add(itemListNameTextBox);
		itemListNameTextBoxPanel.getElement().setId("itemListNameTextBoxPanel");
		this.add(itemListNameTextBoxPanel);

		// Le bouton de création de la liste
		FlowPanel createListButtonPanel = new FlowPanel();
		createListButton = new ButtonWithWait("Create");
		createListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addNewList();
			}
		});
		createListButton.getElement().setId("createListButton");
		createListButtonPanel.add(createListButton);
		createListButtonPanel.getElement().setId("createListButtonPanel");
		this.add(createListButtonPanel);
	}
	
	@Override
	protected void showError(String message){
		super.showError(message);
		itemListNameTextBox.setEnabled(true);
	}
	
	// REQUETAGE

	protected void httpCreateListe() {
		itemListNameTextBox.setEnabled(false);
		ItemList itemList = new ItemList();
		itemList.setName(itemListNameTextBox.getText());
		httpPostJson(itemList.toJson(), Const.JSON_URL_ITEMLIST, new WizardRequestCallback() {
			@Override
			public void showResponse(Response response) {
					if (response.getStatusCode() == Response.SC_CREATED) {
						// retrieve the id from the created url
						String[] urlToken = response.getHeader("Location").split("/");
						String listId = urlToken[urlToken.length-1];
						History.newItem(URL.encode(itemListNameTextBox.getText())+"-"+listId);
					}else{
						showError("Erreur lors de la création de la liste");
						logger.log(Level.SEVERE, "[ERROR] "+response.getStatusCode()+" : "+response.getStatusText());
					}
					itemListNameTextBox.setEnabled(true);
					createListButton.stopWaiting(); // TODO : replace par spinner
				}
			});
	}

	// UTILS

	private void addNewList(){
		itemListNameTextBox.removeStyleName("error");
		itemListNameTextBox.setEnabled(false);
		createListButton.setEnabled(false);
		if (!itemListNameDefaultText.equals(itemListNameTextBox.getText()) 
				&& !"".equals(itemListNameTextBox.getText()) ){
			httpCreateListe();
		}else{
			itemListNameTextBox.addStyleName("error");
		}
	}
}
