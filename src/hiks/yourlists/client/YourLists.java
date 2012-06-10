package hiks.yourlists.client;

import org.mortbay.util.UrlEncoded;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import hiks.yourlists.client.view.ItemListCreationPanel;
import hiks.yourlists.client.view.ItemListPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class YourLists implements EntryPoint, ValueChangeHandler<String> {

	private VerticalPanel wizardPanel;

	public void onModuleLoad() {
		// Add history listener
		History.addValueChangeHandler(this);
		History.fireCurrentHistoryState();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String history = event.getValue();
		String[] historyTokens = history.split("-");
		int nbTokens = historyTokens.length;

		if (nbTokens>1){
			//Affiche le Panel pour le chargement d'une liste
			wizardPanel = new ItemListPanel(historyTokens[nbTokens-1], URL.decode(history.substring(0, history.length()-historyTokens[nbTokens-1].length()-1)));
		}else{
			//Affiche le Panel pour la création d'une liste
			wizardPanel = new ItemListCreationPanel();
		}
		// On vire le loader
		if (DOM.getElementById("loading")!=null){ 
			// TODO : gérer un loader générique qui soit accessible par tous les controleurs
			DOM.removeChild(DOM.getElementById("content"), DOM.getElementById("loading"));
		}
		// On ajoute le panel dans la page
		RootPanel.get("wizard").clear();
		RootPanel.get("wizard").add(wizardPanel);
	}
}
