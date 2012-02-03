package nl.vertinode.facepunch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import nl.vertinode.facepunch.FacepunchAPI.FPPost;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

public class ThreadActivity extends FPActivity {

	private String title = "";
	private int threadId = -1;
	private int page = 1;
	private int pageCount = 1;
	private int post = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threadview);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			threadId = extras.getInt("thread_id");
			page = extras.getInt("page");
			if (extras.containsKey("post"))
				post = extras.getInt("postid");
		}

		changePage();
	}

	private void changePage() {
		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout postList = (LinearLayout)findViewById(R.id.postList);
		postList.removeAllViews();
		final ImageView loaderImage = (ImageView)inflater.inflate(R.layout.loadingspinner, postList, false);
		applyLoadingIcon(loaderImage);
		postList.addView(loaderImage);
		postList.setGravity(Gravity.CENTER_VERTICAL);

		api.listPosts(threadId, page, post, new FacepunchAPI.PostCallback() {
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

	private void populateThread(FPPost[] posts) {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout postList = (LinearLayout)findViewById(R.id.postList);

		LinearLayout header = (LinearLayout)inflater.inflate(R.layout.listheader, postList, false);
		((TextView)header.findViewById(R.id.headerTitle)).setText(title);
		header.removeView(((View)header.findViewById(R.id.listHeaderSeparator)));
		postList.addView(header);

		// Populate list with results
		for (FPPost post : posts) {
			final RelativeLayout postView = (RelativeLayout)inflater.inflate(R.layout.post, postList, false);
			if (post.getStatus().equals("new"))
				postView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluegradient));// setBackgroundColor(Color.rgb(0xc6,0xde,0xfd));
			postView.getBackground().setDither(true);
			((TextView)postView.findViewById(R.id.usernameText)).setText(post.getAuthor().getName());
			if (post.getAuthor().getTitle() != null && !post.getAuthor().getTitle().equals("")) {
				((TextView)postView.findViewById(R.id.userTitleText)).setText(Html.fromHtml(post.getAuthor().getTitle()));
				((TextView)postView.findViewById(R.id.joinDateText)).setText(post.getAuthor().getJoinDate());
				StringBuilder sb = new StringBuilder();
				sb.append(post.getAuthor().getPostCount()).append(" ").append(getString(R.string.posts));
				((TextView)postView.findViewById(R.id.postCountText)).setText(sb.toString());
			} else { // There's probably a much nicer, cleaner way. but
						// whatever.
				((TextView)postView.findViewById(R.id.userTitleText)).setText(post.getAuthor().getJoinDate());
				StringBuilder sb = new StringBuilder();
				sb.append(post.getAuthor().getPostCount()).append(" ").append(getString(R.string.posts));
				((TextView)postView.findViewById(R.id.joinDateText)).setText(sb.toString());
				((TextView)postView.findViewById(R.id.postCountText)).setText("");
			}
			((TextView)postView.findViewById(R.id.postDate)).setText(post.getDate());
			final TextView content = ((TextView)postView.findViewById(R.id.postContent));
			Spanned html = Html.fromHtml(post.getMessageHTML());
			content.setText(html);
			new ImageLoadTask().execute(new SpannableStringBuilder(html), new ImageLoadCallback() {

				public void onResult(SpannableStringBuilder html) {
					content.setText(html);
				}
			});

			api.getAvatar(post.getAuthor().getId(), new FacepunchAPI.AvatarCallback() {
				public void onResult(boolean success, Bitmap avatar) {
					if (avatar == null)
						return;
					// Bitmap resized = Bitmap.createScaledBitmap(avatar, 64,
					// 64, false);
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
				// Allow user to either select page or go to last page
				final CharSequence[] items = { getString(R.string.selectPage), getString(R.string.lastPage) };

				new AlertDialog.Builder(ThreadActivity.this).setTitle(getString(R.string.changePageTitle)).setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 0) { // Select Page

						} else if (item == 1) { // Last Page
							page = pageCount;
							changePage();
						}
					}
				}).create().show();
			}
		});

		((Button)changePage.findViewById(R.id.previousPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page <= 1)
					return;
				page--;
				changePage();
			}
		});
		((Button)changePage.findViewById(R.id.nextPage)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (page + 1 > pageCount)
					return;
				page++;
				changePage();
			}
		});
		postList.addView(changePage);
	}

	private interface ImageLoadCallback {
		public void onResult(SpannableStringBuilder html);
	}

	private class ImageLoadTask extends AsyncTask<Object, Void, Object[]> {
		DisplayMetrics metrics = new DisplayMetrics();
		SpannableStringBuilder html;

		@Override
		protected void onPreExecute() {

			// we need this to properly scale the images later
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

		}

		protected Object[] doInBackground(Object... params) {
			html = (SpannableStringBuilder)params[0];

			// iterate over all images found in the html
			for (ImageSpan img : html.getSpans(0, html.length(), ImageSpan.class)) {
				onProgressUpdate(img);
			}

			return new Object[] { html, params[1] };
		}

		protected void onProgressUpdate(ImageSpan values) {

			// save ImageSpan to a local variable just for convenience
			ImageSpan img = values;

			// now we get the File object again. so remeber to always return
			// the same file for the same ImageSpan object
			Bitmap bitmap = getImageFile(img);

			// if the file exists, show it
			if (bitmap != null) {

				// first we need to get a Drawable object
				Drawable d = new BitmapDrawable(bitmap);

				// next we do some scaling
				int width, height;
				int originalWidthScaled = (int)(d.getIntrinsicWidth() * metrics.density);
				int originalHeightScaled = (int)(d.getIntrinsicHeight() * metrics.density);
				if (originalWidthScaled > metrics.widthPixels) {
					height = d.getIntrinsicHeight() * metrics.widthPixels / d.getIntrinsicWidth();
					width = metrics.widthPixels;
				} else {
					height = originalHeightScaled;
					width = originalWidthScaled;
				}

				// it's important to call setBounds otherwise the image will
				// have a size of 0px * 0px and won't show at all
				d.setBounds(0, 0, width, height);

				// now we create a new ImageSpan
				ImageSpan newImg = new ImageSpan(d, img.getSource());

				// find the position of the old ImageSpan
				int start = html.getSpanStart(img);
				int end = html.getSpanEnd(img);

				// remove the old ImageSpan
				html.removeSpan(img);

				// add the new ImageSpan
				html.setSpan(newImg, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		protected void onPostExecute(Object[] params) {
			SpannableStringBuilder span = (SpannableStringBuilder)params[0];
			ImageLoadCallback callback = (ImageLoadCallback)params[1];

			if (span == null) {
				callback.onResult(null);
				return;
			}

			callback.onResult(span);
		}

		private Bitmap getImageFile(ImageSpan img) {
			Bitmap bitmap = null;

			try {
				String source = img.getSource();
				if (source.startsWith("/fp/")) {
					StringBuilder sb = new StringBuilder();
					sb.append("http://facepunch.com").append(source);
					source = sb.toString();
				}
				URL url = new URL(source);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setInstanceFollowRedirects(true);
				conn.setDoInput(true);
				conn.connect();

				bitmap = BitmapFactory.decodeStream(conn.getInputStream());
			} catch (IOException e) {
			}

			return bitmap;
		}
	}
}
