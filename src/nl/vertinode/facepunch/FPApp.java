package nl.vertinode.facepunch;

import android.app.Application;

public class FPApp extends Application
{
	private final APISession session = new APISession();
	
	public APISession api()
	{
		return session;
	}
}
