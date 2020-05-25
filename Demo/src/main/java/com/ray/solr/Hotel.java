package com.ray.solr;

import org.apache.solr.client.solrj.beans.Field;

/**
 * description:
 * Created by Ray on 2020-05-25
 */
public class Hotel {
    @Field("id")
    private String id;
    @Field("hotelName")
    private String hotelName;
    @Field("address")
    private String address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id='" + id + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
