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

	public boolean loggedIn() {
		return loggedIn;
	}

	public String username() {
		return username;
	}

	public void logout() {
		username = "";
		password = "";
		loggedIn = false;
	}

	public enum LoginStatus {
		FAIL, NO_USERNAME_PASSWORD, INCORRECT_USERNAME_PASSWORD, LOGIN_OK,
	}

	public class Category {
		private String name;
		private Forum[] forums;

		public String getName() {
			return name;
		}

		public Forum[] getForums() {
			return forums;
		}
	}

	public class Forum {
		private int id;
		private String name;
		private int viewers;
		private Forum[] forums;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getViewers() {
			return viewers;
		}

		public Forum[] getSubForums() {
			return forums;
		}
	}

	public class FPUser {
		private int id;
		private String name;
		private String title;
		private String joinDate;
		private int postCount;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getTitle() {
			return title;
		}
		
		public String getJoinDate() {
			return joinDate;
		}

		public int getPostCount() {
			return postCount;
		}

		// todo: ratings
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
		private boolean locked = false;

		public int getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getIcon() {
			return icon;
		}

		public String getStatus() {
			return status;
		}

		public int pageCount() {
			return pages;
		}

		public int readerCount() {
			return reading;
		}

		public int postCount() {
			return replies;
		}

		public int viewCount() {
			return views;
		}

		public FPUser getAuthor() {
			return author;
		}

		public int getLastPostId() {
			return lastPostId;
		}

		public FPUser getLastPostAuthor() {
			return lastPostAuthor;
		}

		public String getLastPostTime() {
			return lastPostTime;
		}

		public int unreadPostCount() {
			return newPosts;
		}

		public boolean isSticky() {
			return status.equals("sticky");
		}
		
		public boolean isLocked() {
			return locked;
		}
	}

	public class FPPost {
		private int id;
		private FPUser author;
		private String postTime;
		private String message;
		private String status;

		public int getId() {
			return id;
		}

		public FPUser getAuthor() {
			return author;
		}

		public String getDate() {
			return postTime;
		}

		public String getMessageHTML() {
			return message;
		}

		public String getStatus() {
			return status;
		}

		// todo: ratings & rating keys.
	}

	public class PrivateMessage {
		private int id;
		private String title;
		private FPUser author;
		private long time;
		private String date;
		private String message;
		private String status;

		public int getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public FPUser getAuthor() {
			return author;
		}

		public long getTime() {
			return time;
		}

		public String getDate() {
			return date;
		}

		public String getMessage() {
			return message;
		}

		public String getStatus() {
			return status;
		}
	}

	public class FPIcon {
		private int id;
		private String name;
		private String url;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}
	}

	public interface LoginCallback {
		public void onResult(LoginStatus status);
	}

	public void checkLogin(String username, final String password, final LoginCallback callback) {
		this.username = username;
		this.password = MD5(password);
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=authenticate", new WebRequestCallback() {
			public void onResult(String response, String cookies) {
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
						// Login OK.
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

	public interface ForumCallback {
		public void onResult(boolean success, Category[] categories);
	}

	public void listForums(final ForumCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getforums", new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);

					JSONArray array = json.getJSONArray("categories");
					Category[] categories = new Category[array.length()];
					for (int i = 0; i < array.length(); i++) {
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
		for (int i = 0; i < array.length(); i++) {
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

	public interface ThreadCallback {
		public void onResult(boolean success, FPThread[] threads, int pageCount);
	}

	public void listThreads(int forumId, int page, final ThreadCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getthreads&forum_id=" + forumId + "&page=" + page, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);
					callback.onResult(true, parseThreads(json.getJSONArray("threads")), json.getInt("numpages"));
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null, 0);
			}
		});
	}

	public void listPopularThreads(final ThreadCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getpopularthreads", new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);
					callback.onResult(true, parseThreads(json.getJSONArray("threads")), 1);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null, 0);
			}
		});
	}

	public void listReadThreads(final ThreadCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getreadthreads", new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);
					callback.onResult(true, parseThreads(json.getJSONArray("threads")), 1);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null, 0);
			}
		});
	}

	private FPThread[] parseThreads(JSONArray array) throws JSONException {
		FPThread[] threads = new FPThread[array.length()];
		for (int i = 0; i < array.length(); i++) {
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
			thread.author = new FPUser();
			thread.author.id = e.getInt("authorid");
			thread.author.name = e.getString("author");
			thread.lastPostId = e.getInt("lastpostid");
			thread.lastPostAuthor = new FPUser();
			thread.lastPostAuthor.id = e.getInt("lastpostauthorid");
			thread.lastPostAuthor.name = e.getString("lastpostauthorname");
			thread.lastPostTime = e.getString("lastposttime");
			thread.locked = e.getBoolean("locked");
			if (e.has("newposts"))
				thread.newPosts = e.getInt("newposts");
			threads[i] = thread;
		}
		return threads;
	}

	public interface PostCallback {
		public void onResult(boolean success, FPPost[] posts, String threadTitle, int pageCount);
	}

	public void listPosts(final int threadId, int page, final PostCallback callback) {
		listPosts(threadId, page, -1, callback);
	}
	
	public void listPosts(final int threadId, int page, int postId, final PostCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getposts&thread_id=" + threadId + (postId != -1 ? "?p=" + postId : "") + "&page=" + page, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				if (source == null) { //Server error.
					Log.e("ServerError", "Returned source was null.");
					callback.onResult(false, null, "", 0);
					return;
				}
				
				JSONObject json;
				try {
					json = new JSONObject(source);

					JSONArray array = json.getJSONArray("posts");
					String title = json.getString("title");
					int numPages = json.getInt("numpages");
					if (numPages == 0)
						numPages = 1;
					FPPost[] posts = new FPPost[array.length()];
					for (int i = 0; i < array.length(); i++) {
						JSONObject e = array.getJSONObject(i);
						FPPost post = new FPPost();
						post.id = e.getInt("id");
						post.author = new FPUser();
						post.author.id = e.getInt("userid");
						post.author.name = e.getString("username");
						post.author.title = e.getString("usertitle");
						post.author.postCount = e.getInt("postcount");
						post.author.joinDate = e.getString("joindate");
						post.postTime = e.getString("time");
						post.status = e.getString("status");
						post.message = e.getString("message");
						posts[i] = post;
					}
					callback.onResult(true, posts, title, numPages);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null, "", 0);
			}
		});
	}

	public interface PMListCallback {
		public void onResult(boolean success, PrivateMessage[] pms);
	}

	public void listPrivateMessages(int page, final PMListCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getpms&page=" + page, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);

					JSONArray array = json.getJSONArray("messages");
					PrivateMessage[] pms = new PrivateMessage[array.length()];
					for (int i = 0; i < array.length(); i++) {
						JSONObject e = array.getJSONObject(i);
						PrivateMessage pm = new PrivateMessage();
						pm.id = e.getInt("id");
						pm.title = e.getString("title");
						pm.author = new FPUser();
						pm.author.name = e.getString("sender");
						pm.author.id = e.getInt("senderid");
						pm.time = e.getLong("time");
						pms[i] = pm;
					}
					callback.onResult(true, pms);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}

	public interface PMCallback {
		public void onResult(boolean success, PrivateMessage pm);
	}

	public void getPrivateMessage(final int pmId, final PMCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getpm&pm_id=" + pmId, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);

					PrivateMessage pm = new PrivateMessage();
					pm.id = pmId;
					pm.date = json.getString("time");
					pm.author = new FPUser();
					pm.author.name = json.getString("username");
					pm.author.id = json.getInt("userid");
					pm.author.title = json.getString("usertitle");
					pm.message = json.getString("message");
					pm.status = json.getString("status");
					callback.onResult(true, pm);
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}

	public interface IconCallback {
		public void onResult(boolean success, FPIcon[] icons);
	}

	public void getPmIcons(final IconCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getpmicons", new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);

					callback.onResult(true, parseIcons(json.getJSONArray("icons")));
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}

	public void getThreadIcons(int forumId, final IconCallback callback) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=getthreadicons&forum_id=" + forumId, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				JSONObject json;
				try {
					json = new JSONObject(source);

					callback.onResult(true, parseIcons(json.getJSONArray("icons")));
					return;
				} catch (JSONException e) {
					Log.e("JSONException", e.toString());
				}
				callback.onResult(false, null);
			}
		});
	}

	private FPIcon[] parseIcons(JSONArray array) throws JSONException {
		FPIcon[] icons = new FPIcon[array.length()];
		for (int i = 0; i < array.length(); i++) {
			JSONObject e = array.getJSONObject(i);
			FPIcon icon = new FPIcon();
			icon.id = e.getInt("id");
			icon.name = e.getString("name");
			icon.url = e.getString("url");
			icons[i] = icon;
		}
		return icons;
	}

	public void postReply(int threadId, String message/*
													 * , final PostReplyCallback
													 * callback
													 */) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=newreply&thread_id=" + threadId + "&message=" + message, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				/* todo */
			}
		});
	}

	public void newThread(int forumId, String subject, String icon, String message/*
																				 * ,
																				 * final
																				 * NewThreadCallback
																				 * callback
																				 */) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=newthread&forum_id=" + forumId + "&subject=" + subject + "&body=" + message + "&icon=" + icon, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				/* todo */
			}
		});
	}

	public void sendPm(String[] recipients, String subject, String body, String icon/*
																					 * ,
																					 * final
																					 * SendPMCallback
																					 * callback
																					 */) {
		StringBuilder joined = new StringBuilder();
		for (int i = 0; i < recipients.length; i++)
			joined.append(recipients[i]).append(";");
		joined.deleteCharAt(joined.length() - 1);
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=sendpm&recipients=" + joined.toString() + "&subject=" + subject + "&body=" + body + "&icon=" + icon, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				/* todo */
			}
		});
	}

	public void ratePost(int postId, String rating, String key/*
															 * , final
															 * RatingCallback
															 * callback
															 */) {
		asyncWebRequest("?username=" + this.username + "&password=" + this.password + "&action=rate&post_id=" + postId + "&rating=" + rating + "&key=" + key, new WebRequestCallback() {
			public void onResult(String source, String cookies) {
				/* todo */
			}
		});
	}

	private String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	// Retrieve avatar
	public interface AvatarCallback {
		public void onResult(boolean success, Bitmap avatar);
	}

	/**
	 * Retrieve the avatar image of the specified user.
	 * 
	 * @author Overv
	 * 
	 * @param userId
	 *            User identifier.
	 * @param callback
	 *            The function to pass the avatar image to.
	 */
	public void getAvatar(int userId, final AvatarCallback callback) {
		asyncImageRequest("http://www.facepunch.com/avatar/" + userId + ".png", new ImageRequestCallback() {
			public void onResult(Bitmap image) {
				if (image == null)
					callback.onResult(false, null);
				else
					callback.onResult(true, image);
			}
		});
	}

	private interface ImageRequestCallback {
		public void onResult(Bitmap image);
	}

	private void asyncImageRequest(String url, ImageRequestCallback callback) {
		new ImageRequest().execute(url, callback);
	}

	private class ImageRequest extends AsyncTask<Object, Void, Object[]> {
		protected Object[] doInBackground(Object... params) {
			Bitmap img = null;

			try {
				URL url = new URL((String)params[0]);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setInstanceFollowRedirects(true);
				conn.setDoInput(true);
				conn.connect();

				img = BitmapFactory.decodeStream(conn.getInputStream());
			} catch (IOException e) {
			}

			return new Object[] { img, params[1] };
		}

		protected void onPostExecute(Object[] params) {
			ImageRequestCallback callback = (ImageRequestCallback)params[1];

			if (params[0] == null) {
				callback.onResult(null);
				return;
			}

			callback.onResult((Bitmap)params[0]);
		}
	}

	// Perform Facepunch web request
	private interface WebRequestCallback {
		public void onResult(String response, String cookies);
	}

	private void asyncWebRequest(String url, WebRequestCallback callback) {
		new WebRequest().execute(url, callback);
	}

	private class WebRequest extends AsyncTask<Object, Void, Object[]> {
		protected Object[] doInBackground(Object... params) {
			String request = "https://api.facepun.ch/" + (String)params[0];
			WebRequestCallback callback = (WebRequestCallback)params[1];

			StringBuilder response = new StringBuilder();
			String cookies = "";

			try {
				URL url = new URL(request);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setInstanceFollowRedirects(true);

				// Get response text
				InputStreamReader reader = new InputStreamReader(conn.getInputStream());
				char[] buffer = new char[65535];
				int n = 0;
				while (n >= 0) {
					n = reader.read(buffer, 0, buffer.length);
					if (n > 0)
						response.append(buffer, 0, n);
				}
				reader.close();

				return new Object[] { callback, response.toString(), cookies };
			} catch (IOException e) {
				return new Object[] { callback };
			}
		}

		protected void onPostExecute(Object[] params) {
			if (params.length == 1) {
				((WebRequestCallback)params[0]).onResult(null, null);
				return;
			}

			WebRequestCallback callback = (WebRequestCallback)params[0];
			String response = (String)params[1];
			String cookies = (String)params[2];

			callback.onResult(response, cookies);
		}
	}
}
