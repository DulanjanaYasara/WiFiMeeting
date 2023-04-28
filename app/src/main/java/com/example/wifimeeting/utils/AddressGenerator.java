package com.example.wifimeeting.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddressGenerator {

    private InetAddress broadcastIp;
    private int ipAddress;

    public AddressGenerator(View view) {
        generateAddress(view);
    }

    public int getIpAddress() {
        return ipAddress;
    }

    public InetAddress getBroadcastIp() {
        return broadcastIp;
    }

    public void generateAddress(View view){
        try{
            WifiManager wifiManager = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String addressString = toBroadcastIp(ipAddress);
            broadcastIp = InetAddress.getByName(addressString);

        } catch(UnknownHostException e) {
            Log.e(Constants.JOIN_MEETING_LOG_TAG,"UnknownHostException in get IP address: " + e);
        }
    }

    // Returns converts an IP address in int format to a formatted string
    private String toBroadcastIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + "255";
    }
}
