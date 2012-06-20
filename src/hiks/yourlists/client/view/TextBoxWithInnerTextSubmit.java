package hiks.yourlists.client.view;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

/**
 * Une classe pour faire des champs texte avec un texte par défaut qui disparait quand on click
 * La méthode onSubmit se déclenche au retour chariot
 * @author xavier.renaudin
 *
 */
public abstract class TextBoxWithInnerTextSubmit extends TextBoxWithInnerText{

	protected boolean onFocus;
	
	/**
	 * Crée un champ texte avec un texte par défaut qui disparait quand on click
	 * et qui déclenche la méthode onSubmit au retour chariot
	 * @param defaultText
	 * @param onFocus true si on déclenche aussi la méthode onSubmit à la perte de focus
	 */
	public TextBoxWithInnerTextSubmit (final String defaultText, final boolean onFocus){
		super(defaultText);
		this.onFocus = onFocus;

		// Lance onSubmit quand on appuie sur Enter
		this.addKeyDownHandler(new KeyDownHandler(){
			@Override
			public void onKeyDown(KeyDownEvent event) {
				handleKeyDown(event);
			}
		});
	}
	
	protected void handleKeyDown(KeyDownEvent event){
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
			if (!defaultText.equals(this.getText())){
				handleSubmit();
			}
		}
	}

	protected void handleSubmit(){
		onSubmit();
	}
	
	@Override
	protected void handleOnBlur(){
		super.handleOnBlur();
		if (onFocus && !"".equals(this.getText()) && !defaultText.equals(this.getText())){
			handleSubmit();
		}
	}
	
	protected abstract void onSubmit();
}
