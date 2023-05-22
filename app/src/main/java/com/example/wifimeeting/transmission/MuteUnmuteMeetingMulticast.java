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

public class MuteUnmuteMeetingMulticast {

    private boolean LISTEN_MUTE_MEETING = true;
    private Object uiPage;
    private InetAddress multicastIP;

    public MuteUnmuteMeetingMulticast(Object uiPage, InetAddress multicastIP) {
        this.uiPage = uiPage;
        this.multicastIP = multicastIP;

        listenMuteUnmuteMeeting();
    }

    /**
     * Multicast the MUTE action
     */
    public void multicastMuteUnmute(String action, final String name, final Boolean isMute) {

        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Multicasting MUTE UNMUTE Action started!");
        Thread multicastThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket = null;
                try {
                    for(int i=0; i <=Constants.MUTE_UNMUTE_MULTICAST_TIMES; i++){
                        String request = action + name + (isMute ? "1" : "0");
                        byte[] message = request.getBytes();
                        socket = new MulticastSocket();

                        DatagramPacket packet = new DatagramPacket(message, message.length, multicastIP, Constants.MUTE_UNMUTE_MULTICAST_PORT);
                        socket.send(packet);
                        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "MUTE UNMUTE Action Multicast packet sent: " + packet.getAddress().toString());

                        Thread.sleep(500);
                    }

                } catch (Exception e) {
                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "Exception in MUTE UNMUTE Action multicast: " + e);
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "MUTE UNMUTE Action Multicasting ending!");

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
     * Listening thread for mute-unmute meeting
     */
    public void listenMuteUnmuteMeeting() {

        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listening started for mute unmute meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket = null;
                try {
                    socket = new MulticastSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MUTE_UNMUTE_MULTICAST_PORT));
                    socket.joinGroup(multicastIP);

                    byte[] buffer = new byte[Constants.MULTICAST_BUF_SIZE];
                    while (LISTEN_MUTE_MEETING) {
                        listen(socket, buffer);
                    }

                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listener for mute unmute meeting ending!");
                    socket.leaveGroup(multicastIP);
                    socket.disconnect();
                    socket.close();
                } catch (Exception e) {
                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "Exception in listener for mute unmute meeting: " + e);
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

                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Listening for a mute unmute meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.MULTICAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);
                    String receiverName = data.substring(2, data.length() - 1);
                    Boolean isMuteValue = data.endsWith("1");

                    if (receivedAction.equals(Constants.MUTE_ACTION) && uiPage instanceof GroupDiscussionPage) {
                        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Mute unmute Meeting Listener received MUTE request");
                        ((GroupDiscussionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, isMuteValue);

                    } else if (receivedAction.equals(Constants.MUTE_ACTION) && uiPage instanceof LectureSessionPage) {
                        Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "Mute unmute Meeting Listener received MUTE request");
                        ((LectureSessionPage) uiPage).updateMemberHashMap(receivedAction, receiverName, isMuteValue);

                    } else {
                        Log.w(Constants.MUTE_UNMUTE_LOG_TAG, "Mute unmute Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.MUTE_UNMUTE_LOG_TAG, "No packet received!");
                    if (LISTEN_MUTE_MEETING) {
                        listen(socket, buffer);
                    }
                    return;

                } catch (Exception e) {
                    Log.e(Constants.MUTE_UNMUTE_LOG_TAG, "Exception in listen: " + e);
                    return;
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningMuteUnmuteMeeting() {
        LISTEN_MUTE_MEETING = false;
    }
}
