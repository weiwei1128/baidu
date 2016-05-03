package com.flyingtravel.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.flyingtravel.Activity.Spot.SpotDetailActivity;
import com.flyingtravel.Adapter.SpotListAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

/**
 * Created by Tinghua on 2016/3/25.
 */
public class SpotListViewFragment extends Fragment {

    public static final String TAG = SpotListViewFragment.class.getSimpleName();
    public static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    public static final String PAGE_NO = "PAGE_NO";
    private String mFragmentName;
    private int mPageNo;

    private SearchView search;

/*
    public static FrameLayout spotList_searchLayout;
    private EditText SearchEditText;
    private ImageView SearchImg;
*/
    private SpotListAdapter adapter;
    private ListView mlistView;

    public SpotListViewFragment () {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragementName Parameter 1.
     * @param pageNo Parameter 2.
     * @return A new instance of fragment RecordDiaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpotListViewFragment newInstance(String fragementName, int pageNo) {
        SpotListViewFragment fragment = new SpotListViewFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_NAME, fragementName);
        args.putInt(PAGE_NO, pageNo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentName = getArguments().getString(FRAGMENT_NAME);
            mPageNo = getArguments().getInt(PAGE_NO);
        }
        //Log.e("3/27_", "SpotListViewFragment. onCreate pageNo" + mPageNo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_listview, container, false);

        mlistView = (ListView) view.findViewById(R.id.spotlist_listView);

        //int index = SpotListFragment.viewPager.getCurrentItem();
        adapter = new SpotListAdapter(getActivity(), mPageNo);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new itemListener());

        search = (SearchView) view.findViewById(R.id.searchView);
        search.setQueryHint(getContext().getResources().getString(R.string.InputSpotName_text));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int index = SpotListFragment.viewPager.getCurrentItem();
                adapter = new SpotListAdapter(getActivity(), (index+1));
                mlistView.setAdapter(adapter);
                adapter.getFilter().filter(newText.toString());
                //Log.e("4/1_", "搜尋: " + newText.toString());
                return true;
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        //Log.e("4/1_SpotListView", "onDestroyView");
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        //Log.e("4/1_SpotListView", "onLowMemory");
        System.gc();
        super.onLowMemory();
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (mPageNo - 1) * 10 + position);
            Functions.go(false, getActivity(), getContext(), SpotDetailActivity.class, bundle);
        }
    }
}
