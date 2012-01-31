package nl.vertinode.facepunch;

import nl.vertinode.facepunch.FacepunchAPI.LoginStatus;
import nl.vertinode.facepunch.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends FPActivity {
	// Used for restoring form input
	private class State {
		public String username;
		public String password;
		public boolean autoLogin;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load layout
		super.onCreate(savedInstanceState);
		suppressUserButton = true;
		setContentView(R.layout.login);

		// Views
		final Button loginButton = (Button)findViewById(R.id.loginButton);
		final EditText usernameField = (EditText)findViewById(R.id.usernameField);
		final EditText passwordField = (EditText)findViewById(R.id.passwordField);
		final CheckBox autoLogin = (CheckBox)findViewById(R.id.autoLoginCheckbox);

		// Get preference access
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		Bundle extra = getIntent().getExtras();
		boolean reset = false;
		if (extra != null) {
			if (extra.getBoolean("reset")) {
				reset = true;
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("username", "");
				editor.putString("password", "");
				editor.putBoolean("autologin", false);
				editor.commit();
			}
		}

		// Log in request handler
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String username = usernameField.getText().toString().trim();
				String password = passwordField.getText().toString().trim();

				if (username.length() == 0 && password.length() == 0)
					return;

				// Attempt logging in
				login(username, password, autoLogin.isChecked());
			}
		});

		// Restore form input
		State data = (State)getLastNonConfigurationInstance();
		if (reset)
			return; // don't fill in the fields.
		if (data != null) {
			usernameField.setText(data.username);
			passwordField.setText(data.password);
			autoLogin.setChecked(data.autoLogin);
		} else {
			// Attempt to retrieve stored login details
			String username = prefs.getString("username", "");
			String password = prefs.getString("password", "");
			boolean autologin = prefs.getBoolean("autologin", false);
			usernameField.setText(username);
			passwordField.setText(password);
			autoLogin.setChecked(autologin);

			if (username.length() != 0 && password.length() != 0 && autologin) {
				login(username, password, true);
			}
		}
	}

	public void login(final String username, final String password, final boolean autoLogin) {
		final ProgressDialog loginDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.loggingIn), true);
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		loginDialog.show();

		api.checkLogin(username, password, new FacepunchAPI.LoginCallback() {
			public void onResult(LoginStatus status) {
				loginDialog.dismiss();

				// Prepare frontpage opening intent
				final Intent frontpageIntent = new Intent(LoginActivity.this, FrontpageActivity.class);

				if (status.equals(LoginStatus.LOGIN_OK)) {
					// Ask the user if the successful login details should be
					// saved
					if (autoLogin && !prefs.getBoolean("autologin", false)) {
						//Don't ask if you want to save password if autologin is checked
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("username", username);
						editor.putString("password", password);
						editor.putBoolean("autologin", autoLogin);
						editor.commit();
						
						startActivity(frontpageIntent);
					} else if (!username.equals(prefs.getString("username", "")) || !password.equals(prefs.getString("password", ""))) {
						new AlertDialog.Builder(LoginActivity.this).setMessage(getString(R.string.successfulLoginRememberPassword)).setCancelable(true).setPositiveButton(getString(R.string.rememberPassword), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString("username", username);
								editor.putString("password", password);
								editor.commit();

								startActivity(frontpageIntent);
							}
						}).setNegativeButton(getString(R.string.forgetPassword), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								startActivity(frontpageIntent);
							}
						}).create().show();
					} else {
						startActivity(frontpageIntent);
					}
				} else {
					// Show failed login message
					String failure;
					if (status.equals(LoginStatus.FAIL))
						failure = getString(R.string.failedLogin);
					else if (status.equals(LoginStatus.NO_USERNAME_PASSWORD))
						failure = getString(R.string.failedLoginNoCredentials);
					else
						failure = getString(R.string.failedLoginCredentials);

					new AlertDialog.Builder(LoginActivity.this).setMessage(failure).setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create().show();
				}
			}
		});
	}

	// Save activity state
	@Override
	public Object onRetainNonConfigurationInstance() {
		final State data = new State();

		data.username = ((EditText)findViewById(R.id.usernameField)).getText().toString().trim();
		data.password = ((EditText)findViewById(R.id.passwordField)).getText().toString().trim();
		data.autoLogin = ((CheckBox)findViewById(R.id.passwordField)).isChecked();

		return data;
	}
}