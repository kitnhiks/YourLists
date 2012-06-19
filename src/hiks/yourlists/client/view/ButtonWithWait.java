package hiks.yourlists.client.view;

import com.google.gwt.user.client.ui.Button;

public class ButtonWithWait extends Button{

	private Spinner spinner;
	
	public ButtonWithWait(){
		this("");
	}
	
	public ButtonWithWait(String html){
		super(html);
		spinner = new Spinner(this);
		
	}
	
	public void startWaiting(){
		spinner.startSpinner();
	}
	
	public void stopWaiting(){
		spinner.stopSpinner();
	}
}
