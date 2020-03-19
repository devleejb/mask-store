package com.devleejb.maskstore.client;

public class Store implements Comparable<Store> {
    public String addr;
    public String code;
    public String created_at;
    public double lat;
    public double lng;
    public String name;
    public String remain_stat;
    public String stock_at;
    public String type;
    public int distance;

    @Override
    public int compareTo(Store o) {
        return distance - o.distance;
    }
}
