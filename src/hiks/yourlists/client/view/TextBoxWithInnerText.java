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

	public TextBoxWithInnerText (final String defaultText){
		this.setText(defaultText);
		
		// On fait disparaitre le texte quand on clic dans le champ
		this.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TextBox src = (TextBox)event.getSource();
				if (defaultText.equals(src.getText())){
					src.setText("");
				}
			}
		});

		// On fait apparaitre le texte quand on clic hors du champ s'il est vide
		this.addBlurHandler(new BlurHandler(){
			@Override
			public void onBlur(BlurEvent event) {
				TextBox src = (TextBox)event.getSource();
				if ("".equals(src.getText())){
					src.setText(defaultText);
				}
			}
		});
	}
}
