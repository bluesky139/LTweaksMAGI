package li.lingfeng.magi.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buildware.widget.indeterm.IndeterminateCheckBox;

import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Constructor;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;
import li.lingfeng.magi.R;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

/**
 * Created by lilingfeng on 2017/6/23.
 */

public class ListCheckActivity extends FragmentActivity {

    public interface OnItemClickListener {
        void onItemClick(DataProvider.ListItem item);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(DataProvider.ListItem item, Boolean isChecked);
    }

    public static abstract class DataProvider implements OnItemClickListener, OnCheckedChangeListener {

        public class ListItem {
            public Object mData;
            public Drawable mIcon;
            public CharSequence mTitle;
            public CharSequence mDescription;
            public Boolean mChecked; // null is indeterminate

            public <T> T getData(Class<T> cls) {
                return cls.cast(mData);
            }

            @Override
            public String toString() {
                return mData.toString();
            }
        }

        protected ListCheckActivity mActivity;

        public DataProvider(ListCheckActivity activity) {
            mActivity = activity;
        }

        protected abstract String getActivityTitle();
        protected abstract String[] getTabTitles();
        protected abstract int getListItemCount(int tab);
        protected abstract ListItem getListItem(int tab, int position);
        protected abstract boolean reload(); // reload to refresh data list for listview.

        protected boolean hideCheckBox() {
            return false;
        }

        protected boolean linkItemClickToCheckBox() {
            return true;
        }

        @Override
        public void onItemClick(ListItem item) {
        }

        @Override
        public void onCheckedChanged(ListItem item, Boolean isChecked) {
        }

        protected boolean allowMove() {
            return false;
        }

        protected boolean allowSwipe() {
            return false;
        }

        protected void onMove(int fromPosition, int toPosition) {
            throw new NotImplementedException(getClass() + ".onMove()");
        }

        protected void onSwiped(int position) {
            throw new NotImplementedException(getClass() + ".onSwiped()");
        }

        protected void notifyDataSetChanged() {
            mActivity.notifyDataSetChanged();
        }

        protected void onCreateOptionsMenu(Menu menu) {
        }

        protected void onOptionsItemSelected(MenuItem item) {
        }
    }

    protected DataProvider mDataProvider;
    private ListFragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private PagerTabStrip mTabLayout;

    public static void create(Activity activity, Class<? extends DataProvider> clsDataProvider) {
        Intent intent = new Intent(activity, ListCheckActivity.class);
        intent.putExtra("data_provider", clsDataProvider);
        activity.startActivity(intent);
    }

    public static void create(Activity activity, Class<? extends DataProvider> clsDataProvider, Bundle extra) {
        Intent intent = new Intent(activity, ListCheckActivity.class);
        intent.putExtra("data_provider", clsDataProvider);
        intent.putExtras(extra);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Constructor constructor = getDataProviderClass().getConstructor(ListCheckActivity.class);
            mDataProvider = (DataProvider) constructor.newInstance(this);
        } catch (Exception e) {
            Logger.e("No data provider, " + e);
            Logger.stackTrace(e);
            finish();
            return;
        }

        setTitle(mDataProvider.getActivityTitle());
        setContentView(R.layout.activity_list_check);
        mTabLayout = (PagerTabStrip) findViewById(R.id.tabs);
        mPagerAdapter = new ListFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ListFragmentPageChangeListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mDataProvider.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDataProvider.onOptionsItemSelected(item);
        return true;
    }

    protected Class<? extends DataProvider> getDataProviderClass() {
        return (Class<? extends DataProvider>) getIntent().getSerializableExtra("data_provider");
    }

    private void notifyDataSetChanged() {
        for (int i = 0; i < mPagerAdapter.getCount(); ++i) {
            ListFragment fragment = (ListFragment) ViewUtils.findFragmentByPosition(
                    getSupportFragmentManager(), mViewPager, i);
            if (fragment != null) {
                fragment.notifyListChanged();
            }
        }
    }

    private class ListFragmentPagerAdapter extends FragmentPagerAdapter {

        public ListFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDataProvider.getTabTitles()[position];
        }

        @Override
        public Fragment getItem(int position) {
            return ListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mDataProvider.getTabTitles().length;
        }
    }

    private class ListFragmentPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mDataProvider.reload()) {
                notifyDataSetChanged();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public static class ListFragment extends Fragment {

        private RecyclerView mListView;
        private ListAdapter mListAdapter;
        private ItemTouchHelper mItemTouchHelper;

        public static ListFragment newInstance(int tab) {
            ListFragment fragment = new ListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("tab", tab);
            fragment.setArguments(bundle);
            return fragment;
        }

        public int getTab() {
            return getArguments().getInt("tab");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_list_check, container, false);
            mListView = (RecyclerView) view.findViewById(R.id.list);
            mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mListAdapter = new ListAdapter();
            mListView.setAdapter(mListAdapter);

            if (getDataProvider().allowMove() || getDataProvider().allowSwipe()) {
                mItemTouchHelper = new ItemTouchHelper(new ItemTouchCallback());
                mItemTouchHelper.attachToRecyclerView(mListView);
            }
            return view;
        }

        private DataProvider getDataProvider() {
            return ((ListCheckActivity) getActivity()).mDataProvider;
        }

        private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

            private ListCheckActivity getActivity() {
                return (ListCheckActivity) ListFragment.this.getActivity();
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(getActivity())
                        .inflate(R.layout.list_check, parent, false));
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, int position) {
                final DataProvider.ListItem data = getDataProvider().getListItem(getTab(), position);
                holder.mIcon.setImageDrawable(data.mIcon);
                holder.mTitle.setText(data.mTitle);
                holder.mDescription.setText(data.mDescription);

                if (getDataProvider().hideCheckBox()) {
                    holder.mEnabler.setVisibility(View.GONE);
                } else {
                    holder.mEnabler.setOnStateChangedListener(null);
                    holder.mEnabler.setState(data.mChecked);
                    holder.mEnabler.setOnStateChangedListener(new IndeterminateCheckBox.OnStateChangedListener() {
                        @Override
                        public void onStateChanged(IndeterminateCheckBox checkBox, @Nullable Boolean state) {
                            getDataProvider().onCheckedChanged(data, state);
                        }
                    });
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!getDataProvider().hideCheckBox() && getDataProvider().linkItemClickToCheckBox()) {
                            holder.mEnabler.toggle();
                        } else {
                            getDataProvider().onItemClick(data);
                        }
                    }
                });

            }

            @Override
            public int getItemCount() {
                return getDataProvider().getListItemCount(getTab());
            }

            class ViewHolder extends RecyclerView.ViewHolder {

                public ImageView mIcon;
                public TextView mTitle;
                public TextView mDescription;
                public IndeterminateCheckBox mEnabler;

                public ViewHolder(View view) {
                    super(view);
                    mIcon = (ImageView) view.findViewById(R.id.icon);
                    mTitle = (TextView) view.findViewById(R.id.title);
                    mDescription = (TextView) view.findViewById(R.id.description);
                    mEnabler = (IndeterminateCheckBox) view.findViewById(R.id.enabler);
                }
            }
        }

        private class ItemTouchCallback extends ItemTouchHelper.Callback {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(getDataProvider().allowMove() ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0,
                        getDataProvider().allowSwipe() ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT : 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                getDataProvider().onMove(from, to);
                mListAdapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                getDataProvider().onSwiped(position);
                mListAdapter.notifyItemRemoved(position);
            }
        }

        public void notifyListChanged() {
            mListAdapter.notifyDataSetChanged();
        }
    }
}
