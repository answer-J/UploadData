package com.android.db;


public class TrafficModel {
	public int id;
	public String pkgname;
	public String label;
	public long traffic;
	public long traffic_last;
	@Override
	public String toString() {
		return "TrafficModel [id=" + id + ", pkgname=" + pkgname + ", label="
				+ label + ", traffic=" + traffic + ", traffic_last="
				+ traffic_last + "]";
	}
	
}
