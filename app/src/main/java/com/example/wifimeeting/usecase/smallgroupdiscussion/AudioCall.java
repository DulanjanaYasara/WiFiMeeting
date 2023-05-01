package com.example.wifimeeting.usecase.smallgroupdiscussion;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.wifimeeting.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class AudioCall {

    //Calculating the buffer size for an Android audio track (in bytes)
    private static final int BUF_SIZE = Constants.SAMPLE_INTERVAL * Constants.SAMPLE_INTERVAL * Constants.SAMPLE_SIZE * 2;
    private InetAddress multicastIP; // Address to call
    private InetAddress myAddress; // My IP Address
    private boolean mic = false; // Enable mic?
    private boolean speakers = false; // Enable speakers?
    private Boolean isMute;

    public AudioCall(InetAddress myAddress, InetAddress multicastIP, Boolean isMute) {
        this.myAddress = myAddress;
        this.isMute = isMute;
        this.multicastIP = multicastIP;
    }

    public void startCall() {
        Log.i(Constants.AUDIO_CALL_LOG_TAG, "Starting call!");
        if(!isMute) startMic();
        startSpeakers();
    }

    public void endCall() {
        Log.i(Constants.AUDIO_CALL_LOG_TAG, "Ending call!");
        muteMic();
        muteSpeakers();
    }

    public void muteMeFromMeeting() {
        Log.i(Constants.AUDIO_CALL_LOG_TAG, "Muting from call!");
        if(speakers) {
            if (mic) {
                muteMic();
            } else {
                Log.i(Constants.AUDIO_CALL_LOG_TAG, "Already mute the call!");
            }
        } else {
            Log.i(Constants.AUDIO_CALL_LOG_TAG, "Already left the call!");
        }
    }

    public void unmuteMeFromMeeting() {
        Log.i(Constants.AUDIO_CALL_LOG_TAG, "Unmuting from call!");
        if(speakers) {
            if (mic) {
                Log.i(Constants.AUDIO_CALL_LOG_TAG, "Already unmute the call!");
            } else {
                startMic();
            }
        } else {
            Log.i(Constants.AUDIO_CALL_LOG_TAG, "Already left the call!");
        }
    }

    public void muteMic() {
        mic = false;
    }

    public void muteSpeakers() {
        speakers = false;
    }

    /**
     * Creates the thread for capturing and transmitting audio
      */
    public void startMic() {

        mic = true;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                int bytes_read = 0;
                int bytes_sent = 0;
                byte[] buf = new byte[BUF_SIZE];
                AudioRecord audioRecorder = null;
                MulticastSocket socket = null;

                try {
                    audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10);
                    Log.i(Constants.AUDIO_CALL_LOG_TAG, "Packet destination: " + multicastIP.toString());

                    socket = new MulticastSocket();
                    audioRecorder.startRecording();

                    while (mic) {
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        DatagramPacket packet = new DatagramPacket(buf, bytes_read, multicastIP, Constants.AUDIO_CALL_MULTICAST_PORT);
                        socket.send(packet);
                        bytes_sent += bytes_read;
                        Log.i(Constants.AUDIO_CALL_LOG_TAG, "Total bytes sent: " + bytes_sent);
                        Thread.sleep(Constants.SAMPLE_INTERVAL, 0);
                    }

                    mic = false;
                } catch (Exception e) {
                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "Exception: " + e);
                    mic = false;
                } finally {

                    if(audioRecorder !=null){
                        audioRecorder.stop();
                        audioRecorder.release();
                    }
                    if(socket !=null){
                        socket.disconnect();
                        socket.close();
                    }
                    return;
                }
            }
        });
        thread.start();
    }

    /**
     * Creates the thread for receiving the audio and playing the audio
     */
    public void startSpeakers() {

        if (!speakers) {
            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    AudioTrack track = null;
                    MulticastSocket socket = null;

                    try {
                        track = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                        track.play();

                        socket = new MulticastSocket(null);
                        socket.setReuseAddress(true);
                        socket.bind(new InetSocketAddress(Constants.AUDIO_CALL_MULTICAST_PORT));
                        socket.joinGroup(multicastIP);

                        byte[] buf = new byte[BUF_SIZE];

                        while (speakers) {
                            DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                            socket.receive(packet);

                            // check if the sender is the current host
                            if (!packet.getAddress().equals(myAddress)) {
                                Log.i(Constants.AUDIO_CALL_LOG_TAG, "Packet received: " + packet.getLength());
                                track.write(packet.getData(), 0, BUF_SIZE);
                            }
                        }
                        speakers = false;

                    } catch (Exception e) {
                        Log.e(Constants.AUDIO_CALL_LOG_TAG, "Exception: " + e);
                        speakers = false;

                    }  finally {

                        if(track !=null){
                            track.stop();
                            track.flush();
                            track.release();
                        }
                        if(socket !=null){
                            try {
                                socket.leaveGroup(multicastIP);
                            } catch (IOException e) {
                                Log.e(Constants.JOIN_MEETING_LOG_TAG, "Exception in listener for leaving the multicast group: " + e);
                            }
                            socket.disconnect();
                            socket.close();
                        }
                        return;
                    }
                }
            });
            receiveThread.start();
        }
    }
}
