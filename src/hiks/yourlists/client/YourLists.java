package hiks.yourlists.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import hiks.yourlists.client.view.ItemListCreationPanel;
import hiks.yourlists.client.view.ItemListPanel;
import hiks.yourlists.client.view.Spinner;
import hiks.yourlists.shared.Const;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class YourLists implements EntryPoint, ValueChangeHandler<String> {

	private Spinner spinner;
	private Panel wizardPanel;
	protected Logger logger = Logger.getLogger("YourListsLogger");

	public void onModuleLoad() {
		logger.log(Level.FINEST, "Ouverture de la page...");
		spinner = new Spinner(RootPanel.get("w"));
		spinner.startSpinner();
		Window.setTitle(Const.PAGE_TITLE);
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
		spinner.stopSpinner();
		// On ajoute le panel dans la page
		RootPanel.get("w").clear();
		RootPanel.get("w").add(wizardPanel);
	}
}
