package nl.vertinode.facepunch;

import nl.vertinode.facepunch.FacepunchAPI.FPThread;
import nl.vertinode.facepunch.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

public class ForumActivity extends FPActivity {
	private int forumId = 6;
	private int page = 1;
	private int numPage = 1;
	private String forumName = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forumview);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			forumId = extras.getInt("forum_id");
			forumName = extras.getString("forum_name");
			if (extras.containsKey("page"))
				page = extras.getInt("page");
		}

		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout threadList = (LinearLayout)findViewById(R.id.threadList);
		final ImageView loaderImage = (ImageView)inflater.inflate(R.layout.loadingspinner, threadList, false);
		applyLoadingIcon(loaderImage);
		threadList.addView(loaderImage);
		threadList.setGravity(Gravity.CENTER_VERTICAL);

		api.listThreads(forumId, page, new FacepunchAPI.ThreadCallback() {
			public void onResult(boolean success, FPThread[] threads, int pageCount) {
				// Remove loading spinner
				threadList.removeView(loaderImage);
				threadList.setGravity(Gravity.NO_GRAVITY);
				numPage = pageCount;

				if (success) {
					populateThreads(threads);
				} else {
					Toast.makeText(ForumActivity.this, getString(R.string.forumLoadingFail), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void populateThreads(FPThread[] threads) {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout threadList = (LinearLayout)findViewById(R.id.threadList);

		LinearLayout header = (LinearLayout)inflater.inflate(R.layout.listheader, threadList, false);
		((TextView)header.findViewById(R.id.headerTitle)).setText(forumName);
		threadList.addView(header);

		// Populate list with results
		for (FPThread thread : threads) {
			if (thread == null || thread.getAuthor() == null)
				continue;

			// Create thread view
			RelativeLayout threadView = (RelativeLayout)inflater.inflate(R.layout.listthread, threadList, false);
			threadView.setTag(thread);
			if (thread.isSticky())
				threadView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xaa));
			else if (thread.isLocked())
				threadView.setBackgroundColor(Color.rgb(0xee, 0xee, 0xee));
			((TextView)threadView.findViewById(R.id.threadTitle)).setText(thread.getTitle());

			StringBuilder sb = new StringBuilder();
			sb.append(thread.getAuthor().getName()).append(" • ").append(thread.postCount());
			sb.append(" ").append((thread.postCount() == 1 ? getString(R.string.replySingular) : getString(R.string.replyPlural)));

			if (thread.readerCount() > 0)
				sb.append(" • ").append(thread.readerCount()).append(" ").append(getString(R.string.viewing));
			((TextView)threadView.findViewById(R.id.threadInfo)).setText(sb.toString());
			((TextView)threadView.findViewById(R.id.postDateInfo)).setText(thread.getLastPostTime());

			threadView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					FPThread thread = (FPThread)v.getTag();

					Intent intent = new Intent(ForumActivity.this, ThreadActivity.class);
					intent.putExtra("thread_id", thread.getId());
					intent.putExtra("page", thread.pageCount());
					startActivity(intent);
				}
			});

			threadList.addView(threadView);
		}
		RelativeLayout changePage = (RelativeLayout)inflater.inflate(R.layout.changepage, threadList, false);
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.page)).append(" ").append(page).append("/").append(numPage);
		((TextView)changePage.findViewById(R.id.pageCount)).setText(sb.toString());
		((TextView)changePage.findViewById(R.id.pageCount)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Allow user to either select page or go to last page
				final CharSequence[] items = { getString(R.string.selectPage), getString(R.string.lastPage) };

				new AlertDialog.Builder(ForumActivity.this).setTitle(getString(R.string.changePageTitle)).setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 0) { // Select Page
							
						} else if (item == 1) { // Last Page
							Intent intent = new Intent(ForumActivity.this, ForumActivity.class);
							intent.putExtra("forum_id", forumId);
							intent.putExtra("forum_name", forumName);
							intent.putExtra("page", numPage);
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
				Intent intent = new Intent(ForumActivity.this, ForumActivity.class);
				intent.putExtra("forum_id", forumId);
				intent.putExtra("forum_name", forumName);
				intent.putExtra("page", page - 1);
				startActivity(intent);
			}
		});
		((Button)changePage.findViewById(R.id.nextPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page + 1 > numPage)
					return;
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
