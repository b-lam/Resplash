package com.b_lam.resplash.data.data;

import java.util.List;

public class Historical {

    /**
     "change": 19, // total number of likes for the past 30 days
     "resolution": "days",
     "quantity": 30,
     "values": [
        { "date": "2017-02-07", "value": 2 },
        { "date": "2017-02-08", "value": 0 },
        { "date": "2017-02-09", "value": 2 },
        { "date": "2017-02-10", "value": 0 },
        { "date": "2017-02-11", "value": 0 },
        { "date": "2017-02-12", "value": 0 },
        { "date": "2017-02-13", "value": 0 },
        { "date": "2017-02-14", "value": 1 },
        { "date": "2017-02-15", "value": 3 },
        { "date": "2017-02-16", "value": 0 },
        { "date": "2017-02-17", "value": 1 },
        { "date": "2017-02-18", "value": 0 }
     ] // array of hashes with all the dates requested and the number of new likes for each date
     */

    public int change;
    public String resolution;
    public int quantity;

    public List<Value> values;

    public static class Value {
        public String date;
        public int value;
    }
}
