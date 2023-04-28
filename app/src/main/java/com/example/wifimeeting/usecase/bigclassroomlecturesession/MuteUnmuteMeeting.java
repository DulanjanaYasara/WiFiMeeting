package com.example.wifimeeting.usecase.bigclassroomlecturesession;

import android.util.Log;

import com.example.wifimeeting.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;

public class MuteUnmuteMeeting {

    private boolean LISTEN_MUTE_MEETING = true;
    private LinkedHashMap<String, Boolean> members;
    private InetAddress broadcastIP;

    public MuteUnmuteMeeting(LinkedHashMap<String, Boolean> members, InetAddress broadcastIP) {
        this.members = members;
        this.broadcastIP = broadcastIP;

        listenMuteUnmuteMeeting();
    }

    /**
     * Updating members information from the LinkedHashMap
     */
    public void muteUnmuteMember(String name, Boolean isMute) {
        if (members.containsKey(name)) {
            Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Updating member: " + name);
        } else {
            Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Adding member: " + name);
        }
        members.put(name, isMute);
        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "#Members: " + members.size());
    }


    /**
     * Broadcast the MUTE action
     */
    public void broadcastMuteUnmute(String action, final String name,final Boolean isMute) {

        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Broadcasting MUTE UNMUTE Action started!");
        Thread broadcastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String request = action + name + (isMute ? "1" : "0");
                    byte[] message = request.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, Constants.MUTE_UNMUTE_BROADCAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "MUTE UNMUTE Action Broadcast packet sent: " + packet.getAddress().toString());
                    socket.disconnect();
                    socket.close();
                } catch (SocketException e) {

                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "SocketException in MUTE UNMUTE Action broadcast: " + e);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "MUTE UNMUTE Action Broadcaster ending!");
                } catch (IOException e) {

                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "IOException in MUTE UNMUTE Action broadcast: " + e);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "MUTE UNMUTE Action Broadcaster ending!");
                } catch (Exception e) {

                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "Exception in MUTE UNMUTE Action broadcast: " + e);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "MUTE UNMUTE Action Broadcaster ending!");
                }
            }
        });
        broadcastThread.start();
    }


    /**
     * Listening thread for mute-unmute meeting
     */
    public void listenMuteUnmuteMeeting() {

        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listening started for mute unmute meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(Constants.MUTE_UNMUTE_BROADCAST_PORT);
                } catch (SocketException e) {

                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "SocketException in listener for mute unmute meeting: " + e);
                    return;
                }
                byte[] buffer = new byte[Constants.BROADCAST_BUF_SIZE];
                while (LISTEN_MUTE_MEETING) {
                    listen(socket, buffer);
                }

                Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listener for mute unmute meeting ending!");
                socket.disconnect();
                socket.close();
            }

            //Listen in for new notifications
            public void listen(DatagramSocket socket, byte[] buffer) {

                try {

                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listening for a mute unmute meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.BROADCAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverName = data.substring(2, data.length() - 1);
                    Boolean isMuteValue = Boolean.parseBoolean(data.substring(data.length() - 1));

                    if (receivedAction.equals(Constants.MUTE_ACTION)) {
                        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Mute unmute Meeting Listener received MUTE request");
                        muteUnmuteMember(receiverName, isMuteValue);

                    } else {
                        Log.w(Constants.MUTE_UNMUTE_LOG_TAG, "Mute unmute Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "No packet received!");
                    if (LISTEN_MUTE_MEETING) {
                        listen(socket, buffer);
                    }
                } catch (SocketException e) {

                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "SocketException in listen: " + e);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listener ending!");
                } catch (IOException e) {

                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "IOException in listen: " + e);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listener ending!");
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningMuteUnmuteMeeting() {
        LISTEN_MUTE_MEETING = false;
    }
}
