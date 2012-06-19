package hiks.yourlists.client.controller;

import com.google.gwt.user.client.Timer;

/**
 * Permet de lancer une commande de façon répétée sans faire planter le navigateur 
 * @author xavier.renaudin
 *
 */
public abstract class BackgroundRepeater extends Timer {

	private BackgroundRepeater () {
		this(5000);
	}
	
	private BackgroundRepeater (int milliPeriod) {
		super();
		this.scheduleRepeating(milliPeriod);
	}
}