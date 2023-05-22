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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateMembersMulticast {

    private boolean LISTEN_UPDATE_MEMBER = true;
    private Object uiPage;
    private InetAddress multicastIP;

    public UpdateMembersMulticast(Object uiPage, InetAddress multicastIP, InetAddress myIp) {
        this.uiPage = uiPage;
        this.multicastIP = multicastIP;
       
        listenUpdateMember(myIp);
    }

    /**
     * Multicast the UPDATE action
     */
    public void multicastUpdateMember(String action, final LinkedHashMap<String, Boolean> memberHashMap) {

        Log.i(Constants.UPDATE_MEMBERS, "Multicasting UPDATE Action started!");
        Thread multicastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                MulticastSocket socket = null;

                try {

                    StringBuilder stringBuilder = new StringBuilder();
                    for (Map.Entry<String, Boolean> entry : memberHashMap.entrySet()) {
                        stringBuilder.append(entry.getKey());
                        stringBuilder.append(entry.getValue() ? "1" : "0");
                        stringBuilder.append(Constants.STRING_SEPARATOR);
                    }

                    String request = action + stringBuilder.deleteCharAt(stringBuilder.length()-1);
                    byte[] message = request.getBytes();
                    socket = new MulticastSocket();

                    DatagramPacket packet = new DatagramPacket(message, message.length, multicastIP, Constants.UPDATE_MEMBER_MULTICAST_PORT);
                    socket.send(packet);
                    Log.i(Constants.UPDATE_MEMBERS, "UPDATE Action Multicast packet sent: " + packet.getAddress().toString());

                } catch (Exception e) {
                    Log.e(Constants.UPDATE_MEMBERS, "Exception in UPDATE Action multicast: " + e);
                    Log.i(Constants.UPDATE_MEMBERS, "UPDATE Action Multicasting ending!");

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
     * Listening thread for update member
     */
    public void listenUpdateMember(InetAddress myIp) {

        Log.i(Constants.UPDATE_MEMBERS, "Listening started for update member!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket =null;
                try {
                    socket = new MulticastSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.UPDATE_MEMBER_MULTICAST_PORT));
                    socket.joinGroup(multicastIP);

                    byte[] buffer = new byte[Constants.MULTICAST_BUF_SIZE];
                    while (LISTEN_UPDATE_MEMBER) {
                        listen(socket, buffer);
                    }

                    Log.i(Constants.UPDATE_MEMBERS, "Listener for update member ending!");
                    socket.leaveGroup(multicastIP);
                    socket.disconnect();
                    socket.close();
                } catch (Exception e) {
                    Log.e(Constants.UPDATE_MEMBERS, "Exception in listener for update member: " + e);
                    if(socket!=null){
                        try {
                            socket.leaveGroup(multicastIP);
                        } catch (IOException ex) {
                            Log.e(Constants.UPDATE_MEMBERS, "Exception in listener for leaving the multicast group: " + e);
                        }
                        socket.disconnect();
                        socket.close();
                    }
                    return;
                }
            }

            //Listen in for new messages
            public void listen(MulticastSocket socket, byte[] buffer) {

                try {

                    Log.i(Constants.UPDATE_MEMBERS, "Listening for a update member packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.MULTICAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);

                    //neglect the message sent by itself
                    if(packet.getAddress().equals(myIp))
                        return;

                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.UPDATE_MEMBERS, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverPayload = data.substring(2);

                    List<String> memberListSingleString = Arrays.asList(receiverPayload.split(Constants.STRING_SEPARATOR));

                    LinkedHashMap<String, Boolean> memberList = new LinkedHashMap<>();
                    for(String memberDetail : memberListSingleString){
                        memberList.put(memberDetail.substring(0,memberDetail.length()-1), memberDetail.substring(memberDetail.length()-1).equals("1"));
                    }

                    if (receivedAction.equals(Constants.UPDATE_ACTION) && uiPage instanceof GroupDiscussionPage) {
                        Log.i(Constants.UPDATE_MEMBERS, "Update member Listener received UPDATE request");
                        GroupDiscussionPage groupDiscussionPage = ((GroupDiscussionPage) uiPage);
                        groupDiscussionPage.addMissedMembers(memberList);

                    } else if (receivedAction.equals(Constants.UPDATE_ACTION) && uiPage instanceof LectureSessionPage) {
                        Log.i(Constants.UPDATE_MEMBERS, "Update member Listener received UPDATE request");
                        LectureSessionPage lectureSessionPage = ((LectureSessionPage) uiPage);
                        lectureSessionPage.addMissedMembers(memberList);

                    } else {
                        Log.w(Constants.UPDATE_MEMBERS, "Update member Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.UPDATE_MEMBERS, "No packet received!");
                    if (LISTEN_UPDATE_MEMBER) {
                        listen(socket, buffer);
                    }
                    return;

                } catch (Exception e) {
                    Log.e(Constants.UPDATE_MEMBERS, "Exception in listen: " + e);
                    return;
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningUpdateMembers() {
        LISTEN_UPDATE_MEMBER = false;
    }
}
