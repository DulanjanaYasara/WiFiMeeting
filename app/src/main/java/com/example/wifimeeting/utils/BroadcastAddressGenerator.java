package com.example.wifimeeting.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class BroadcastAddressGenerator {

    private InetAddress broadcastIp;
    private InetAddress ipAddress;

    public BroadcastAddressGenerator(View view) {
        generateAddress(view);
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public InetAddress getBroadcastIp() {
        return broadcastIp;
    }

    private void generateAddress(View view){
        try{
            WifiManager wifiManager = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            int ip = wifiManager.getConnectionInfo().getIpAddress();
            String ipAddressString = toIp(ip);
            String broadcastAddressString = toBroadcastIp(ip);
            broadcastIp = InetAddress.getByName(broadcastAddressString);
            ipAddress = InetAddress.getByName(ipAddressString);

        } catch(UnknownHostException e) {
            Log.e(Constants.ADDRESS_GENERATOR_LOG_TAG,"UnknownHostException in get IP address: " + e);
        }
    }

    // Returns converts an IP address in int format to a formatted broadcast string
    private String toBroadcastIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + "255";
    }

    // Returns converts an IP address in int format to a formatted ip string
    private String toIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }
}
