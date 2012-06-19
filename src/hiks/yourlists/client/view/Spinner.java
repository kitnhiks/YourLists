package hiks.yourlists.client.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Spinner {

	protected Logger logger = Logger.getLogger("YourListsLogger");
	// TODO : test
	private Image spinner;
	private Widget element;
	private FlowPanel layer;

	public Spinner(Widget element){
		this.element = element;
		spinner = new Image("img/loading.gif");
		spinner.setAltText("Loading...");
		layer = new FlowPanel();
		layer.getElement().setId("spinner");
	}

	public void startSpinner() {
		logger.log(Level.FINEST, "Demande un Spinner");
		if (Document.get().getElementById("spinner") == null){
			logger.log(Level.FINEST, "Ajoute un Spinner");
			RootPanel.get("wizard").add(layer);
		}
	}

	public void stopSpinner() {
		logger.log(Level.FINEST, "Supprimer un Spinner");
		RootPanel.get("wizard").remove(layer);
	}
}
