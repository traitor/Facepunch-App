package nl.vertinode.facepunch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nl.vertinode.facepunch.FacepunchAPI.PrivateMessage;
import android.content.Context;
import android.content.Intent;
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

public class PMActivity extends FPActivity {
	private int page = 1;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		// Load layout
		super.onCreate( savedInstanceState );
		setContentView( R.layout.pms );
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			page = extras.getInt("page");
		}
		
		// Show loading spinner
		LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		final LinearLayout pmList = (LinearLayout)findViewById( R.id.pmList );
		final ImageView loaderImage = (ImageView)inflater.inflate( R.layout.loadingspinner, pmList, false );
		applyLoadingIcon( loaderImage );
		pmList.addView( loaderImage );
		pmList.setGravity( Gravity.CENTER_VERTICAL );
		
		api.listPrivateMessages(page, new FacepunchAPI.PMListCallback() {
			public void onResult(boolean success, PrivateMessage[] pms) {
				pmList.removeView( loaderImage );
				pmList.setGravity( Gravity.NO_GRAVITY );
				
				if (success)
					populatePMs(pms);
				else
					Toast.makeText( PMActivity.this, getString( R.string.pmLoadFailed ), Toast.LENGTH_SHORT ).show();
			}
		});
	}
	
	private void populatePMs(PrivateMessage[] pms) {
		LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		LinearLayout pmList = (LinearLayout)findViewById( R.id.pmList );
		
		LinearLayout header = (LinearLayout)inflater.inflate( R.layout.listheader, pmList, false );
		( (TextView)header.findViewById( R.id.headerTitle ) ).setText("Private Messages");
		pmList.addView( header );
		
		// Populate list with results
		for (PrivateMessage pm : pms )
		{
			LinearLayout pmView = (LinearLayout)inflater.inflate( R.layout.listthread, pmList, false );
			pmView.setTag(pm);
			((TextView)pmView.findViewById(R.id.threadTitle)).setText(pm.getTitle());
			
			StringBuilder sb = new StringBuilder();
			sb.append(pm.getAuthor().getName()).append(" • ").append(timestampToDate(pm.getTime()));
			((TextView)pmView.findViewById(R.id.threadInfo)).setText(sb.toString());
			
			pmView.setOnClickListener(new OnClickListener()
				{
					public void onClick( View v )
					{
						Intent intent = new Intent(PMActivity.this, ViewPMActivity.class);
						PrivateMessage pm = (PrivateMessage)v.getTag();
						intent.putExtra("pm_id", pm.getId());
						intent.putExtra("pm_title", pm.getTitle());
						startActivity(intent);
					}
			});
			
			pmList.addView(pmView);
		}
		RelativeLayout changePage = (RelativeLayout)inflater.inflate(R.layout.changepage, pmList, false);
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.page)).append(" ").append(page);
		((TextView)changePage.findViewById(R.id.pageCount)).setText(sb.toString());
		((Button)changePage.findViewById(R.id.previousPage)).setOnClickListener(new OnClickListener()
		{
			public void onClick( View v )
			{
				if (page <= 1)
					return;
				Intent intent = new Intent(PMActivity.this, PMActivity.class);
				intent.putExtra("page", page - 1);
				startActivity(intent);
			}
		});
		((Button)changePage.findViewById(R.id.nextPage)).setOnClickListener(new OnClickListener()
		{
			public void onClick( View v )
			{
				Intent intent = new Intent(PMActivity.this, PMActivity.class);
				intent.putExtra("page", page + 1);
				startActivity(intent);
			}
		});
		pmList.addView(changePage);
	}
	
	private String timestampToDate(long input){
		Date date = new Date(input * 1000);
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
		sdf.setCalendar(cal);
		cal.setTime(date);
		return sdf.format(date);
	}
}
