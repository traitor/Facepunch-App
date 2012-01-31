package nl.vertinode.facepunch;

import nl.vertinode.facepunch.FacepunchAPI.FPPost;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;
import android.text.Html;

public class ThreadActivity extends FPActivity {

	private String title = "";
	private int threadId = -1;
	private int page = 1;
	private int pageCount = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threadview);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			threadId = extras.getInt("thread_id");
			page = extras.getInt("page");
		}

		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout postList = (LinearLayout)findViewById(R.id.postList);
		final ImageView loaderImage = (ImageView)inflater.inflate(R.layout.loadingspinner, postList, false);
		applyLoadingIcon(loaderImage);
		postList.addView(loaderImage);
		postList.setGravity(Gravity.CENTER_VERTICAL);

		api.listPosts(threadId, page, new FacepunchAPI.PostCallback() {
			public void onResult(boolean success, FPPost[] posts, String threadTitle, int numPage) {
				postList.removeView(loaderImage);
				postList.setGravity(Gravity.NO_GRAVITY);
				
				title = threadTitle;
				pageCount = numPage;

				if (success)
					populateThread(posts);
				else
					Toast.makeText(ThreadActivity.this, getString(R.string.threadLoadFail), Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void populateThread(FPPost[] posts) {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout postList = (LinearLayout)findViewById(R.id.postList);

		LinearLayout header = (LinearLayout)inflater.inflate(R.layout.listheader, postList, false);
		((TextView)header.findViewById(R.id.headerTitle)).setText(title);
		postList.addView(header);

		// Populate list with results
		for (FPPost post : posts) {
			final RelativeLayout postView = (RelativeLayout)inflater.inflate(R.layout.post, postList, false);
			if (post.getStatus().equals("new"))
				postView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluegradient));//setBackgroundColor(Color.rgb(0xc6,0xde,0xfd));
			postView.getBackground().setDither(true);
			((TextView)postView.findViewById(R.id.usernameText)).setText(post.getAuthor().getName());
			((TextView)postView.findViewById(R.id.joinDateText)).setText(post.getAuthor().getJoinDate());
			StringBuilder sb = new StringBuilder();
			sb.append(post.getAuthor().getPostCount()).append(" ").append(getString(R.string.posts));
			((TextView)postView.findViewById(R.id.postCountText)).setText(sb.toString());
			((TextView)postView.findViewById(R.id.postDate)).setText(post.getDate());
			((TextView)postView.findViewById(R.id.postContent)).setText(Html.fromHtml(post.getMessageHTML(), new ImageGetter(), new TagHandler()));

			api.getAvatar(post.getAuthor().getId(), new FacepunchAPI.AvatarCallback() {
				public void onResult(boolean success, Bitmap avatar) {
					if (avatar == null)
						return;
					//Bitmap resized = Bitmap.createScaledBitmap(avatar, 64, 64, false);
					((ImageView)postView.findViewById(R.id.avatarView)).setImageBitmap(avatar);
				}
			});

			postList.addView(postView);
		}
		
		View view = new View(this);
		view.setId(R.id.listSeparator);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);
		lp.setMargins(0, 3, 0, 0);
		view.setLayoutParams(lp);
		view.setBackgroundColor(Color.rgb(0x77, 0x77, 0x77));
		postList.addView(view);
		
		RelativeLayout changePage = (RelativeLayout)inflater.inflate(R.layout.changepage, postList, false);
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.page)).append(" ").append(page).append("/").append(pageCount);
		((TextView)changePage.findViewById(R.id.pageCount)).setText(sb.toString());
		((TextView)changePage.findViewById(R.id.pageCount)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Allow user to either select page or go to last page
				final CharSequence[] items = { getString(R.string.selectPage), getString(R.string.lastPage) };

				new AlertDialog.Builder(ThreadActivity.this).setTitle(getString(R.string.changePageTitle)).setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 0) { // Select Page
							
						} else if (item == 1) { // Last Page
							Intent intent = new Intent(ThreadActivity.this, ThreadActivity.class);
							intent.putExtra("thread_id", threadId);
							intent.putExtra("page", pageCount);
							startActivity(intent);
						}
					}
				}).create().show();
			}
		});
		
		((Button)changePage.findViewById(R.id.previousPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page <= 1)
					return;
				Intent intent = new Intent(ThreadActivity.this, ThreadActivity.class);
				intent.putExtra("thread_id", threadId);
				intent.putExtra("page", page - 1);
				startActivity(intent);
			}
		});
		((Button)changePage.findViewById(R.id.nextPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page + 1 > pageCount)
					return;
				Intent intent = new Intent(ThreadActivity.this, ThreadActivity.class);
				intent.putExtra("thread_id", threadId);
				intent.putExtra("page", page + 1);
				startActivity(intent);
			}
		});
		postList.addView(changePage);
	}
}
