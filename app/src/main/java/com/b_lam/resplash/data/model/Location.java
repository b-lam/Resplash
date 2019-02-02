package com.b_lam.resplash.data.model;

public class Location {

    /**
     "title": "Kitsuné Café, Montreal, Canada"
     "name": "Kitsuné Café"
     "city": "Montreal",
     "country": "Canada",
     "position": {
        "latitude": 45.4732984,
        "longitude": -73.6384879
     }
     */

    public String title;
    public String name;
    public String city;
    public String country;

    public Position position;

    public static class Position {
        public double latitude;
        public double longitude;
    }
}
