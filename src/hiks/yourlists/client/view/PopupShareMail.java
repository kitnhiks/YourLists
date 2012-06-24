package hiks.yourlists.client.view;

import hiks.yourlists.client.controller.PopupCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class PopupShareMail extends PopupPanel{
	
	TextAreaWithInnerText mailsTextArea;
	PopupCallback callback;
	
	String mailsDefaultText = "Enter e-mail adresses separated by a coma ','";
	
	public PopupShareMail(String title, PopupCallback callback){
		super(true, true);
		this.callback = callback;
		this.addStyleName("sharePopup");
		
		// Un seul panel car les PopupPanel ne peuvent contenir qu'un Widget
		FlowPanel popupContent = new FlowPanel();
		
		// La zone de texte pour les mails 
		// Elle est contenue dans un Panel pour prendre toute la place sans dépasser
		FlowPanel mailsTextAreaContainer = new FlowPanel();
		mailsTextArea = new TextAreaWithInnerText (mailsDefaultText);
		mailsTextArea.addStyleName("mailsTextArea");
		mailsTextAreaContainer.add(mailsTextArea);
		popupContent.add(mailsTextAreaContainer);
		
		// Le bouton qui lance le partage
		Button shareButton = new Button("Share");
		shareButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleClick(event);
			}
		});
		popupContent.add(shareButton);
		
		this.add(popupContent);
		
		this.setTitle(title);
		
		this.center();
	}
	
	protected void handleClick(ClickEvent event) {
		String returnValue="";
		if (!mailsDefaultText.equals(mailsTextArea.getText())){
			returnValue = mailsTextArea.getText();
		}
		this.hide();
		callback.handleReturnValue(returnValue);
	}
}