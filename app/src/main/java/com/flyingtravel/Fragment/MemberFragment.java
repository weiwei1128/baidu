package com.flyingtravel.Fragment;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MemberFragment extends Fragment {
    Context context;
    TextView NameText, PhoneText, EmailText, AddrText;
    LinearLayout logoutLayout, shareLayout,ratingLayout;

    public MemberFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.member_activity, container, false);
        memberData(view);
        return view;
    }

    public void memberData(View view) {
        logoutLayout = (LinearLayout) view.findViewById(R.id.member_logout_layout);
        shareLayout = (LinearLayout) view.findViewById(R.id.member_share_layout);
        ratingLayout = (LinearLayout)view.findViewById(R.id.member_value_layout);
        NameText = (TextView) view.findViewById(R.id.member_name_text);
        PhoneText = (TextView) view.findViewById(R.id.member_phone_text);
        EmailText = (TextView) view.findViewById(R.id.member_email_text);
        AddrText = (TextView) view.findViewById(R.id.member_addr_text);
        Boolean login = false;
        DataBaseHelper helper = DataBaseHelper.getmInstance(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
//            Log.d("2.26", "DB " + member_cursor.getString(2));
            NameText.setText(member_cursor.getString(2));
            PhoneText.setText(member_cursor.getString(3));
            EmailText.setText(member_cursor.getString(4));
            AddrText.setText(member_cursor.getString(5));
            login = true;
        }
        if (member_cursor != null)
            member_cursor.close();
        if (database.isOpen())
            database.close();
        //=======Logout=======//


        final Boolean finalLogin = login;
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalLogin) {
                    //表示登入過了!
                    // 創建退出對話框
                    AlertDialog isExit = new AlertDialog.Builder(context).create();
                    // 設置對話框標題
                    isExit.setTitle(getContext().getResources().getString(R.string.systemMessage_text));
                    // 設置對話框消息
                    isExit.setMessage(getContext().getResources().getString(R.string.logoutLeave_text));
                    // 添加選擇按鈕並注冊監聽
                    isExit.setButton(getContext().getResources().getString(R.string.ok_text), listener);
                    isExit.setButton2(getContext().getResources().getString(R.string.cancel_text), listener);
                    // 顯示對話框
                    isExit.show();
                }
            }
        });
        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                //drawable -> bitmap
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_512);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//                InputStream inputStream = getResources().openRawResource(R.drawable.icon_512);
                byte buf[] = new byte[1024];
                int len = 0;

                String path = Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg";
                File f = new File(path);
                try {
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
//                    while ((len = inputStream.read(buf)) > 0)
//                        fo.write(buf);
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
//                    Log.d("4.18", "error" + e.toString());
                }
                /*
                * try
    {
    File f=new File("your file name");
    InputStream inputStream = getResources().openRawResource(id);
    OutputStream out=new FileOutputStream(f);
    byte buf[]=new byte[1024];
    int len;
    while((len=inputStream.read(buf))>0)
    out.write(buf,0,len);
    out.close();
    inputStream.close();
    }
    catch (IOException e){}
    }
                * */
                //setting share information
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.title_text));
                sharingIntent.putExtra(Intent.EXTRA_TEMPLATE,"testtt");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getContext().getResources().getString(R.string.title_text));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.flyingtravel");

//                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                sharingIntent.setType("image/jpeg");
                sharingIntent.setType("*/*");
//                Log.d("4.18", "path:" + path + " lens: " + len+" bytes"+bytes.size());
//                File file = new File(path);
//                Log.d("4.18", String.valueOf(file.exists()));


//                image/jpeg
                startActivity(Intent.createChooser(sharingIntent, getContext().getResources().getString(R.string.shareto_text)));
            }
        });

    //======rating====//
        ratingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMarket();
            }
        });
    }
    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出程序

                    DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                    SQLiteDatabase database = helper.getWritableDatabase();
                    database.delete("member", null, null);
                    if (database.isOpen())
                        database.close();
                    Intent MyIntent = new Intent(Intent.ACTION_MAIN);
                    MyIntent.addCategory(Intent.CATEGORY_HOME);
                    MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    MyIntent.putExtra("EXIT", true);
//                    MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(MyIntent);
                    getActivity().finish();

                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };
}
