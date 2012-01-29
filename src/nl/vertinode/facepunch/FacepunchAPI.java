package nl.vertinode.facepunch;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class FacepunchAPI {
	
	private String username = "";
	private String password = "";
	private boolean loggedIn = false;
	
	public boolean loggedIn() { return loggedIn; }
	public String username() { return username; }
	
	public void logout() {
		username = "";
		password = "";
		loggedIn = false;
	}
	
	public enum LoginStatus {
		FAIL,
		NO_USERNAME_PASSWORD,
		INCORRECT_USERNAME_PASSWORD,
		LOGIN_OK,
	}
	
	public class Category {
		private String name;
		private Forum[] forums;
		
		public String getName() { return name; }
		public Forum[] getForums() { return forums; }
	}
	
	public class Forum {
		private int id;
		private String name;
		private int viewers;
		private Forum[] forums;
		
		public int getId() { return id; }
		public String getName() { return name; }
		public int getViewers() { return viewers; }
		public Forum[] getSubForums() { return forums; }
	}
	
	public class FPUser {
		private int id;
		private String name;
		private String title;
		
		public int getId() { return id; }
		public String getName() { return name; }
		public String getTitle() { return name; }
		
		//todo: wait for api to add these
		public String getJoinMonth() { return ""; }
		public String getJoinYear() { return ""; }
		public int getPostCount() { return 0; }
	}
	
	public class FPThread {
		private int id;
		private String title;
		private String icon;
		private String status;
		private int pages;
		private int reading;
		private int replies;
		private int views;
		private FPUser author;
		private int lastPostId;
		private FPUser lastPostAuthor;
		private String lastPostTime;
		private int newPosts = 0;
		
		public int getId() { return id; }
		public String getTitle() { return title; }
		public String getIcon() { return icon; }
		public String getStatus() { return status; }
		public int pageCount() { return pages; }
		public int readerCount() { return reading; }
		public int postCount() { return replies; }
		public int viewCount() { return views; }
		public FPUser getAuthor() { return author; }
		public int getLastPostId() { return lastPostId; }
		public FPUser getLastPostAuthor() { return lastPostAuthor; }
		public String getLastPostTime() { return lastPostTime; }
		public int unreadPostCount() { return newPosts; }
		public boolean isSticky() { return false; /* return status.equals("sticky"); */ } //api currently return sall threads as stickied
	}
	
	/*
"ratings":
	{"informative":1},
"ratingkeys":{"agree":"5f03b97f4cb74b0a317a7f6bf2b0ae0d19282211","disagree":"54cf0c1c4f6478f990f67cbeac32ec17f2d41478","funny":"3cde450e48abba99d8fcb799f11c01f2d0dde840","winner":"e8d54f67403f1792cf2f750e51a1f601c527cfea","zing":"32700deefa97395eb0f1df185c0822f03fc44835","informative":"0181b9210da90e038117d270dd9d647ab701df2b","friendly":"5ab0a3f9ce19c3042695a0e5cf5887d85b144892","useful":"f8ca3b047f41b5389c29cc6c12d99e61ceb68ea2","programming_king":"21b4307f7b57d6032e56e226a85b8affd05f60f7","optimistic":"ea03e1088bc958f6983ac120adf0df6b39087ee2","artistic":"35cca6979da58d968f0d62ebf8fed3bd784aa4fd","late":"f3e3f11a24919d0eb7d3b760bf5f7d11f3cdc986","dumb":"130c2ccd214b42b6670ca38c0173632cc3c7d1f9"}},
*/
	public class FPPost {
		private int id;
		private FPUser author;
		private String postTime;
		private String message;
		private String status;
		
		public int getId() { return id; }
		public FPUser getAuthor() { return author; }
		public String getDate() { return postTime; }
		public String getMessageHTML() { return message; }
		public String getStatus() { return status; }
		
		//todo: ratings & rating keys.
	}
	
	public interface LoginCallback
	{
		public void onResult(LoginStatus status);
	}
	
	public void checkLogin(String username, final String password, final LoginCallback callback) {
		this.username = username;
		this.password = MD5(password);
		asyncWebRequest("?username=" + this.username + "&password=" + this.password +"&action=authenticate", new WebRequestCallback() {
			public void onResult( String response, String cookies ) {
				if (response == null) {
					callback.onResult(LoginStatus.FAIL);
					return;
				}
				
				JSONObject json;
				try {
					json = new JSONObject(response);
					
					if (json.has("error")) {
						String error = json.getString("error");
						if (error.contains("No username")) {
							callback.onResult(LoginStatus.NO_USERNAME_PASSWORD);
							return;
						} else if (error.contains("Bad username")) {
							callback.onResult(LoginStatus.INCORRECT_USERNAME_PASSWORD);
							return;
						}
					} else if (json.has("login")) {
						//Login OK.
						loggedIn = true;
						callback.onResult(LoginStatus.LOGIN_OK);
						return;
					}
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(LoginStatus.FAIL);
			}
		});
	}
	
	public interface ForumCallback
	{
		public void onResult(boolean success, Category[] categories);
	}
	
	public void listForums(final ForumCallback callback)
	{
		asyncWebRequest("?username=" + this.username + "&password=" + this.password +"&action=getforums", new WebRequestCallback()
		{
			public void onResult(String source, String cookies)
			{
				JSONObject json;
				try {
					json = new JSONObject(source);
					
					JSONArray array = json.getJSONArray("categories");
					Category[] categories = new Category[array.length()];
					for (int i=0; i < array.length(); i++) {	
						JSONObject e = array.getJSONObject(i);
						Category category = new Category();
						category.name = e.getString("name");
						category.forums = getSubForums(e.getJSONArray("forums"));
						categories[i] = category;
					}
					callback.onResult(true, categories);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}
	
	private Forum[] getSubForums(JSONArray array) {
		Forum[] subForums = new Forum[array.length()];
		for (int i=0; i < array.length(); i++) {	
			try {
				JSONObject e = array.getJSONObject(i);
				Forum forum = new Forum();
				forum.id = e.getInt("id");
				forum.name = e.getString("name");
				if (e.has("viewing"))
					forum.viewers = e.getInt("viewing");
				if (e.has("forums"))
					forum.forums = getSubForums(e.getJSONArray("forums"));
				subForums[i] = forum;
			} catch (JSONException e) {
				Log.e("JSONException", e.toString());
			}
		}
		return subForums;
	}
	
	public interface ThreadCallback
	{
		public void onResult( boolean success, FPThread[] threads );
	}
	
	public void listThreads( int forumId, int page, final ThreadCallback callback )
	{
		asyncWebRequest("?username=" + this.username + "&password=" + this.password +"&action=getthreads&forum_id=" + forumId + "&page=" + page, 
				new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);
					
					JSONArray array = json.getJSONArray("threads");
					FPThread[] threads = new FPThread[array.length()];
					for (int i=0; i < array.length(); i++) {	
						JSONObject e = array.getJSONObject(i);
						FPThread thread = new FPThread();
						thread.id = e.getInt("id");
						thread.title = e.getString("title");
						thread.icon = e.getString("icon");
						thread.status = e.getString("status");
						thread.pages = e.getInt("pages");
						thread.reading = e.getInt("reading");
						thread.replies = e.getInt("replies");
						thread.views = e.getInt("views");
						FPUser author = new FPUser();
						author.id = e.getInt("authorid");
						author.name = e.getString("author");
						thread.author = author;
						thread.lastPostId = e.getInt("lastpostid");
						FPUser lastPostAuthor = new FPUser();
						author.id = e.getInt("lastpostauthorid");
						author.name = e.getString("lastpostauthorname");
						thread.lastPostAuthor = lastPostAuthor;
						thread.lastPostTime = e.getString("lastposttime");
						if (e.has("newposts"))
							thread.newPosts = e.getInt("newposts");
						threads[i] = thread;
					}
					callback.onResult(true, threads);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}
	
	public interface PostCallback
	{
		public void onResult(boolean success, FPPost[] posts);
	}
	
	public void listPosts(final int threadId, int page, final PostCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password +"&action=getposts&thread_id=" + threadId + "&page=" + page, 
				new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);
					
					JSONArray array = json.getJSONArray("posts");
					FPPost[] posts = new FPPost[array.length()];
					for (int i=0; i < array.length(); i++) {	
						JSONObject e = array.getJSONObject(i);
						FPPost post = new FPPost();
						post.id = e.getInt("id");
						post.author = new FPUser();
						post.author.id = e.getInt("userid");
						post.author.name = e.getString("username");
						post.author.title = e.getString("usertitle");
						post.postTime = e.getString("time");
						post.status = e.getString("status");
						post.message = e.getString("message");
						posts[i] = post;
					}
					callback.onResult(true, posts);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}
	
	private String MD5(String md5) {
	   try {
	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
	        byte[] array = md.digest(md5.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	       }
	        return sb.toString();
	    } catch (java.security.NoSuchAlgorithmException e) {
	    }
	    return null;
	}
	
	// Retrieve avatar
	public interface AvatarCallback
	{
		public void onResult( boolean success, Bitmap avatar );
	}
	
	/**
	 * Retrieve the avatar image of the specified user.
	 * 
	 * @author Overv
	 * 
	 * @param userId User identifier.
	 * @param callback The function to pass the avatar image to.
	 */
	public void getAvatar( int userId, final AvatarCallback callback )
	{
		asyncImageRequest( "http://www.facepunch.com/avatar/" + userId + ".png", new ImageRequestCallback()
		{
			public void onResult( Bitmap image )
			{
				if ( image == null )
					callback.onResult( false, null );
				else
					callback.onResult( true, image );
			}
		} );
	}
	
	private interface ImageRequestCallback
	{
		public void onResult( Bitmap image );
	}
	
	private void asyncImageRequest( String url, ImageRequestCallback callback )
	{
		new ImageRequest().execute( url, callback );
	}
	
	private class ImageRequest extends AsyncTask<Object, Void, Object[]>
	{
		protected Object[] doInBackground( Object... params )
		{
			Bitmap img = null;
			
			try
			{
				URL url = new URL( (String)params[0] );
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setInstanceFollowRedirects( true );
				conn.setDoInput( true );
				conn.connect();
				
				img = BitmapFactory.decodeStream( conn.getInputStream() );
			} catch ( IOException e ) {}
			
			return new Object[]{ img, params[1] };
		}
		
		protected void onPostExecute( Object[] params )
		{
			ImageRequestCallback callback = (ImageRequestCallback)params[1];
			
			if ( params[0] == null )
			{
				callback.onResult( null );
				return;
			}
			
			callback.onResult( (Bitmap)params[0] );
		}
	}

	// Perform Facepunch web request
	private interface WebRequestCallback
	{
		public void onResult( String response, String cookies );
	}
	
	private void asyncWebRequest( String url, WebRequestCallback callback )
	{
		new WebRequest().execute( url, callback );
	}
	
	private class WebRequest extends AsyncTask<Object, Void, Object[]>
	{
		protected Object[] doInBackground( Object... params )
		{
			String request = "https://api.facepun.ch/" + (String)params[0];
			WebRequestCallback callback = (WebRequestCallback)params[1];
			
			StringBuilder response = new StringBuilder();
			String cookies = "";
			
			try
			{
				URL url = new URL( request );
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setInstanceFollowRedirects( true );
				
				// Get response text
				InputStreamReader reader = new InputStreamReader( conn.getInputStream() );
				char[] buffer = new char[65535];
				int n = 0;
				while ( n >= 0 )
				{
					n = reader.read( buffer, 0, buffer.length );
					if ( n > 0 ) response.append( buffer, 0, n );
				}
				reader.close();
				
				return new Object[] { callback, response.toString(), cookies };
			} catch ( IOException e )
			{
				return new Object[] { callback };
			}
		}
		
		protected void onPostExecute( Object[] params )
		{
			if ( params.length == 1 )
			{
				( (WebRequestCallback)params[0] ).onResult( null, null );
				return;
			}
			
			WebRequestCallback callback = (WebRequestCallback)params[0];
			String response = (String)params[1];
			String cookies = (String)params[2];
			
			callback.onResult( response, cookies );
		}
	}
}
