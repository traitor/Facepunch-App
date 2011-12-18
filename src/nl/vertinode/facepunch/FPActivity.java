package nl.vertinode.facepunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * This is a base class for activites belonging to this application.
 * It contains the header and some additional utility code.
 * 
 * @author Overv
 */
public class FPActivity extends Activity
{
	protected APISession api = null;
	
	// Inheriting activities can prevent the user button from appearing, e.g. in the login activity
	protected boolean suppressUserButton = false;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		// Retrieve global API handle
		api = ( (FPApp)getApplicationContext() ).api();
	}
	
	@Override
	public void onContentChanged()
	{
		// Set up user button if it isn't suppressed and there is a user logged in
		if ( !suppressUserButton && api.loggedIn() )
		{
			Button userButton = (Button)findViewById( R.id.userButton );
			
			userButton.setVisibility( View.VISIBLE );
			userButton.setText( api.username() );
			
			userButton.setOnClickListener( new Button.OnClickListener()
			{
				public void onClick( View v )
				{
					// Show list of user actions
					final CharSequence[] items = { "Log out" };

					new AlertDialog.Builder( FPActivity.this )
						.setTitle( api.username() )
						.setItems( items, new DialogInterface.OnClickListener()
						{
						    public void onClick( DialogInterface dialog, int item )
						    {
						    	// Log out
						        if ( item == 0 )
						        {
						        	startActivity( new Intent( FPActivity.this, LoginActivity.class ) );
						        }
						    }
						} )
					.create().show();
				}
			} );
		}
	}
	
	// Show animated loading icon in ImageView
	protected void applyLoadingIcon( final ImageView iv )
	{
		iv.setImageResource( R.anim.loadingspinner );				
		final AnimationDrawable loadingAnimation = (AnimationDrawable)iv.getDrawable();
		iv.post( new Runnable()
		{
			public void run()
			{
				loadingAnimation.start();
			}
		} );
	}
}
