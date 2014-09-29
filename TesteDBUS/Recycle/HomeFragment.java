package br.com.carenet.a4o_v011.ui.fragments.main;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import br.com.carenet.a4o_v011.R;
import br.com.carenet.a4o_v011.ui.fragments.schedule.EventEditFragment;
import br.com.carenet.a4o_v011.ui.fragments.schedule.EventListFragment;

import static br.com.carenet.a4o_v011.utils.LogUtils.makeLogTag;

/**
 * Created by dennis on 16.09.14.
 */
public class HomeFragment extends Fragment /*implements LoaderManager.LoaderCallbacks<Cursor>*/
{
	public static final String TAG = makeLogTag(HomeFragment.class);

	/*public static final int DATA_DAYS = 0;
	public static final int DATA_EVENTS = 1;*/

	private ViewGroup btnNoAnyEvent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		/*getLoaderManager().initLoader(DATA_DAYS, getArguments(), this);*/
	}

	/*public static class DaysLoader extends CursorLoader
	{
		public DaysLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
		{
			super(context, uri, projection, selection, selectionArgs, sortOrder);
		}
	}

	public static class EventsLoader extends CursorLoader
	{
		private final String dateOfEvent;

		public EventsLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, String dateOfEvent)
		{
			super(context, uri, projection, selection, selectionArgs, sortOrder);
			this.dateOfEvent = dateOfEvent;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		startProgressBar();
		switch (i)
		{
			case DATA_DAYS:
			{
				return null;
			}
			case DATA_EVENTS:
			{
				return null;
			}
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
	{
		endProgressBar();
		if (cursorLoader instanceof DaysLoader)
		{
			if (cursor != null && cursor.moveToFirst())
			{
				btnNoAnyEvent.setVisibility(View.GONE);
				getListView().setVisibility(View.VISIBLE);
			}
			else
			{
				btnNoAnyEvent.setVisibility(View.VISIBLE);
				getListView().setVisibility(View.GONE);
			}
		}
		else if (cursorLoader instanceof EventsLoader)
		{

		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader)
	{
		endProgressBar();
		if (cursorLoader instanceof DaysLoader)
		{
			btnNoAnyEvent.setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
		}
	}*/

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_schedule:
				showSheduleList();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void showSheduleList()
	{
		getActivity().getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.layoutContent, Fragment.instantiate(getActivity(), EventListFragment.class.getName()))
			.addToBackStack(null)
			.commit();
	}


}
