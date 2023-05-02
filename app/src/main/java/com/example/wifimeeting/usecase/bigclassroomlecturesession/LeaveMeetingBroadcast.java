package com.example.wifimeeting.usecase.bigclassroomlecturesession;

import android.util.Log;

import com.example.wifimeeting.page.LectureSessionPage;
import com.example.wifimeeting.utils.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class LeaveMeetingBroadcast {

    private boolean LISTEN_LEAVE_MEETING = true;
    private LectureSessionPage uiPage;
    private InetAddress broadcastIP;

    public LeaveMeetingBroadcast(LectureSessionPage uiPage, InetAddress broadcastIP) {
        this.uiPage = uiPage;
        this.broadcastIP = broadcastIP;

        listenLeaveMeeting();
    }

    /**
     * Broadcast the LEAVE or ABSENT action
     */
    public void broadcastLeaveAbsent(String action, final String name) {

        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Broadcasting LEAVE ABSENT Action started!");
        Thread broadcastThread = new Thread(new Runnable() {

            @Override
            public void run() {

                DatagramSocket socket = null;
                try {
                    String request = action + name;
                    byte[] message = request.getBytes();
                    socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, Constants.MARK_ABSENCE_BROADCAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Broadcast packet sent: " + packet.getAddress().toString());

                } catch (Exception e) {
                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "Exception in LEAVE ABSENT Action broadcast: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Broadcaster ending!");

                } finally {
                    if(socket != null){
                        socket.disconnect();
                        socket.close();
                    }
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

                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MARK_ABSENCE_BROADCAST_PORT));
                } catch (Exception e) {
                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "Exception in listener for leave meeting: " + e);
                    if(socket != null){
                        socket.disconnect();
                        socket.close();
                    }
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
                        uiPage.updateMemberHashMap(receivedAction, receiverName, true);
                        broadcastLeaveAbsent(Constants.ABSENT_ACTION, receiverName);
                    
                    } else  if (receivedAction.equals(Constants.ABSENT_ACTION)) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received ABSENT request");
                        uiPage.updateMemberHashMap(receivedAction, receiverName, true);
                    } else {
                        Log.w(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "No packet received!");
                    if (LISTEN_LEAVE_MEETING) {
                        listen(socket, buffer);
                    }
                    return;

                } catch (Exception e) {
                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "Exception in listen: " + e);
                    return;
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningLeaveMeeting() {
        LISTEN_LEAVE_MEETING = false;
    }
}
