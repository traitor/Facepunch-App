package nl.vertinode.facepunch;

import nl.vertinode.facepunch.APISession.Category;
import nl.vertinode.facepunch.APISession.FPThread;
import nl.vertinode.facepunch.APISession.Forum;
import nl.vertinode.testing.facepunch.R;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ForumActivity extends FPActivity {
	
	// Used for restoring list
	private class State
	{
		Category[] categories;
		int scrollY;
	}
	
	//private Forum forum;
	private int forumId = 6;
	private FPThread[] displayedThreads;
	
	/*public ForumActivity(Forum forum)
	{
		this.forum = forum;
	}*/
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		// Load layout
		super.onCreate( savedInstanceState );
		setContentView( R.layout.forumview );

		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			forumId = extras.getInt("forum_id");
		}
		
		// Restore thread list
		final State data = (State)getLastNonConfigurationInstance();
		if ( data != null )
		{
			/*populateList( data.categories );
			
			final ScrollView scroller = (ScrollView)findViewById( R.id.scroller );
			scroller.post( new Runnable()
			{
				public void run()
				{
					scroller.scrollTo( 0, data.scrollY );
				}
			} );*/
			
			return;
		}
		
		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		final LinearLayout threadList = (LinearLayout)findViewById( R.id.threadList );
		final ImageView loaderImage = (ImageView)inflater.inflate( R.layout.loadingspinner, threadList, false );
		applyLoadingIcon( loaderImage );
		threadList.addView( loaderImage );
		threadList.setGravity( Gravity.CENTER_VERTICAL );
		
		api.listThreads(forumId, 1, new APISession.ThreadCallback() {
			public void onResult(boolean success, FPThread[] threads) {
				// Remove loading spinner
				threadList.removeView( loaderImage );
				threadList.setGravity( Gravity.NO_GRAVITY );
				
				if ( success ) {
					populateThreads( threads );
				} else {
					Toast.makeText( ForumActivity.this, getString( R.string.forumLoadingFail ), Toast.LENGTH_SHORT ).show();
				}
			}
		} );
	}
	
	public void populateThreads( FPThread[] threads )
	{
		displayedThreads = threads;
		
		LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		LinearLayout forumList = (LinearLayout)findViewById( R.id.threadList );
		
		// Populate list with results
		for ( FPThread thread : threads )
		{
			// Create thread view
			LinearLayout threadView = (LinearLayout)inflater.inflate( R.layout.listthread, forumList, false );
			if (thread.isSticky())
				threadView.setBackgroundColor(Color.YELLOW);
			( (TextView)threadView.findViewById( R.id.threadTitle ) ).setText( thread.getTitle() );
			
			StringBuilder sb = new StringBuilder();
			sb.append(thread.getAuthor().getName()).append(" • ").append(thread.postCount()).append(" replies");
			if (thread.readerCount() > 0)
				sb.append(" • ").append(thread.readerCount()).append(" viewing");
			( (TextView)threadView.findViewById( R.id.threadInfo ) ).setText( sb.toString() );
			
			threadView.setOnClickListener(new OnClickListener()
				{
					public void onClick( View v )
					{
						//open thread n stuff
					}
			});
			
			forumList.addView( threadView );
		}
	}
}
