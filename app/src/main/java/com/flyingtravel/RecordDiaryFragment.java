package com.flyingtravel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Adapter.RecordDiaryFragmentAdapter;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordDiaryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordDiaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordDiaryFragment extends Fragment {
    public static final String TAG = RecordDiaryFragment.class.getSimpleName();

    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private TextView DateOfLastOne;
    private ListView mlistView;
    public static RecordDiaryFragmentAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    public RecordDiaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragementName Parameter 1.
     * @return A new instance of fragment RecordDiaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordDiaryFragment newInstance(String fragementName) {
        RecordDiaryFragment fragment = new RecordDiaryFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_NAME, fragementName);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentName = getArguments().getString(FRAGMENT_NAME);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
//        Log.e("3/27_", "RecordDiaryFragment. onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record_diary, container, false);
        DateOfLastOne = (TextView) rootView.findViewById(R.id.LastOneDate);
        mlistView = (ListView) rootView.findViewById(R.id.diary_listView);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToLast();
                String dateString = trackRoute_cursor.getString(7);
                DateOfLastOne.setText(dateString);
            } else {
                SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date=new Date();
                DateOfLastOne.setText(DateFormat.format(date));
            }
            trackRoute_cursor.close();
        }

        mAdapter = new RecordDiaryFragmentAdapter(getActivity());
//        Log.e("3/27_", "RecordDiaryFragment. onActivityCreated");
    }

    @Override
    public void onDestroyView() {
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
//        Log.e("3/23_SpotMap", "onLowMemory");
        System.gc();
        super.onLowMemory();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //you are visible to user now - so set whatever you need
//            Log.e("3/23_SpotMap", "setUserVisibleHint: Visible");
            if (mlistView != null) {
                mlistView.setAdapter(mAdapter);
                mlistView.setOnItemClickListener(new itemListener());
                if (mAdapter.getCount() == 0)
                    Toast.makeText(getActivity(), getContext().getResources().getString(R.string.noUpload_text), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            //you are no longer visible to the user so cleanup whatever you need
//            Log.e("3/23_SpotMap", "setUserVisibleHint: not Visible");
            System.gc();
        }
    }

    private class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", position);
            Functions.go(false, getActivity(), getContext(), RecordDiaryDetailActivity.class, bundle);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
    }
}
