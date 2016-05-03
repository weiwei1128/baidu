package com.flyingtravel.Activity.Spot;

/**
 * Created by Tinghua on 2016/3/7.
 */
public class SpotData {
    private String Name;        //景點店家名稱
    private double Latitude;    //景點店家緯度
    private double Longitude;   //景點店家經度
    private double Distance;    //景點店家距離
    private String Add;        //景點店家名稱
    private String Picture1;
    private String Picture2;
    private String Picture3;
    private String OpenTime;
    private String TicketInfo;
    private String InfoDetail;

    //建立物件時需帶入景點店家名稱、景點店家緯度、景點店家經度
    public SpotData (String name, double latitude, double longitude,
                     String add, String picture1, String picture2, String picture3,
                     String openTime, String ticketInfo ,String infoDetail) {
        //將資訊帶入類別屬性
        Name = name;
        Latitude = latitude;
        Longitude = longitude;
        Add = add;
        Picture1 = picture1;
        Picture2 = picture2;
        Picture3 = picture3;
        OpenTime = openTime;
        TicketInfo = ticketInfo;
        InfoDetail = infoDetail;
    }

    //取得店家名稱
    public String getName() {
        return Name;
    }

    //取得店家緯度
    public double getLatitude() {
        return Latitude;
    }

    //取得店家經度
    public double getLongitude() {
        return Longitude;
    }

    //寫入店家距離
    public void setDistance(double distance) {
        Distance = distance;
    }

    //取得店家距離
    public double getDistance() {
        return Distance;
    }

    public String getAdd() {
        return Add;
    }

    public String getPicture1() {
        return Picture1;
    }

    public String getPicture2() {
        return Picture2;
    }

    public String getPicture3() {
        return Picture3;
    }

    public String getOpenTime() {
        return OpenTime;
    }

    public String getTicketInfo() {
        return TicketInfo;
    }

    public String getInfoDetail() {
        return InfoDetail;
    }
}
