package nl.vertinode.facepunch;

import nl.vertinode.facepunch.FacepunchAPI.PrivateMessage;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPMActivity extends FPActivity {
	private int pmId = -1;
	private String pmTitle = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pms);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			pmId = extras.getInt("pm_id");
			pmTitle = extras.getString("pm_title");
		}
		if (pmId == -1) {
			Toast.makeText(ViewPMActivity.this, getString(R.string.pmLoadFailed), Toast.LENGTH_SHORT).show();
			return;
		}

		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout pmList = (LinearLayout)findViewById(R.id.pmList);
		final ImageView loaderImage = (ImageView)inflater.inflate(R.layout.loadingspinner, pmList, false);
		applyLoadingIcon(loaderImage);
		pmList.addView(loaderImage);
		pmList.setGravity(Gravity.CENTER_VERTICAL);

		api.getPrivateMessage(pmId, new FacepunchAPI.PMCallback() {
			public void onResult(boolean success, PrivateMessage pm) {
				pmList.removeView(loaderImage);
				pmList.setGravity(Gravity.NO_GRAVITY);

				if (success)
					populatePrivateMessage(pm);
				else
					Toast.makeText(ViewPMActivity.this, getString(R.string.pmLoadFailed), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void populatePrivateMessage(PrivateMessage pm) {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout pmList = (LinearLayout)findViewById(R.id.pmList);

		LinearLayout header = (LinearLayout)inflater.inflate(R.layout.listheader, pmList, false);
		((TextView)header.findViewById(R.id.headerTitle)).setText(pmTitle);
		pmList.addView(header);

		final RelativeLayout postView = (RelativeLayout)inflater.inflate(R.layout.post, pmList, false);
		((TextView)postView.findViewById(R.id.usernameText)).setText(pm.getAuthor().getName());

		StringBuilder sb = new StringBuilder();
		sb.append("Sent ").append(pm.getDate());

		((TextView)postView.findViewById(R.id.joinDateText)).setText(sb.toString());
		((TextView)postView.findViewById(R.id.postCountText)).setText("");
		((TextView)postView.findViewById(R.id.postContent)).setText(Html.fromHtml(pm.getMessage(), new ImageGetter(), new TagHandler()));

		api.getAvatar(pm.getAuthor().getId(), new FacepunchAPI.AvatarCallback() {
			public void onResult(boolean success, Bitmap avatar) {
				if (avatar == null)
					return;
				Bitmap resized = Bitmap.createScaledBitmap(avatar, 60, 60, false);
				((ImageView)postView.findViewById(R.id.avatarView)).setImageBitmap(resized);
			}
		});

		pmList.addView(postView);
	}
}
