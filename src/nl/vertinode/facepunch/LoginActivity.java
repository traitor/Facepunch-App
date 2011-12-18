package nl.vertinode.facepunch;

import nl.vertinode.facepunch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity
{	
	// Used for restoring form input
	private class State
	{
		public String username;
		public String password;
	}
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		// Load layout
		super.onCreate( savedInstanceState );
		setContentView( R.layout.login );
		
		// Views
		final Button loginButton = (Button)findViewById( R.id.loginButton );
		final EditText usernameField = (EditText)findViewById( R.id.usernameField );
		final EditText passwordField = (EditText)findViewById( R.id.passwordField );
		
		// Get preference access
		final SharedPreferences prefs = getPreferences( MODE_PRIVATE );
		
		// Log in request handler
		loginButton.setOnClickListener( new OnClickListener()
		{
			public void onClick( View v )
			{
				final String username = usernameField.getText().toString().trim();
				final String password = passwordField.getText().toString().trim();
				
				if ( username.length() == 0 && password.length() == 0 ) return;
				
				final ProgressDialog loginDialog = ProgressDialog.show( LoginActivity.this, "", getString( R.string.loggingIn ), true );
				loginDialog.show();
				
				// Attempt logging in
				( (FPApp)getApplicationContext() ).api().login( username, password, new APISession.LoginCallback()
				{
					public void onResult( boolean success )
					{
						loginDialog.dismiss();
						
						// Prepare frontpage opening intent
						final Intent frontpageIntent = new Intent( LoginActivity.this, FrontpageActivity.class );
						
						if ( success )
						{
							// Ask the user if the successful login details should be saved
							if ( !username.equals( prefs.getString( "username", "" ) ) || !password.equals( prefs.getString( "password", "" ) ) )
							{
								new AlertDialog.Builder( LoginActivity.this )
									.setMessage( getString( R.string.successfulLoginRememberPassword ) )
									.setCancelable( true )
									.setPositiveButton( getString( R.string.rememberPassword ), new DialogInterface.OnClickListener()
									{
										public void onClick( DialogInterface dialog, int id )
										{
											SharedPreferences.Editor editor = prefs.edit();
											editor.putString( "username", username );
											editor.putString( "password", password );
											editor.commit();
											
											startActivity( frontpageIntent );
										}
									} )
									.setNegativeButton( getString( R.string.forgetPassword ), new DialogInterface.OnClickListener()
									{
										public void onClick( DialogInterface dialog, int id )
										{
											startActivity( frontpageIntent );
										}
									} )
									.create()
								.show();
							} else {
								startActivity( frontpageIntent );
							}
						} else {
							// Show failed login message
							new AlertDialog.Builder( LoginActivity.this )
								.setMessage( getString( R.string.failedLogin ) )
								.setCancelable( true )
								.setPositiveButton( "Ok", new DialogInterface.OnClickListener() { public void onClick( DialogInterface dialog, int which ) {} } )
								.create()
							.show();
						}
					}
				} );
			}
		} );
		
		// Restore form input
		State data = (State)getLastNonConfigurationInstance();
		if ( data != null )
		{
			usernameField.setText( data.username );
			passwordField.setText( data.password );
		} else {
			// Attempt to retrieve stored login details
			usernameField.setText( prefs.getString( "username", "" ) );
			passwordField.setText( prefs.getString( "password", "" ) );
		}
	}
	
	// Save activity state
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		final State data = new State();
		
		data.username = ( (EditText)findViewById( R.id.usernameField ) ).getText().toString().trim();
		data.password = ( (EditText)findViewById( R.id.passwordField ) ).getText().toString().trim();
		
		return data;
	}
}