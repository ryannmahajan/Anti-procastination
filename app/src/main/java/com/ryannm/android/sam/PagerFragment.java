package com.ryannm.android.sam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.instabug.library.Instabug;

public class PagerFragment extends Fragment {
    public static final short MAIN = 0;
    private static final Class[] MAIN_CLASSES = new Class[] {PunishmentFragment.class, TaskListFragment.class, LaterScheduleFragment.class};
    private short mScreen;

    public PagerFragment setScreen(short screen) {
        mScreen = screen;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_with_pager, container, false);

        Toolbar mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public CharSequence getPageTitle(int position) {
                if (mScreen==MAIN) {
                    return getResources().getStringArray(R.array.main_screen_labels)[position];
                }
                return null;
            }

            @Override
            public Fragment getItem(int position) {
                try {
                    if (mScreen==MAIN) return (Fragment) MAIN_CLASSES[position].newInstance();
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                return null; // Program must not reach here
            }

            @Override
            public int getCount() {
                if (mScreen==MAIN) {
                    return MAIN_CLASSES.length;
                } else return 0;
            }

            @Override
            public int getItemPosition(Object object) {
                if (object instanceof PunishmentFragment) return POSITION_NONE; // So that, reloads
                else return super.getItemPosition(object);
            }
        });

        final TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab_layout);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(viewPager);

        switch (mScreen) {
            case MAIN:
                setHasOptionsMenu(true);
                break;
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
               /* if (mScreen==MAIN && MAIN_CLASSES[position]==PunishmentFragment.class) {
                    if (MAIN_CLASSES[position] == PunishmentFragment.class && positionOffset == 0)
                        (((FragmentPagerAdapter) viewPager.getAdapter()).getItem(position)).onResume();
                    if (MAIN_CLASSES[position+1] == PunishmentFragment.class && positionOffset != 0)
                        (((FragmentPagerAdapter) viewPager.getAdapter()).getItem(position+1)).onResume();
                } */
                // ((PunishmentFragment.TaskPieChart) viewPager.getChildAt(position).findViewById(R.id.pie_chart_tom)).refresh();
            }

            @Override
            public void onPageSelected(int position) {
                if (mScreen==MAIN && MAIN_CLASSES[position]==PunishmentFragment.class) {
                    // todo see below
                    // ((TaskPieChart) viewPager.getChildAt(position).findViewById(R.id.pie_chart_tom)).refresh();// Isn't finding the pie chart
                    // ((PunishmentFragment) (((FragmentPagerAdapter) viewPager.getAdapter()).getItem(position))).refresh();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setCurrentItem(1);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mScreen==MAIN) inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_button:
                // Open settings
                return true;

            case R.id.feedback_button:
                Instabug.invoke();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
