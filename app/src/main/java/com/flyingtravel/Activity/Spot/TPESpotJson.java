package com.flyingtravel.Activity.Spot;

/**
 * Created by Tinghua on 1/25/2016.
 * Taipei景點資訊
 */
public class TPESpotJson {

    public PostResult result;

    public TPESpotJson() {

    }

    public void setResult(PostResult result)
    {
        this.result = result;
    }

    public PostResult getResult()
    {
        return result;
    }

    public class PostResult {

        public PostResults[] results;

        public PostResult() {

        }

        public void setResults(PostResults[] results)
        {
            this.results = results;
        }

        public PostResults[] getResults()
        {
            return results;
        }


        public class PostResults {

            private Integer _id;
            private String stitle;
            private String MEMO_TIME;
            private String xbody;
            private String address;
            private String file;
            private String longitude;
            private String latitude;

            public PostResults() {

            }

            public void setID(Integer _id)
            {
                this._id = _id;
            }

            public Integer getID()
            {
                return _id;
            }

            public void setStitle(String stitle)
            {
                this.stitle = stitle;
            }

            public String getStitle()
            {
                return stitle;
            }

            public void setMemoTime(String MEMO_TIME)
            {
                this.MEMO_TIME = MEMO_TIME;
            }

            public String getMemoTime()
            {
                return MEMO_TIME;
            }

            public void setXbody(String xbody)
            {
                this.xbody = xbody;
            }

            public String getXbody()
            {
                return xbody;
            }

            public void setAddress(String address)
            {
                this.address = address;
            }

            public String getAddress()
            {
                return address;
            }

            public void setFile(String file)
            {
                this.file = file;
            }

            public String getFile()
            {
                return file;
            }

            public void setLongitude(String longitude)
            {
                this.longitude = longitude;
            }

            public String getLongitude()
            {
                return longitude;
            }

            public void setLatitude(String latitude)
            {
                this.latitude = latitude;
            }

            public String getLatitude()
            {
                return latitude;
            }
        }
    }
}
