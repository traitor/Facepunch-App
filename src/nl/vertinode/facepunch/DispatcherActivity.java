package nl.vertinode.facepunch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class DispatcherActivity extends Activity
{
	@Override
    protected void onCreate( Bundle savedInstanceState )
	{
        super.onCreate( savedInstanceState );
        
        // Find last viewed activity and re-open it
        Class<?> activityClass;

        try
        {
            SharedPreferences prefs = getPreferences( MODE_PRIVATE );
            activityClass = Class.forName( prefs.getString( "lastActivity", LoginActivity.class.getName() ) );
        } catch( ClassNotFoundException e ) {
            activityClass = LoginActivity.class;
        }

        startActivity( new Intent( this, activityClass ) );
    }
}