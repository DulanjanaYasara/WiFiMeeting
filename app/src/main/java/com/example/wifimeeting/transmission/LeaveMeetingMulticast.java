package com.example.wifimeeting.transmission;

import android.util.Log;

import com.example.wifimeeting.page.GroupDiscussionPage;
import com.example.wifimeeting.page.LectureSessionPage;
import com.example.wifimeeting.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class LeaveMeetingMulticast {

    private boolean LISTEN_LEAVE_MEETING = true;
    private Object uiPage;
    private InetAddress multicastIP;

    public LeaveMeetingMulticast(Object uiPage, InetAddress multicastIP) {
        this.uiPage = uiPage;
        this.multicastIP = multicastIP;

        listenLeaveMeeting();
    }

    /**
     * Multicast the LEAVE or ABSENT action
     */
    public void multicastLeaveAbsent(String action, final String name) {

        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Multicasting LEAVE ABSENT Action started!");
        Thread multicastThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket = null;
                try {
                    String request = action + name;
                    byte[] message = request.getBytes();
                    socket = new MulticastSocket();

                    DatagramPacket packet = new DatagramPacket(message, message.length, multicastIP, Constants.MARK_ABSENCE_MULTICAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Multicast packet sent: " + packet.getAddress().toString());

                } catch (Exception e) {
                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "Exception in LEAVE ABSENT Action multicast: " + e);
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "LEAVE ABSENT Action Multicasting ending!");

                } finally {
                    if(socket != null){
                        socket.disconnect();
                        socket.close();
                    }
                }
            }
        });
        multicastThread.start();
    }

    /**
     * Listening thread for leave meeting
     */
    public void listenLeaveMeeting() {

        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listening started for leave meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket = null;
                try {
                    socket = new MulticastSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MARK_ABSENCE_MULTICAST_PORT));
                    socket.joinGroup(multicastIP);


                    byte[] buffer = new byte[Constants.MULTICAST_BUF_SIZE];
                    while (LISTEN_LEAVE_MEETING) {
                        listen(socket, buffer);
                    }

                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listener for leave meeting ending!");
                    socket.leaveGroup(multicastIP);
                    socket.disconnect();
                    socket.close();
                } catch (Exception e) {
                    Log.e(Constants.LEAVE_MEETING_LOG_TAG, "Exception in listener for leave meeting: " + e);
                    if(socket != null){
                        try {
                            socket.leaveGroup(multicastIP);
                        } catch (IOException ex) {
                            Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in listener for leaving the multicast group: " + e);
                        }
                        socket.disconnect();
                        socket.close();
                    }
                    return;
                }
            }

            //Listen in for new notifications
            public void listen(MulticastSocket socket, byte[] buffer) {

                try {

                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Listening for a leave meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.MULTICAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverName = data.substring(2);

                    if (receivedAction.equals(Constants.LEAVE_ACTION) && uiPage instanceof GroupDiscussionPage) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received LEAVE request");
                        ((GroupDiscussionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, true);
                        multicastLeaveAbsent(Constants.ABSENT_ACTION, receiverName);
                    
                    } else  if (receivedAction.equals(Constants.ABSENT_ACTION) && uiPage instanceof GroupDiscussionPage) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received ABSENT request");
                        ((GroupDiscussionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, true);

                    } else if (receivedAction.equals(Constants.LEAVE_ACTION) && uiPage instanceof LectureSessionPage) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received LEAVE request");
                        ((LectureSessionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, true);
                        multicastLeaveAbsent(Constants.ABSENT_ACTION, receiverName);

                    } else  if (receivedAction.equals(Constants.ABSENT_ACTION) && uiPage instanceof LectureSessionPage) {
                        Log.i(Constants.LEAVE_MEETING_LOG_TAG, "Leave Meeting Listener received ABSENT request");
                        ((LectureSessionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, true);


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
