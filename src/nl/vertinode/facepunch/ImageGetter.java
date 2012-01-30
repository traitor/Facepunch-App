package nl.vertinode.facepunch;

import android.graphics.drawable.Drawable;
import android.text.Html;

public class ImageGetter implements Html.ImageGetter {
	public Drawable getDrawable(String source) {
		return null; // temp.
		/*if (source.startsWith("/fp/")) {
			StringBuilder sb = new StringBuilder();
			sb.append("http://facepunch.com").append(source);
			source = sb.toString();
		}
		Bitmap img = null;
		try {
			URL url = new URL(source);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setInstanceFollowRedirects( true );
			conn.setDoInput( true );
			conn.connect();
			
			img = BitmapFactory.decodeStream( conn.getInputStream() );
		} catch ( IOException e ) { }
		if (img == null)
			return null;
		Drawable d = new BitmapDrawable(img);
		d.setBounds(0, 0, img.getWidth(), img.getHeight());
		return d;*/
	}
}