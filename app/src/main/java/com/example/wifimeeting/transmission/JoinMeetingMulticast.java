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

public class JoinMeetingMulticast {

    private boolean LISTEN_JOIN_MEETING = true;
    private Object uiPage;
    private InetAddress multicastIP;
    private String name;
    private Boolean isMute;

    public JoinMeetingMulticast(Object uiPage, String name, Boolean isMute, InetAddress multicastIP) {
        this.uiPage = uiPage;
        this.multicastIP = multicastIP;
        this.name = name;
        this.isMute = isMute;

        listenJoinMeeting();
        multicastJoinPresent(Constants.JOIN_ACTION, name, isMute);
    }

    /**
     * Multicast the JOIN or PRESENT action
     */
    public void multicastJoinPresent(String action, final String name, final Boolean isMute) {

        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Multicasting JOIN PRESENT Action started!");
        Thread multicastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                MulticastSocket socket = null;

                try {
                    String request = action + name + (isMute ? "1" : "0");
                    byte[] message = request.getBytes();
                    socket = new MulticastSocket();

                    DatagramPacket packet = new DatagramPacket(message, message.length, multicastIP, Constants.MARK_PRESENCE_MULTICAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "JOIN PRESENT Action Multicast packet sent: " + packet.getAddress().toString());

                } catch (Exception e) {
                    Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in JOIN PRESENT Action multicast: " + e);
                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "JOIN PRESENT Action Multicasting ending!");

                } finally {
                    if(socket!=null){
                        socket.disconnect();
                        socket.close();
                    }
                }
            }
        });
        multicastThread.start();
    }

    /**
     * Listening thread for join meeting
     */
    public void listenJoinMeeting() {

        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Listening started for join meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket =null;
                try {
                    socket = new MulticastSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MARK_PRESENCE_MULTICAST_PORT));
                    socket.joinGroup(multicastIP);

                    byte[] buffer = new byte[Constants.MULTICAST_BUF_SIZE];
                    while (LISTEN_JOIN_MEETING) {
                        listen(socket, buffer);
                    }

                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "Listener for join meeting ending!");
                    socket.leaveGroup(multicastIP);
                    socket.disconnect();
                    socket.close();
                } catch (Exception e) {
                    Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in listener for join meeting: " + e);
                    if(socket!=null){
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

                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "Listening for a join meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.MULTICAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.JOIN_MEETING_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverName = data.substring(2, data.length() - 1);
                    Boolean isMuteValue = data.endsWith("1");

                    if (receivedAction.equals(Constants.JOIN_ACTION) && uiPage instanceof GroupDiscussionPage) {
                        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received JOIN request");
                        ((GroupDiscussionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, isMuteValue);
                        multicastJoinPresent(Constants.PRESENT_ACTION, name, isMute);

                    } else  if (receivedAction.equals(Constants.PRESENT_ACTION) && uiPage instanceof GroupDiscussionPage) {
                        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received PRESENT request");
                        ((GroupDiscussionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, isMuteValue);

                    } else if (receivedAction.equals(Constants.JOIN_ACTION) && uiPage instanceof LectureSessionPage) {
                            Log.i(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received JOIN request");
                            ((LectureSessionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, isMuteValue);
                            multicastJoinPresent(Constants.PRESENT_ACTION, name, isMute);

                    } else  if (receivedAction.equals(Constants.PRESENT_ACTION) && uiPage instanceof LectureSessionPage) {
                        Log.i(Constants.JOIN_MEETING_LOG_TAG, "Join Meeting Listener received PRESENT request");
                        ((LectureSessionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, isMuteValue);

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
