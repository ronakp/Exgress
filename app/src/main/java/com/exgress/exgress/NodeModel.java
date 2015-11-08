package com.exgress.exgress;

/**
 * Created by Not-A-Mac on 11/7/2015.
 */
public class NodeModel {
    public String name;
    public float longitude;
    public float latitude;
    public String faction;
    public int hp;

    public NodeModel(String name, float longitude, float latitude, String faction, int hp)
    {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.faction = faction;
        this.hp = hp;
    }
}
