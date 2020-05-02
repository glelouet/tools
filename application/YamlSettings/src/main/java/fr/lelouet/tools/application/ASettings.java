package fr.lelouet.tools.application;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * code to add to a {@link ISettings} to make it correct.
 */
public abstract class ASettings implements ISettings {

	private transient PauseTransition storeLaterTransition = new PauseTransition(Duration.seconds(1));

	@Override
	public void storeLater() {
		synchronized (storeLaterTransition) {
			storeLaterTransition.setOnFinished(event -> store());
			storeLaterTransition.playFromStart();
		}
	}

}
