package nl.vertinode.facepunch;

import nl.vertinode.facepunch.APISession.FPPost;
import nl.vertinode.facepunch.APISession.FPThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ThreadActivity extends FPActivity {
	
	private int threadId = -1;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		// Load layout
		super.onCreate( savedInstanceState );
		setContentView( R.layout.threadview );
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			threadId = extras.getInt("thread_id");
		}
		
		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		final LinearLayout postList = (LinearLayout)findViewById( R.id.postList );
		final ImageView loaderImage = (ImageView)inflater.inflate( R.layout.loadingspinner, postList, false );
		applyLoadingIcon( loaderImage );
		postList.addView( loaderImage );
		postList.setGravity( Gravity.CENTER_VERTICAL );
		
		api.listPosts(threadId, 1, new APISession.PostCallback() {
			
			public void onResult(boolean success, FPPost[] posts) {
				postList.removeView( loaderImage );
				postList.setGravity( Gravity.NO_GRAVITY );
				
				if (success)
					populateThread(posts);
				else
					Toast.makeText( ThreadActivity.this, getString( R.string.forumLoadingFail ), Toast.LENGTH_SHORT ).show();
			}
		});
	}
	
	public void populateThread(FPPost[] posts) {
		LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		LinearLayout postList = (LinearLayout)findViewById( R.id.postList );
		
		LinearLayout header = (LinearLayout)inflater.inflate( R.layout.listheader, postList, false );
		( (TextView)header.findViewById( R.id.headerTitle ) ).setText( "No thread title yet" );
		postList.addView( header );
		
		// Populate list with results
		for ( FPPost post : posts )
		{
			final RelativeLayout postView = (RelativeLayout)inflater.inflate(R.layout.post, postList, false);
			((TextView)postView.findViewById( R.id.usernameText ) ).setText( post.getAuthor().getName());
			
			StringBuilder sb = new StringBuilder();
			sb.append(post.getAuthor().getJoinMonth()).append(" ").append(post.getAuthor().getJoinYear());
			
			((TextView)postView.findViewById( R.id.joinDateText ) ).setText(sb.toString());
			sb = new StringBuilder();
			sb.append(post.getAuthor().getPostCount()).append(" posts");
			((TextView)postView.findViewById( R.id.postCountText ) ).setText(sb.toString());
			((TextView)postView.findViewById( R.id.postContent ) ).setText(post.getMessageHTML());
			
			api.getAvatar(post.getAuthor().getId(), new APISession.AvatarCallback() {
				public void onResult(boolean success, Bitmap avatar) {
					if (avatar == null)
						return;
					Bitmap resized = Bitmap.createScaledBitmap(avatar, 60, 60, false);
					((ImageView)postView.findViewById( R.id.avatarView ) ).setImageBitmap(resized);
				}
			});
			
			postList.addView( postView );
		}
	}
}
