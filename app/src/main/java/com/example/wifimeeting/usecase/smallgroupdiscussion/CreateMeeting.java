package com.example.wifimeeting.usecase.smallgroupdiscussion;

import android.util.Log;

import com.example.wifimeeting.page.SmallGroupDiscussionPage;
import com.example.wifimeeting.utils.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

public class CreateMeeting {

    private boolean LISTEN_CREATE_MEETING = true;
    private SmallGroupDiscussionPage uiPage;
    private InetAddress broadcastIP;

    public CreateMeeting(SmallGroupDiscussionPage uiPage, InetAddress broadcastIP) {
        this.uiPage = uiPage;
        this.broadcastIP = broadcastIP;

        listenCreateMeeting();
    }

    /**
     * Broadcast the CREATE 
     */
    public void broadcastCreatePresent(String action, String groupName, String noOfMembers, String multicastAddress) {

        Log.i(Constants.CREATE_MEETING_LOG_TAG, "Broadcasting CREATE Action started!");
        Thread broadcastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                DatagramSocket socket = null;

                try {
                    String request = action + groupName + Constants.STRING_SEPARATOR + multicastAddress + Constants.STRING_SEPARATOR + noOfMembers;
                    byte[] message = request.getBytes();
                    socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, Constants.MARK_CREATE_BROADCAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.CREATE_MEETING_LOG_TAG, "CREATE Action Broadcast packet sent: " + packet.getAddress().toString());

                } catch (Exception e) {
                    Log.e(Constants.CREATE_MEETING_LOG_TAG, "Exception in CREATE Action broadcast: " + e);
                    Log.i(Constants.CREATE_MEETING_LOG_TAG, "CREATE Action Broadcaster ending!");

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
     * Listening thread for create meeting
     */
    public void listenCreateMeeting() {

        Log.i(Constants.CREATE_MEETING_LOG_TAG, "Listening started for create meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                DatagramSocket socket =null;
                try {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MARK_CREATE_BROADCAST_PORT));
                } catch (Exception e) {
                    Log.e(Constants.CREATE_MEETING_LOG_TAG, "Exception in listener for create meeting: " + e);
                    if(socket!=null){
                        socket.disconnect();
                        socket.close();
                    }
                    return;
                }
                byte[] buffer = new byte[Constants.BROADCAST_BUF_SIZE];
                while (LISTEN_CREATE_MEETING) {
                    listen(socket, buffer);
                }

                Log.i(Constants.CREATE_MEETING_LOG_TAG, "Listener for create meeting ending!");
                socket.disconnect();
                socket.close();
            }

            //Listen in for new notifications
            public void listen(DatagramSocket socket, byte[] buffer) {

                try {

                    Log.i(Constants.CREATE_MEETING_LOG_TAG, "Listening for a create meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.BROADCAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.CREATE_MEETING_LOG_TAG, "Packet received: " + data);

                    List<String> dataList = Arrays.asList(data.split(Constants.STRING_SEPARATOR));

                    String receivedAction = dataList.get(0).substring(0, 2);
                    String groupName = dataList.get(0).substring(2);
                    String multicastIpAddress = dataList.get(1);
                    String noOfMembers = dataList.get(dataList.size()-1);

                    if (receivedAction.equals(Constants.CREATE_ACTION)) {
                        Log.i(Constants.CREATE_MEETING_LOG_TAG, "Create Meeting Listener received CREATE request");

                        //TODO:
//                        uiPage.updateDiscusionGroupItemList(groupName, multicastIpAddress, noOfMembers);

                    } else {
                        Log.w(Constants.CREATE_MEETING_LOG_TAG, "Create Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.CREATE_MEETING_LOG_TAG, "No packet received!");
                    if (LISTEN_CREATE_MEETING) {
                        listen(socket, buffer);
                    }
                    return;

                } catch (Exception e) {
                    Log.e(Constants.CREATE_MEETING_LOG_TAG, "Exception in listen: " + e);
                    return;
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningCreateMeeting() {
        LISTEN_CREATE_MEETING = false;
    }
}
