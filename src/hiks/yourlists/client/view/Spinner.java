package hiks.yourlists.client.view;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Spinner {

	protected Logger logger = Logger.getLogger("YourListsLogger");
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
		if (Document.get().getElementById("spinner") == null){
			RootPanel.get("wizard").add(layer);
		}
	}

	public void stopSpinner() {
		RootPanel.get("wizard").remove(layer);
	}
}
