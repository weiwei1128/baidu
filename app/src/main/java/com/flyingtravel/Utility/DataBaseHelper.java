package com.flyingtravel.Utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wei on 2016/1/4.
 * 2016/3/30
 * 修改後記得更新資料庫 避免使用者資料遺失!!
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static DataBaseHelper mInstance = null;
    private static final int VERSION = 3;
    // version 2: news 新增一個column //TODO 尚未測試!
    private static final String DATABASE_NAME = "Travel.db";
    private Context mcontext;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

//    public DataBaseHelper(Context context){
//        super(context,DATABASE_NAME,null,VERSION);
//        this.mcontext = context;
//
//    }

    public static synchronized DataBaseHelper getmInstance(Context ctx) {
        //make sure do not accidentally leak Activity's context.
        if (mInstance == null) {
            mInstance = new DataBaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //會員資料
        String DATABASE_CREATE_TABLE_MEMBER = "create table member("
                + "_ID INTEGER PRIMARY KEY," + "account TEXT,"
                + "password TEXT,"
                + "name TEXT,"
                + "phone TEXT,"
                + "email TEXT,"
                + "addr TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_MEMBER);

        //景點資訊 偷偷記錄位置
        String DATABASE_CREATE_TABLE_LOCATION = "create table location("
                + "_ID INTEGER PRIMARY KEY," + "CurrentLat BLOB,"
                + "CurrentLng BLOB"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_LOCATION);

        //景點資訊 原始資料
        String DATABASE_CREATE_TABLE_SPOTDATA_RAW = "create table spotDataRaw("
                + "_ID INTEGER PRIMARY KEY," + "spotName TEXT,"
                + "spotAdd TEXT,"
                + "spotLat BLOB,"
                + "spotLng BLOB,"
                + "picture1 TEXT,"
                + "picture2 TEXT,"
                + "picture3 TEXT,"
                + "openTime TEXT,"
                + "ticketInfo TEXT,"
                + "infoDetail TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_RAW);

        //景點列表 排序過後
        String DATABASE_CREATE_TABLE_SPOTDATA_SORTED = "create table spotDataSorted("
                + "_ID INTEGER PRIMARY KEY," + "spotName TEXT,"
                + "spotAdd TEXT,"
                + "spotLat BLOB,"
                + "spotLng BLOB,"
                + "picture1 TEXT,"
                + "picture2 TEXT,"
                + "picture3 TEXT,"
                + "openTime TEXT,"
                + "ticketInfo TEXT,"
                + "infoDetail TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_SORTED);

        //軌跡紀錄
        String DATABASE_CREATE_TABLE_TRACKROUTE = "create table trackRoute("
                + "_ID INTEGER PRIMARY KEY," + "routesCounter INTEGER,"
                + "track_no INTEGER,"
                + "track_lat BLOB,"
                + "track_lng BLOB,"
                + "track_start INTEGER,"
                + "track_title TEXT,"
                + "track_totaltime TEXT,"
                + "track_completetime TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_TRACKROUTE);

        //旅遊日誌
        String DATABASE_CREATE_TABLE_TRAVELMEMO = "create table travelMemo("
                + "_ID INTEGER PRIMARY KEY," + "memo_routesCounter INTEGER,"
                + "memo_trackNo INTEGER,"
                + "memo_content TEXT,"
                + "memo_img BLOB,"
                + "memo_latlng BLOB,"
                + "memo_time TEXT,"
                + "memo_imgUrl TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_TRAVELMEMO);

/*        //旅遊日誌 日誌列表 同步Server的版本
        String DATABASE_CREATE_TABLE_TRAVELMEMO = "create table travelMemo("
                +"_ID INTEGER PRIMARY KEY,"+"totalCount TEXT,"
                +"id TEXT,"
                +"title TEXT,"
                +"url TEXT,"
                +"zhaiyao TEXT,"
                +"click TEXT,"
                +"addtime TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_TRAVELMEMO);
*/
        //伴手禮
        String DATABASE_CREATE_TABLE_GOODS = "create table goods("
                + "_ID INTEGER PRIMARY KEY," + "totalCount TEXT,"
                + "goods_id TEXT,"
                + "goods_title TEXT,"
                + "goods_url TEXT,"
                + "goods_money TEXT,"
                + "goods_content TEXT,"
                + "goods_click TEXT,"
                + "goods_addtime TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_GOODS);
        //0324 伴手禮小項目
        String DATABASE_CREATE_TABLE_GOODSITEM = "create table goodsitem("
                + "_ID INTEGER PRIMARY KEY," + "goods_bigid TEXT,"
                + "goods_itemid TEXT,"
                + "goods_title TEXT,"
                + "goods_money TEXT,"
                + "goods_url TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_GOODSITEM);

        //即時好康
        String DATABASE_CREATE_TABLE_SPECIAL = "create table special_activity("
                + "_ID INTEGER PRIMARY KEY," + "special_id TEXT,"
                + "title TEXT,"
                + "img TEXT,"
                + "content TEXT,"
                + "price TEXT,"
                + "click TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_SPECIAL);

        //最新消息
        String DATABASE_CREATE_TABLE_NEWS = "create table news("
                + "_ID INTEGER PRIMARY KEY," + "title TEXT,"
                +"link TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_NEWS);

        String DATABASE_CREATE_TABLE_BANNER = "create table banner("
                + "_ID INTEGER PRIMARY KEY," + "img_url TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_BANNER);

        String DATABASE_CREATE_TABLE_SHOPRECORD = "create table shoporder("
                + "_ID INTEGER PRIMARY KEY," + "order_id TEXT,"
                + "order_userid TEXT,"
                + "order_no TEXT,"
                + "order_time TEXT,"
                + "order_name TEXT,"
                + "order_phone TEXT,"
                + "order_email TEXT,"
                + "order_money TEXT,"
                + "order_state TEXT,"
                + "order_schedule INTEGER DEFAULT 0"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_SHOPRECORD);


        System.out.println("database CREATE");
    }
    private static final String DATABASE_ALTER_TEAM_1 = "ALTER TABLE "
            + "news" + " ADD COLUMN " + "link" + " TEXT;";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /** TODO 正式版已修改!!
         * [0425] [DATABASE_ALTER_TEAM_1] [新增一個table]
         * **/
        if(oldVersion<2)
            db.execSQL(DATABASE_ALTER_TEAM_1);

    }

}
