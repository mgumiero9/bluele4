package br.com.carenet.a4o_v011.ui.fragments.basic;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.viewpagerindicator.CirclePageIndicator;

import br.com.carenet.a4o_v011.R;
import br.com.carenet.a4o_v011.ui.fragments.ConfirmationDialogFragment;

import static br.com.carenet.a4o_v011.utils.LogUtils.makeLogTag;

/**
 * Created by dennis on 02.09.14.
 */
public class TutorialFragment extends Fragment
{
	public static final String TAG = makeLogTag(TutorialFragment.class);

	public static final String ARG_TITLE = TutorialFragment.class.getName() + ".ARG_TITLE";
	public static final String ARG_IMAGES = TutorialFragment.class.getName() + ".ARG_IMAGES";
	public static final String ARG_PREFS_VAR = TutorialFragment.class.getName() + ".ARG_PREFS_VAR";

	public static TutorialFragment makeInstance(String title, int [] images, String prefsVar)
	{
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putIntArray(ARG_IMAGES, images);
		args.putString(ARG_PREFS_VAR, prefsVar);
		TutorialFragment frag = new TutorialFragment();
		frag.setArguments(args);
		return frag;
	}

	private ViewPager mPager;
	private CirclePageIndicator indicator;
	private TutorialAdapter tutorialAdapter;
	private SharedPreferences prefs;
	private int lastIndex;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		tutorialAdapter = new TutorialAdapter(getChildFragmentManager(), savedInstanceState
			, getArguments().getIntArray(ARG_IMAGES));
		mPager = (ViewPager) view.findViewById(R.id.pager);
		mPager.setAdapter(tutorialAdapter);
		mPager.setCurrentItem(lastIndex = prefs.getInt(getArguments().getString(ARG_PREFS_VAR), 0));

		view.findViewById(R.id.btnCancel).setOnClickListener(btnClickListener);
		view.findViewById(R.id.btnNext).setOnClickListener(btnClickListener);

		indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		mPager.setOnPageChangeListener(tutorialAdapter);

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	private View.OnClickListener btnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			switch (view.getId())
			{
				/*case DRV_R.id.btnBack:
					mPager.setCurrentItem(--lastIndex < 0 ? 0 : lastIndex);
					break;*/
				case R.id.btnCancel:
				{
					ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
					Bundle args = new Bundle();
					args.putString(ConfirmationDialogFragment.PARAM_TITLE, getString(R.string.title_info));
					args.putString(ConfirmationDialogFragment.PARAM_MESSAGE, getString(R.string.tutorial_cancelation_info));
					args.putString(ConfirmationDialogFragment.PARAM_POS_BTN_TEXT, getString(android.R.string.ok));
					args.putString(ConfirmationDialogFragment.PARAM_NEG_BTN_TEXT, getString(android.R.string.cancel));
					dialog.setArguments(args);
					dialog.setListener(new ConfirmationDialogFragment.ConfirmationDialogListener()
					{
						@Override
						public void onDialogPositiveClick(DialogFragment dialog)
						{
							prefs.edit().putInt(getArguments().getString(ARG_PREFS_VAR)
								, getArguments().getIntArray(ARG_IMAGES).length).commit();
							getActivity().getSupportFragmentManager().popBackStack();
						}

						@Override
						public void onDialogNegativeClick(DialogFragment dialog)
						{

						}
					});
					dialog.show(getActivity().getSupportFragmentManager(), "dialog");
				}
				break;
				case R.id.btnNext:
				{
					if (lastIndex < tutorialAdapter.getCount() - 1)
						mPager.setCurrentItem(++lastIndex);
					else
						getActivity().getSupportFragmentManager().popBackStack();
				} break;
			}
		}
	};

	private class TutorialAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener
	{
		private int[] SCREENS;

		public TutorialAdapter(FragmentManager fm, Bundle saveState, int[] imagesIds) {
			super(fm);
			this.SCREENS = imagesIds;
		}

		@Override
		public int getCount() {
			return SCREENS.length;
		}

		@Override
		public Fragment getItem(int position) {
			return TutorialScreenFragment.newInstance(SCREENS[position]);
		}

		@Override
		public void onPageScrolled(int i, float v, int i2) {
			indicator.onPageScrolled(i, v, i2);
		}

		@Override
		public void onPageSelected(int i) {
			lastIndex = i;
			prefs.edit().putInt(getArguments().getString(ARG_PREFS_VAR), lastIndex).commit();
			indicator.onPageSelected(i);
		}

		@Override
		public void onPageScrollStateChanged(int i)
		{
			indicator.onPageScrollStateChanged(i);
		}
	}

	public static class TutorialScreenFragment extends Fragment
	{

		int drawableId;

		static public TutorialScreenFragment newInstance(int drawableId) {
			TutorialScreenFragment fragment = new TutorialScreenFragment();
			Bundle args = new Bundle();
			args.putInt("image", drawableId);
			fragment.setArguments(args);
			return fragment;
		}

		public TutorialScreenFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (getArguments() != null) {
				drawableId = getArguments().getInt("image");
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			ImageView view = new ImageView(getActivity());
			view.setImageResource(drawableId);
			view.setScaleType(ImageView.ScaleType.FIT_CENTER);
			return view;
		}

	}

}
