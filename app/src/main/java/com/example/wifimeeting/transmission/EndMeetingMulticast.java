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

public class EndMeetingMulticast {

    private boolean LISTEN_END_MEETING = true;
    private InetAddress multicastIP;

    public EndMeetingMulticast(InetAddress multicastIP) {
        this.multicastIP = multicastIP;
    }

    /**
     * Multicast the END
     */
    public void multicastEndMeeting(String action, Object uiPage) {

        Log.i(Constants.END_MEETING_LOG_TAG, "Multicasting END Action started!");
        Thread multicastThread = new Thread(new Runnable() {

            @Override
            public void run() {
                MulticastSocket socket = null;

                try {
                    int noOfMessagesSent = 1;
                    if(uiPage instanceof GroupDiscussionPage)
                        noOfMessagesSent = Constants.GROUP_DISCUSSION_END_MEETING_MULTICAST_TIMES;

                    for(int i=0; i <= noOfMessagesSent; i++){
                        byte[] message = action.getBytes();
                        socket = new MulticastSocket();

                        DatagramPacket packet = new DatagramPacket(message, message.length, multicastIP, Constants.MARK_END_MULTICAST_PORT);
                        socket.send(packet);
                        Log.i(Constants.END_MEETING_LOG_TAG, "END Action Multicast packet sent: " + packet.getAddress().toString());

                        Thread.sleep(500);
                    }

                } catch (Exception e) {
                    Log.e(Constants.END_MEETING_LOG_TAG, "Exception in END Action multicast: " + e);
                    Log.i(Constants.END_MEETING_LOG_TAG, "END Action Multicasting ending!");

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
     * Listening thread for End meeting
     */
    public void listenEndMeeting(Object uiPage) {

        Log.i(Constants.END_MEETING_LOG_TAG, "Listening started for End meeting!");

        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                MulticastSocket socket =null;
                try {
                    socket = new MulticastSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Constants.MARK_END_MULTICAST_PORT));
                    socket.joinGroup(multicastIP);

                    byte[] buffer = new byte[Constants.MULTICAST_BUF_SIZE];
                    while (LISTEN_END_MEETING) {
                        listen(socket, buffer);
                    }

                    Log.i(Constants.END_MEETING_LOG_TAG, "Listener for End meeting ending!");
                    socket.leaveGroup(multicastIP);
                    socket.disconnect();
                    socket.close();
                } catch (Exception e) {
                    Log.e(Constants.END_MEETING_LOG_TAG, "Exception in listener for End meeting: " + e);
                    if(socket!=null){
                        try {
                            socket.leaveGroup(multicastIP);
                        } catch (IOException ex) {
                            Log.e(Constants.END_MEETING_LOG_TAG, "Exception in listener for leaving the multicast group: " + e);
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

                    Log.i(Constants.END_MEETING_LOG_TAG, "Listening for a End meeting packet!");

                    DatagramPacket packet = new DatagramPacket(buffer, Constants.MULTICAST_BUF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    Log.i(Constants.END_MEETING_LOG_TAG, "Packet received: " + data);

                    String receivedAction = data.substring(0, 2);

                    if (receivedAction.equals(Constants.END_ACTION)) {
                        Log.i(Constants.END_MEETING_LOG_TAG, "End Meeting Listener received END request");

                        if(uiPage instanceof GroupDiscussionPage)
                            ((GroupDiscussionPage) uiPage).leaveMeeting(true);
                        else if (uiPage instanceof LectureSessionPage)
                            ((LectureSessionPage) uiPage).leaveMeeting(true);
                        
                    } else {
                        Log.w(Constants.END_MEETING_LOG_TAG, "End Meeting Listener received invalid request: " + receivedAction);
                    }

                } catch (SocketTimeoutException e) {

                    Log.i(Constants.END_MEETING_LOG_TAG, "No packet received!");
                    if (LISTEN_END_MEETING) {
                        listen(socket, buffer);
                    }
                    return;

                } catch (Exception e) {
                    Log.e(Constants.END_MEETING_LOG_TAG, "Exception in listen: " + e);
                    return;
                }
            }
        });
        listenThread.start();
    }

    public void stopListeningEndMeeting() {
        LISTEN_END_MEETING = false;
    }
}
