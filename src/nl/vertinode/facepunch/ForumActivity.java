package nl.vertinode.facepunch;

import nl.vertinode.facepunch.APISession.Category;
import nl.vertinode.facepunch.APISession.FPPost;
import nl.vertinode.facepunch.APISession.FPThread;
import nl.vertinode.facepunch.APISession.Forum;
import nl.vertinode.facepunch.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
	private int page = 1;
	private String forumName = "";
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
			forumName = extras.getString("forum_name");
			if (extras.containsKey("page"))
				page = extras.getInt("page");
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
		
		api.listThreads(forumId, page, new APISession.ThreadCallback() {
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
		LinearLayout threadList = (LinearLayout)findViewById( R.id.threadList );
		
		LinearLayout header = (LinearLayout)inflater.inflate( R.layout.listheader, threadList, false );
		( (TextView)header.findViewById( R.id.headerTitle ) ).setText( forumName );
		threadList.addView( header );
		
		// Populate list with results
		for ( FPThread thread : threads )
		{
			if (thread == null || thread.getAuthor() == null)
				continue;
			
			// Create thread view
			LinearLayout threadView = (LinearLayout)inflater.inflate( R.layout.listthread, threadList, false );
			threadView.setTag(thread);
			if (thread.isSticky())
				threadView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xaa));
			( (TextView)threadView.findViewById( R.id.threadTitle ) ).setText( thread.getTitle() );
			
			StringBuilder sb = new StringBuilder();
			sb.append(thread.getAuthor().getName()).append(" • ").append(thread.postCount()).append((thread.postCount() == 1 ? " reply" : " replies"));
			if (thread.readerCount() > 0)
				sb.append(" • ").append(thread.readerCount()).append(" viewing");
			( (TextView)threadView.findViewById( R.id.threadInfo ) ).setText( sb.toString() );
			
			threadView.setOnClickListener(new OnClickListener()
				{
					public void onClick( View v )
					{
						FPThread thread = (FPThread)v.getTag();
						
						Intent intent = new Intent(ForumActivity.this, ThreadActivity.class);
						intent.putExtra("thread_id", thread.getId());
						startActivity(intent);
					}
			});
			
			threadList.addView( threadView );
		}
		RelativeLayout changePage = (RelativeLayout)inflater.inflate(R.layout.changepage, threadList, false);
		StringBuilder sb = new StringBuilder();
		sb.append("Page ").append(page);
		((TextView)changePage.findViewById(R.id.pageCount)).setText(sb.toString());
		((Button)changePage.findViewById(R.id.previousPage)).setOnClickListener(new OnClickListener()
		{
			public void onClick( View v )
			{
				if (page <= 1)
					return;
				Intent intent = new Intent(ForumActivity.this, ForumActivity.class);
				intent.putExtra("forum_id", forumId);
				intent.putExtra("forum_name", forumName);
				intent.putExtra("page", page - 1);
				startActivity(intent);
			}
		});
		((Button)changePage.findViewById(R.id.nextPage)).setOnClickListener(new OnClickListener()
		{
			public void onClick( View v )
			{
				Intent intent = new Intent(ForumActivity.this, ForumActivity.class);
				intent.putExtra("forum_id", forumId);
				intent.putExtra("forum_name", forumName);
				intent.putExtra("page", page + 1);
				startActivity(intent);
			}
		});
		threadList.addView(changePage);
	}
}
