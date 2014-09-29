package br.com.carenet.a4o_v011.ui.fragments.basic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;

import java.io.IOException;
import java.util.Arrays;

import br.com.carenet.a4o_v011.R;
import br.com.carenet.a4o_v011.ui.activities.MainActivity;
import br.com.carenet.a4o_v011.utils.ActivityHelper;

import static br.com.carenet.a4o_v011.utils.LogUtils.LOGI;
import static br.com.carenet.a4o_v011.utils.LogUtils.LOGV;
import static br.com.carenet.a4o_v011.utils.LogUtils.makeLogTag;

/**
 * Created by dennis on 03.09.14.
 */
public class LoginFragment extends Fragment
	implements Session.StatusCallback, GooglePlayServicesClient.ConnectionCallbacks
		, GooglePlayServicesClient.OnConnectionFailedListener
		, PlusClient.OnAccessRevokedListener
		, View.OnClickListener
{
	public static final String TAG = makeLogTag(LoginFragment.class);

	private UiLifecycleHelper uiHelper;

	/* Request code used to invoke sign in user interactions. */
	private static final int REQUEST_CODE_RESOLVE_ERR = 10000;

	/* Client used to interact with Google APIs. */
	private PlusClient mPlusClient;

	/* Store the connection result from onConnectionFailed callbacks so that we can
	 * resolve them when the user clicks sign-in.
	 */
	private ConnectionResult mConnectionResult;

	// A flag to stop multiple dialogues appearing for the user.
	private boolean mResolveOnFail;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), this);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_login, container, false);

        view.findViewById(R.id.plus_sign_in_button).setOnClickListener(this);
		view.findViewById(R.id.plus_sign_out_button).setOnClickListener(this);
		view.findViewById(R.id.netshoes_sign_in_button).setOnClickListener(this);
		view.findViewById(R.id.cadastre_sign_in_button).setOnClickListener(this);
		view.findViewById(R.id.guest_sign_in_button).setOnClickListener(this);

		// Hide the sign in button, show the sign out buttons.
		view.findViewById(R.id.plus_sign_in_button).setVisibility(View.VISIBLE);
		view.findViewById(R.id.plus_sign_out_button).setVisibility(View.GONE);

		// G+ login
		mPlusClient = new PlusClient.Builder(getActivity(), this, this)
			.setActions("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
			.setScopes(Scopes.PLUS_LOGIN)
			.build();

		// We use mResolveOnFail as a flag to say whether we should trigger
		// the resolution of a connectionFailed ConnectionResult.
		mResolveOnFail = false;

		// Facebook login
		LoginButton authButton = (LoginButton) view.findViewById(R.id.facebook_sign_in_button);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("public_profile", "user_status"));

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception)
	{
		if (state.isOpened()) {
			LOGV(TAG, "Facebook Logged in...");
		} else if (state.isClosed()) {
			LOGV(TAG, "Facebook Logged out...");
		}
	}

	@Override
	public void call(Session session, SessionState state, Exception exception)
	{
		onSessionStateChange(session, state, exception);
	}

	public void onStart()
	{
		super.onStart();
		mPlusClient.connect();
	}

	public void onStop()
	{
		super.onStop();
        mPlusClient.disconnect();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null &&
			(session.isOpened() || session.isClosed()) )
		{
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		LOGV(TAG, "ActivityResult: " + requestCode);
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == Activity.RESULT_OK)
		{
			// If we have a successful result, we will want to be able to
			// resolve any further errors, so turn on resolution with our
			// flag.
			mResolveOnFail = true;
			// If we have a successful result, lets call connect() again. If
			// there are any more errors to resolve we'll get our
			// onConnectionFailed, but if not, we'll get onConnected.
			mPlusClient.connect();
		}
		else if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode != Activity.RESULT_OK)
		{
			// If we've got an error we can't resolve, we're no
			// longer in the midst of signing in, so we can stop
			// the progress spinner.
			// mConnectionProgressDialog.dismiss();
		}
		else uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		LOGV(TAG, "Google+ ConnectionFailed");
		// Most of the time, the connection will fail with a
		// user resolvable result. We can store that in our
		// mConnectionResult property ready for to be used
		// when the user clicks the sign-in button.
		if (result.hasResolution())
		{
			mConnectionResult = result;
			if (mResolveOnFail) {
				// This is a local helper function that starts
				// the resolution of the problem, which may be
				// showing the user an account chooser or similar.
				startResolution();
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		LOGV(TAG, "Google+ connected by " + mPlusClient.getAccountName());

		// Turn off the flag, so if the user signs out they'll have to
		// tap to sign in again.
		mResolveOnFail = false;

		// Hide the progress dialog if its showing.
		//mConnectionProgressDialog.dismiss();

		// Hide the sign in button, show the sign out buttons.
		getView().findViewById(R.id.plus_sign_in_button).setVisibility(View.GONE);
		getView().findViewById(R.id.plus_sign_out_button).setVisibility(View.VISIBLE);

		// Retrieve the oAuth 2.0 access token.
		final Context context = getActivity().getApplicationContext();
		AsyncTask task = new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				String scope = "oauth2:" + Scopes.PLUS_LOGIN;
				try
				{
					// We can retrieve the token to check via
					// tokeninfo or to pass to a service-side
					// application.
					String token = GoogleAuthUtil.getToken(context,
						mPlusClient.getAccountName(), scope);
				}
				catch (UserRecoverableAuthException e)
				{
					// This error is recoverable, so we could fix this
					// by displaying the intent to the user.
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				} catch (GoogleAuthException e)
				{
					e.printStackTrace();
				}
				return null;
			}
		};
		task.execute((Void) null);

		Toast.makeText(getActivity(), "User is connected!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDisconnected()
	{
		LOGV(TAG, "Google+ disconnected");
		// Hide the sign out buttons, show the sign in button.
		getView().findViewById(R.id.plus_sign_in_button).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.plus_sign_out_button).setVisibility(View.GONE);
	}

	@Override
	public void onAccessRevoked(ConnectionResult status)
	{
		LOGV(TAG, "Google+ access revoked");
		// mPlusClient is now disconnected and access has been revoked.
		// We should now delete any data we need to comply with the
		// developer properties. To reset ourselves to the original state,
		// we should now connect again. We don't have to disconnect as that
		// happens as part of the call.
		mPlusClient.connect();

		// Hide the sign out buttons, show the sign in button.
		getView().findViewById(R.id.plus_sign_in_button).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.plus_sign_out_button).setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.plus_sign_in_button:
			{
				LOGV(TAG, "Google+ Tapped sign in");
				if (!mPlusClient.isConnected() && !mPlusClient.isConnecting())
				{
					// Show the dialog as we are now signing in.
					//mConnectionProgressDialog.show();

					// Make sure that we will start the resolution (e.g. fire the
					// intent and pop up a dialog for the user) for any errors
					// that come in.
					mResolveOnFail = true;
					// We should always have a connection result ready to resolve,
					// so we can start that process.
					if (mConnectionResult != null)
					{
						startResolution();
					} else
					{
						// If we don't have one though, we can start connect in
						// order to retrieve one.
						mPlusClient.connect();
					}
				}
			}
			break;
			case R.id.plus_sign_out_button:
			{
				LOGV(TAG, "Google+ Tapped sign out");
				// We only want to sign out if we're connected.
				if (mPlusClient.isConnected())
				{
					// Clear the default account in order to allow the user
					// to potentially choose a different account from the
					// account chooser.
					mPlusClient.clearDefaultAccount();

					// Disconnect from Google Play Services, then reconnect in
					// order to restart the process from scratch.
					mPlusClient.disconnect();
					mPlusClient.connect();

					// Hide the sign out buttons, show the sign in button.
					getView().findViewById(R.id.plus_sign_in_button).setVisibility(View.VISIBLE);
					getView().findViewById(R.id.plus_sign_out_button).setVisibility(View.GONE);
				}
			}
			break;
			case R.id.guest_sign_in_button:
			case R.id.netshoes_sign_in_button:
			case R.id.cadastre_sign_in_button:
			{
				((MainActivity)getActivity()).onLoginCompleted();
			} break;
		}
	}

	/**
	 * A helper method to flip the mResolveOnFail flag and start the resolution
	 * of the ConnenctionResult from the failed connect() call.
	 */
	private void startResolution()
	{
		try
		{
			// Don't start another resolution now until we have a
			// result from the activity we're about to start.
			mResolveOnFail = false;
			// If we can resolve the error, then call start resolution
			// and pass it an integer tag we can use to track. This means
			// that when we get the onActivityResult callback we'll know
			// its from being started here.
			mConnectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_ERR);
		}
		catch (IntentSender.SendIntentException e)
		{
			// Any problems, just try to connect() again so we get a new
			// ConnectionResult.
			mPlusClient.connect();
		}
	}


}
