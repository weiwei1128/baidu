package com.flyingtravel.Fragment;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.Activity.Buy.BuyActivity;
import com.flyingtravel.Activity.CheckScheduleActivity;
import com.flyingtravel.Activity.ServiceActivity;
import com.flyingtravel.Activity.Special.SpecialActivity;
import com.flyingtravel.Activity.Spot.SpotActivity;
import com.flyingtravel.Activity.WebviewActivity;
import com.flyingtravel.ImageSlide.MainImageFragment;
import com.flyingtravel.R;
import com.flyingtravel.RecordActivity;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.Utility.View.MyTextview2;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    LinearLayout linearLayout, buyLayout, spotLayout, recordLayout, scheduleLayout,
            serviceLayout, goodthingLayout;
    private Fragment contentFragment;
    MainImageFragment homefragment;
    Context context;
    Boolean ifStop = false;
    //    MyTextview news;
    MyTextview2 news;
    Bundle getSavedInstanceState;
    /**
     * GA
     **/
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void onResume() {
//        Log.e("3.22", "=========Main onResume");
        if (getNewsBroadcast == null) {
            context.registerReceiver(getNewsBroadcast, new IntentFilter("news"));
            context.registerReceiver(getNewsBroadcast, new IntentFilter("banner"));
        }
        String message = getContext().getResources().getString(R.string.loading_text);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        DataBaseHelper helper = DataBaseHelper.getmInstance(context);
        final SQLiteDatabase database = helper.getWritableDatabase();
        final Cursor news_cursor = database.query("news", new String[]{"title", "link"}, null, null, null, null, null);
        if (news_cursor != null && news_cursor.getCount() > 0) {
            while (news_cursor.moveToNext()) {
                final String setup = news_cursor.getString(0);
                final String link = news_cursor.getString(1);
                spannableStringBuilder.append(setup);
                spannableStringBuilder.setSpan(new ClickableSpan() {
                    @SuppressLint("JavascriptInterface")
                    @Override
                    public void onClick(View widget) {
//                        Log.d("4.14","onClick!");
                        tracker.send(new HitBuilders.EventBuilder().setCategory("最新消息-ID:" + setup)
//                .setAction("click")
//                .setLabel("submit")
                                .build());
                        Bundle bundle = new Bundle();
                        bundle.putString("NewsLink", link);
                        Functions.go(false, MainFragment.this.getActivity(), context,
                                WebviewActivity.class,
                                bundle
                        );
                    }
                }, spannableStringBuilder.length() - news_cursor.getString(0).length(), spannableStringBuilder.length(), 0);
                spannableStringBuilder.append("     ");
            }
            news.setMovementMethod(LinkMovementMethod.getInstance());
            news.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);

        } else
            news.setText(message);
        if (news_cursor != null)
            news_cursor.close();

        super.onResume();
        /**GA**/
//        Log.i("5.6","======Resume=====");
        tracker.setScreenName("主頁");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        /**GA**/
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
//        Log.e("3.22","=========Main onDestroy");
        if (getNewsBroadcast != null)
            context.unregisterReceiver(getNewsBroadcast);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getActivity().getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/

        this.context = getActivity();
        this.getSavedInstanceState = savedInstanceState;
        context.registerReceiver(getNewsBroadcast, new IntentFilter("news"));
        context.registerReceiver(getNewsBroadcast, new IntentFilter("banner"));
        FragmentManager fragmentManager = getChildFragmentManager();
        if (getSavedInstanceState != null) {
            if (fragmentManager.findFragmentByTag(MainImageFragment.ARG_ITEM_ID) != null) {
                homefragment = (MainImageFragment) fragmentManager
                        .findFragmentByTag(MainImageFragment.ARG_ITEM_ID);
                contentFragment = homefragment;
            }
        } else {
            homefragment = new MainImageFragment();
            switchContent(homefragment, MainImageFragment.ARG_ITEM_ID);
        }

    }

    ////ImageSlide
    public void switchContent(Fragment fragment, String tag) {
//        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentManager fragmentManager = getChildFragmentManager();
        while (fragmentManager.popBackStackImmediate())
            ;

        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager
                    .beginTransaction();
            transaction.replace(R.id.content_frame, fragment, tag);
            // Only ProductDetailFragment is added to the back stack.
            if (!(fragment instanceof MainImageFragment)) {
                transaction.addToBackStack(tag);
            }
            transaction.commit();
            contentFragment = fragment;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.e("3.22", "=========Main onCreateView");
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.main_main_layout);
        UI(view);

