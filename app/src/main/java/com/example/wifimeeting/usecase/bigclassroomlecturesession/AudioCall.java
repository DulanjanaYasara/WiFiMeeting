package com.example.wifimeeting.usecase.bigclassroomlecturesession;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.Role;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AudioCall {

    //Calculating the buffer size for an Android audio track (in bytes)
    private static final int BUF_SIZE = Constants.SAMPLE_INTERVAL * Constants.SAMPLE_INTERVAL * Constants.SAMPLE_SIZE * 2;
    private InetAddress address; // Address to call
    private InetAddress myAddress; // My IP Address
    private boolean mic = false; // Enable mic?
    private boolean speakers = false; // Enable speakers?
    private Boolean isMute;

    public AudioCall(InetAddress myAddress, InetAddress address, Boolean isMute) {
        this.myAddress = myAddress;
        this.isMute = isMute;
        this.address = address;
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

                try {
                    AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10);
                    Log.i(Constants.AUDIO_CALL_LOG_TAG, "Packet destination: " + address.toString());

                    DatagramSocket socket = new DatagramSocket();
                    audioRecorder.startRecording();

                    while (mic) {
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        DatagramPacket packet = new DatagramPacket(buf, bytes_read, address, Constants.AUDIO_CALL_BROADCAST_PORT);
                        socket.send(packet);
                        bytes_sent += bytes_read;
                        Log.i(Constants.AUDIO_CALL_LOG_TAG, "Total bytes sent: " + bytes_sent);
                        Thread.sleep(Constants.SAMPLE_INTERVAL, 0);
                    }

                    audioRecorder.stop();
                    audioRecorder.release();
                    socket.disconnect();
                    socket.close();
                    mic = false;
                } catch (InterruptedException e) {

                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "InterruptedException: " + e);
                    mic = false;
                } catch (SocketException e) {

                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "SocketException: " + e);
                    mic = false;
                } catch (UnknownHostException e) {

                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "UnknownHostException: " + e);
                    mic = false;
                } catch (IOException e) {

                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "IOException: " + e);
                    mic = false;
                } catch (SecurityException e) {

                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "SecurityException: " + e);
                    mic = false;
                } catch (Exception e) {

                    Log.e(Constants.AUDIO_CALL_LOG_TAG, "Exception: " + e);
                    mic = false;
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

                    try {
                        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                        track.play();

                        DatagramSocket socket = new DatagramSocket(Constants.AUDIO_CALL_BROADCAST_PORT);
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

                        socket.disconnect();
                        socket.close();
                        track.stop();
                        track.flush();
                        track.release();
                        speakers = false;
                    } catch (SocketException e) {

                        Log.e(Constants.AUDIO_CALL_LOG_TAG, "SocketException: " + e);
                        speakers = false;
                    } catch (IOException e) {

                        Log.e(Constants.AUDIO_CALL_LOG_TAG, "IOException: " + e);
                        speakers = false;
                    } catch (Exception e) {

                        Log.e(Constants.AUDIO_CALL_LOG_TAG, "Exception: " + e);
                        speakers = false;
                    }
                }
            });
            receiveThread.start();
        }
    }
}
