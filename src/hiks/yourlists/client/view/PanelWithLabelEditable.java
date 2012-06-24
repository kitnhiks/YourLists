package hiks.yourlists.client.view;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Une classe pour faire des label qu'on peut éditer quand on clique dessus
 * @author xavier.renaudin
 *
 */
public abstract class PanelWithLabelEditable extends FlowPanel{
	TextBox textBox;
	Label label;
	boolean isEnabled = false;
	HandlerRegistration labelHR;
	
	public PanelWithLabelEditable(String defaultText){
		this(defaultText, true);
	}
	
	public PanelWithLabelEditable(String defaultText, boolean isEnabled){
		super();
		label = new Label(defaultText);
		this.add(label);
		if (isEnabled){
			setEnable(true);
		}
		this.isEnabled = isEnabled;
	}
	
	public void setEnable(boolean enabled) {
		if (enabled && !isEnabled){
			labelHR = label.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(ClickEvent event) {
					handleClick(event);
				}
			});

			textBox = new TextBox();
			textBox.addBlurHandler(new BlurHandler(){
				@Override
				public void onBlur(BlurEvent event) {
					handleOnBlur(event);
				}
			});
			textBox.addKeyDownHandler(new KeyDownHandler(){
				@Override
				public void onKeyDown(KeyDownEvent event) {
					handleKeyDown(event);
				}
			});
			textBox.setVisible(false);
			this.add(textBox);
		}else if (!enabled && isEnabled){
			labelHR.removeHandler();
			this.remove(textBox);
		}
	}

	private void handleClick(ClickEvent event){
		textBox.setText(label.getText());
		textBox.setVisible(true);
		label.setVisible(false);
		textBox.setFocus(true);
	}
	private void handleKeyDown(KeyDownEvent event){
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
			handleSubmit();
		}
	}

	private void handleOnBlur(BlurEvent event){
		handleSubmit();
	}
	
	private void handleSubmit(){
		if (!label.getText().equals(textBox.getText())){
			label.setText(textBox.getText());
			onSubmit();
		}
		textBox.setVisible(false);
		label.setVisible(true);
	}
	
	public abstract void onSubmit();

	public String getText() {
		if (textBox.isVisible()){
			return textBox.getText();
		}else{
			return label.getText();
		}
	}
}
