package com.example.wifimeeting.usecase.bigclassroomlecturesession;

import android.util.Log;

import com.example.wifimeeting.page.MeetingPage;
import com.example.wifimeeting.utils.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class JoinMeetingBroadcast {

    private boolean LISTEN_JOIN_MEETING = true;
    private MeetingPage uiPage;
    private InetAddress broadcastIP;
    private String name;
    private Boolean isMute;

    public JoinMeetingBroadcast(MeetingPage uiPage, String name, Boolean isMute, InetAddress broadcastIP) {
        this.uiPage = uiPage;
        this.broadcastIP = broadcastIP;
        this.name = name;
        this.isMute = isMute;

        listenJoinMeeting();
        broadcastJoinPresent(Constants.JOIN_ACTION, name, isMute);
    }

    /**
     * Broadcast the JOIN or PRESENT action
     */
    public void broadcastJoinPresent(String action, final String name, final Boolean isMute) {

        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Broadcasting JOIN PRESENT Action started!");
        Thread broadcastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                DatagramSocket socket = null;

                try {
                    String request = action + name + (isMute ? "1" : "0");
                    byte[] message = request.getBytes();
                    socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, Constants.MARK_PRESENCE_BROADCAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "JOIN PRESENT Action Broadcast packet sent: " + packet.getAddress().toString());

                } catch (Exception e) {
                    Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in JOIN PRESENT Action broadcast: " + e);
                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "JOIN PRESENT Action Broadcaster ending!");

                } finally {
                    if(socket!=null){
                        socket.disconnect();
                        socket.close();
                    }
                }
            }
        });
        broadcastThread.start();
    }

    /**
     * Listening thread for join meeting
     */
    public void listenJoinMeeting() {

        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Listening started for join meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                DatagramSocket socket =null;
                try {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MARK_PRESENCE_BROADCAST_PORT));
                } catch (Exception e) {
                    Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in listener for join meeting: " + e);
                    if(socket!=null){
                        socket.disconnect();
                        socket.close();
                    }
                    return;
                }
                byte[] buffer = new byte[Constants.BROADCAST_BUF_SIZE];
                while (LISTEN_JOIN_MEETING) {
                    listen(socket, buffer);
                }

                Log.i(Constants.JOIN_MEETING_LOG_TAG, "Listener for join meeting ending!");
                socket.disconnect();
                socket.close();
            }

            //Listen in for new notifications
            public void listen(DatagramSocket socket, byte[] buffer) {

                try {

                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "Listening for a join meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.BROADCAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverName = data.substring(2, data.length() - 1);
                    Boolean isMuteValue = data.endsWith("1");

                    if (receivedAction.equals(Constants.JOIN_ACTION)) {
                        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received JOIN request");
                        uiPage.updateMemberHashMap(receivedAction, receiverName, isMuteValue);
                        broadcastJoinPresent(Constants.PRESENT_ACTION, name, isMute);

                    } else  if (receivedAction.equals(Constants.PRESENT_ACTION)) {
                        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received PRESENT request");
                        uiPage.updateMemberHashMap(receivedAction, receiverName, isMuteValue);

                    } else {
                        Log.w(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "No packet received!");
                    if (LISTEN_JOIN_MEETING) {
                        listen(socket, buffer);
                    }
                    return;

                } catch (Exception e) {
                    Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in listen: " + e);
                    return;
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningJoinMeeting() {
        LISTEN_JOIN_MEETING = false;
    }
}
