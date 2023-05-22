package com.example.wifimeeting.utils;

import android.util.Log;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class MulticastAddressGenerator {

    public static String generateMulticastAddress(){
        Random rand = new Random();
        return "239." + rand.nextInt(256) + "." + rand.nextInt(256) + "." + rand.nextInt(256);
    }

    public static Boolean validateMulticastGroupAddress(String multicastAddress){
        InetAddress group;
        MulticastSocket socket = null;

        try {
            group = InetAddress.getByName(multicastAddress);

            if(!group.isMulticastAddress()){
                // address not a valid multicast group address
                return false;
            }

            try {
                socket = new MulticastSocket(0);
                socket.setReuseAddress(true);
                socket.joinGroup(group);
                return true;
            } catch (Exception e) {
                // address is already in use
                return false;
            } finally {
                if (socket != null) {
                    socket.leaveGroup(group);
                    socket.close();
                }
            }
        } catch (Exception e) {
            Log.e(Constants.ADDRESS_GENERATOR_LOG_TAG, "Error validating multicast group address: " + e.getMessage());
            return false;
        }
    }
}
