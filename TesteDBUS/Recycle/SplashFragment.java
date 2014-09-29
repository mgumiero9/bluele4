package br.com.carenet.a4o_v011.ui.fragments.basic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.carenet.a4o_v011.R;
import br.com.carenet.a4o_v011.ui.activities.MainActivity;

import static br.com.carenet.a4o_v011.utils.LogUtils.makeLogTag;

/**
 * Created by dennis on 02.09.14.
 */
public class SplashFragment extends Fragment
{
	public static final String TAG = makeLogTag(SplashFragment.class);

	private CompletionTask completionTask;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_splash, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		completionTask = new CompletionTask();
		completionTask.execute();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((ActionBarActivity)getActivity()).getSupportActionBar().hide();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		((ActionBarActivity)getActivity()).getSupportActionBar().show();

		if (completionTask != null) {
			completionTask.cancel(true);
		}
	}

	class CompletionTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... voids)
		{
			SystemClock.sleep(2000);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid)
		{
			if (!isCancelled())
			{
				((MainActivity)getActivity()).onSplashCompleted();
			}
		}
	}
}
