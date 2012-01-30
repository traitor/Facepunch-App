package nl.vertinode.facepunch;

import android.app.Application;

public class FPApp extends Application {
	private final FacepunchAPI session = new FacepunchAPI();

	public FacepunchAPI api() {
		return session;
	}
}
