package com.example.wifimeeting.usecase.bigclassroomlecturesession;

import android.util.Log;

import com.example.wifimeeting.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class LeaveMeeting {

    private boolean LISTEN_LEAVE_MEETING = true;
    private HashMap<String, Boolean> members;
    private InetAddress broadcastIP;

    public LeaveMeeting(HashMap<String, Boolean> members, InetAddress broadcastIP) {
        this.members = members;
        this.broadcastIP = broadcastIP;

        listenLeaveMeeting();
    }

    /**
     * Removing members information from the HashMap
     */
    public void leaveMember(String name) {
        if(members.containsKey(name)) {
            Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Removing member: " + name);
            members.remove(name);
            Log.i(Constants.LEAVE_MEETING_LOG_TAG, "#Members: " + members.size());
            return;
        }
        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Cannot remove member. " + name + " does not exist.");
    }


    /**
     * Broadcast the LEAVE or ABSENT action
     */
    public void broadcastLeaveAbsent(String action, final String name) {

        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Broadcasting LEAVE ABSENT Action started!");
        Thread broadcastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String request = action + name;
                    byte[] message = request.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, Constants.MARK_ABSENCE_BROADCAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Broadcast packet sent: " + packet.getAddress().toString());
                    socket.disconnect();
                    socket.close();
                } catch (SocketException e) {

                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "SocketException in LEAVE ABSENT Action broadcast: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Broadcaster ending!");
                } catch (IOException e) {

                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "IOException in LEAVE ABSENT Action broadcast: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Broadcaster ending!");
                } catch (Exception e) {

                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "Exception in LEAVE ABSENT Action broadcast: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Broadcaster ending!");
                }
            }
        });
        broadcastThread.start();
    }


    /**
     * Listening thread for leave meeting
     */
    public void listenLeaveMeeting() {

        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listening started for leave meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(Constants.MARK_ABSENCE_BROADCAST_PORT);
                } catch (SocketException e) {

                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "SocketException in listener for leave meeting: " + e);
                    return;
                }
                byte[] buffer = new byte[Constants.BROADCAST_BUF_SIZE];
                while (LISTEN_LEAVE_MEETING) {
                    listen(socket, buffer);
                }

                Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listener for leave meeting ending!");
                socket.disconnect();
                socket.close();
            }

            //Listen in for new notifications
            public void listen(DatagramSocket socket, byte[] buffer) {

                try {

                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listening for a leave meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.BROADCAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverName = data.substring(2);

                    if (receivedAction.equals(Constants.LEAVE_ACTION)) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received LEAVE request");
                        leaveMember(receiverName);
                        broadcastLeaveAbsent(Constants.ABSENT_ACTION, receiverName);
                    
                    } else  if (receivedAction.equals(Constants.ABSENT_ACTION)) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received ABSENT request");
                        leaveMember(receiverName);
                    
                    } else {
                        Log.w(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "No packet received!");
                    if (LISTEN_LEAVE_MEETING) {
                        listen(socket, buffer);
                    }
                } catch (SocketException e) {

                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "SocketException in listen: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listener ending!");
                } catch (IOException e) {

                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "IOException in listen: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listener ending!");
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningLeaveMeeting() {
        LISTEN_LEAVE_MEETING = false;
    }
}
