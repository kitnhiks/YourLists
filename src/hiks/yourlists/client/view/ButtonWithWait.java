package hiks.yourlists.client.view;

import com.google.gwt.user.client.ui.Button;

public class ButtonWithWait extends Button{

	private String savedText;
	
	public ButtonWithWait(){
		super();
	}
	
	public ButtonWithWait(String html){
		super(html);
	}
	
	public void startWaiting(){
		savedText = this.getText();
		this.setText("");
		this.setEnabled(false);
		this.addStyleName("wait");
	}
	
	public void stopWaiting(){
		savedText = null;
		this.setText(savedText);
		this.setEnabled(true);
		this.removeStyleName("wait");
	}
}
