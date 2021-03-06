package nl.vertinode.facepunch;

import nl.vertinode.facepunch.FacepunchAPI.Category;
import nl.vertinode.facepunch.FacepunchAPI.Forum;
import nl.vertinode.facepunch.R;

import android.content.Context;
import android.content.Intent;
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

public class FrontpageActivity extends FPActivity {
	// Used for restoring list
	private class State {
		Category[] categories;
		int scrollY;
	}

	private Category[] displayedCategories;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frontpage);

		// Restore forum list
		final State data = (State)getLastNonConfigurationInstance();
		if (data != null) {
			populateList(data.categories);

			final ScrollView scroller = (ScrollView)findViewById(R.id.scroller);
			scroller.post(new Runnable() {
				public void run() {
					scroller.scrollTo(0, data.scrollY);
				}
			});

			return;
		}

		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout forumList = (LinearLayout)findViewById(R.id.forumList);
		final ImageView loaderImage = (ImageView)inflater.inflate(R.layout.loadingspinner, forumList, false);
		applyLoadingIcon(loaderImage);
		forumList.addView(loaderImage);
		forumList.setGravity(Gravity.CENTER_VERTICAL);

		// Fetch main categories and forums
		api.listForums(new FacepunchAPI.ForumCallback() {
			public void onResult(boolean success, Category[] categories) {
				// Remove loading spinner
				forumList.removeView(loaderImage);
				forumList.setGravity(Gravity.NO_GRAVITY);

				if (success) {
					populateList(categories);
				} else {
					Toast.makeText(FrontpageActivity.this, getString(R.string.frontpageLoadingFailed), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	// Save activity state
	@Override
	public Object onRetainNonConfigurationInstance() {
		State data = new State();

		data.categories = displayedCategories;
		data.scrollY = ((ScrollView)findViewById(R.id.scroller)).getScrollY();

		return data;
	}

	// Populate list with categories and forums
	private void populateList(Category[] categories) {
		displayedCategories = categories;

		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout forumList = (LinearLayout)findViewById(R.id.forumList);

		// Populate list with results
		for (Category cat : categories) {
			// Create header view
			LinearLayout header = (LinearLayout)inflater.inflate(R.layout.listheader, forumList, false);
			((TextView)header.findViewById(R.id.headerTitle)).setText(cat.getName().toUpperCase());
			forumList.addView(header);

			// Add forums
			LinearLayout lastForum = null;
			for (Forum forum : cat.getForums()) {
				if (forum == null)
					continue;
				LinearLayout forumItem = (LinearLayout)inflater.inflate(R.layout.listforum, forumList, false);
				((TextView)forumItem.findViewById(R.id.forumTitle)).setText(forum.getName());
				forumItem.setTag(forum);

				// Attempt to find icon for this forum
				int resId = getResources().getIdentifier("drawable/forumicon_" + forum.getId(), "drawable", getPackageName());
				ImageView iconView = (ImageView)forumItem.findViewById(R.id.forumIcon);
				if (resId != 0)
					iconView.setImageResource(resId);
				else
					iconView.setVisibility(View.INVISIBLE);

				forumList.addView(forumItem);
				forumItem.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Forum forum = (Forum)v.getTag();

						Intent intent = new Intent(FrontpageActivity.this, ForumActivity.class);
						intent.putExtra("forum_id", forum.getId());
						intent.putExtra("forum_name", forum.getName());
						startActivity(intent);
					}
				});
				lastForum = forumItem;
			}

			// Modify separator of last forum to account for next category
			// header
			View separator = lastForum.findViewById(R.id.listSeparator);
			separator.setBackgroundColor(Color.WHITE);
		}
	}
}
