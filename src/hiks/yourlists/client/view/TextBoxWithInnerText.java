package hiks.yourlists.client.view;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Une classe pour faire des champs texte avec un texte par défaut qui disparait quand on click
 * @author xavier.renaudin
 *
 */
public class TextBoxWithInnerText extends TextBox{

	protected String defaultText;
	
	/**
	 * Crée un champ texte avec un texte par défaut qui disparait quand on click
	 * @param defaultText
	 */
	public TextBoxWithInnerText (final String defaultText){
		super();
		this.defaultText = defaultText;
		
		// S'il n'y a pas de texte par défaut, pas la peine d'avoir les handler
		if (!"".equals(defaultText)){
			this.setText(defaultText);
			
			// On fait disparaitre le texte quand on clic dans le champ
			this.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleOnClick();
				}
			});
	
			// On fait apparaitre le texte quand on clic hors du champ s'il est vide
			this.addBlurHandler(new BlurHandler(){
				@Override
				public void onBlur(BlurEvent event) {
					handleOnBlur();
				}
			});
		}
	}
	
	protected void handleOnClick(){
		if (defaultText.equals(this.getText())){
			this.setText("");
		}
	}
	
	protected void handleOnBlur(){
		if ("".equals(this.getText())){
			this.setText(defaultText);
		}
	}
}