//        news = new MyTextview(context);
        news = new MyTextview2(context);
        news.setText(getContext().getResources().getString(R.string.loading_text));
        news.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        linearLayout.addView(news,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        );


        //little trick
        ((LinearLayout.LayoutParams) news.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
        news.startFor0();
        news.setTextColor(Color.BLACK);
        /////跑馬燈
        return view;
    }


    void UI(View view) {

        //Goodthing
        goodthingLayout = (LinearLayout) view.findViewById(R.id.main_good_layout);
        goodthingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, SpecialActivity.class, null);
            }
        });

        //service
        serviceLayout = (LinearLayout) view.findViewById(R.id.main_service_layout);
        serviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, ServiceActivity.class, null);
            }
        });

        spotLayout = (LinearLayout) view.findViewById(R.id.main_spot_layout);
        spotLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, SpotActivity.class, null);
            }
        });

        recordLayout = (LinearLayout) view.findViewById(R.id.main_record_layout);
        recordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, RecordActivity.class, null);
            }
        });

        buyLayout = (LinearLayout) view.findViewById(R.id.main_buy_layout);
        buyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, BuyActivity.class, null);
            }
        });

        scheduleLayout = (LinearLayout) view.findViewById(R.id.main_schedule_layout);
        scheduleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, CheckScheduleActivity.class, null);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (contentFragment instanceof MainImageFragment) {
            outState.putString("content", MainImageFragment.ARG_ITEM_ID);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        ifStop = true;
    }

    /**
     * 接收下載成功的資料
     **/
    private BroadcastReceiver getNewsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent != null) {
                if (intent.getBooleanExtra("news", false)) {
                    DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                    SQLiteDatabase database = helper.getWritableDatabase();
                    String message = getContext().getResources().getString(R.string.loading_text);
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    final Cursor news_cursor = database.query("news", new String[]{"title", "link"}, null, null, null, null, null);
                    if (news_cursor != null && news_cursor.getCount() > 0) {
                        while (news_cursor.moveToNext()) {
                            final String setup = news_cursor.getString(0);
                            final String link = news_cursor.getString(1);
                            spannableStringBuilder.append(setup);
                            spannableStringBuilder.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("NewsLink", link);
                                    Functions.go(false, MainFragment.this.getActivity(), context,
                                            WebviewActivity.class,
                                            bundle
                                    );
                                }
                            }, spannableStringBuilder.length() - news_cursor.getString(0).length(), spannableStringBuilder.length(), 0);
                            spannableStringBuilder.append("     ");
                        }
                        news.setMovementMethod(LinkMovementMethod.getInstance());
                        news.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);

                    } else
                        news.setText(message);
                    if (news_cursor != null)
                        news_cursor.close();

                }
                if (intent.getBooleanExtra("banner", false) && !ifStop) {
                    FragmentManager fragmentManager = getChildFragmentManager();
                    if (getSavedInstanceState != null) {
                        if (fragmentManager.findFragmentByTag(MainImageFragment.ARG_ITEM_ID) != null) {
                            homefragment = (MainImageFragment) fragmentManager
                                    .findFragmentByTag(MainImageFragment.ARG_ITEM_ID);
                            contentFragment = homefragment;
                        }
                    } else {
                        homefragment = new MainImageFragment();
                        switchContent(homefragment, MainImageFragment.ARG_ITEM_ID);
                    }
                }
            }
        }
    };


}
