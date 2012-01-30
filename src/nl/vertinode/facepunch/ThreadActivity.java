package nl.vertinode.facepunch;

import nl.vertinode.facepunch.FacepunchAPI.FPPost;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

	private String threadTitle = "";
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
			threadTitle = extras.getString("thread_title");
			threadId = extras.getInt("thread_id");
			page = extras.getInt("page");
			pageCount = extras.getInt("page_count");
		}

		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout postList = (LinearLayout)findViewById(R.id.postList);
		final ImageView loaderImage = (ImageView)inflater.inflate(R.layout.loadingspinner, postList, false);
		applyLoadingIcon(loaderImage);
		postList.addView(loaderImage);
		postList.setGravity(Gravity.CENTER_VERTICAL);

		api.listPosts(threadId, 1, new FacepunchAPI.PostCallback() {

			public void onResult(boolean success, FPPost[] posts) {
				postList.removeView(loaderImage);
				postList.setGravity(Gravity.NO_GRAVITY);

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
		((TextView)header.findViewById(R.id.headerTitle)).setText(threadTitle);
		postList.addView(header);

		// Populate list with results
		for (FPPost post : posts) {
			final RelativeLayout postView = (RelativeLayout)inflater.inflate(R.layout.post, postList, false);
			((TextView)postView.findViewById(R.id.usernameText)).setText(post.getAuthor().getName());

			StringBuilder sb = new StringBuilder();
			sb.append(post.getAuthor().getJoinMonth()).append(" ").append(post.getAuthor().getJoinYear());

			((TextView)postView.findViewById(R.id.joinDateText)).setText(sb.toString());
			sb = new StringBuilder();
			sb.append(post.getAuthor().getPostCount()).append(" ").append(getString(R.string.posts));
			((TextView)postView.findViewById(R.id.postCountText)).setText(sb.toString());
			((TextView)postView.findViewById(R.id.postContent)).setText(Html.fromHtml(post.getMessageHTML(), new ImageGetter(), new TagHandler()));

			api.getAvatar(post.getAuthor().getId(), new FacepunchAPI.AvatarCallback() {
				public void onResult(boolean success, Bitmap avatar) {
					if (avatar == null)
						return;
					Bitmap resized = Bitmap.createScaledBitmap(avatar, 60, 60, false);
					((ImageView)postView.findViewById(R.id.avatarView)).setImageBitmap(resized);
				}
			});

			postList.addView(postView);
		}
		RelativeLayout changePage = (RelativeLayout)inflater.inflate(R.layout.changepage, postList, false);
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.page)).append(" ").append(page).append("/").append(pageCount);
		((TextView)changePage.findViewById(R.id.pageCount)).setText(sb.toString());
		((Button)changePage.findViewById(R.id.previousPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page <= 1)
					return;
				Intent intent = new Intent(ThreadActivity.this, ThreadActivity.class);
				intent.putExtra("thread_id", threadId);
				intent.putExtra("thread_title", threadTitle);
				intent.putExtra("page", page - 1);
				intent.putExtra("page_count", pageCount);
				startActivity(intent);
			}
		});
		((Button)changePage.findViewById(R.id.nextPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page + 1 > pageCount)
					return;
				Intent intent = new Intent(ThreadActivity.this, ThreadActivity.class);
				intent.putExtra("thread_id", threadId);
				intent.putExtra("thread_title", threadTitle);
				intent.putExtra("page", page + 1);
				intent.putExtra("page_count", pageCount);
				startActivity(intent);
			}
		});
		postList.addView(changePage);
	}
}
