package com.example.wifimeeting.utils;

public class Constants {

    public static final int SMALL_GROUP_DISCUSSION_MEMBER_MAX_COUNT = 2;
    public static final String GROUP_SUFFIX = "_Group";
    public static final String STRING_SEPARATOR = "#";
    public static final int PASSWORD_MAX_LENGTH = 12;
    public static final int MUTE_UNMUTE_BUTTON_THRESHOLD_MILLISECONDS = 1000;
    public static final int GROUP_DISCUSSION_HEARTBEAT_INTERVAL = 5000; // 5 seconds
    public static final int GROUP_DISCUSSION_DISCOVERY_TIMEOUT_MILLISECONDS = 15000;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    public static final int CREATE_MEETING_BROADCAST_INTERVAL = 10000;


    //    Lecturer Credentials
    public static final String SEC_USERNAME = "chami";
    public static final String SEC_PWD = "1234";

    //Actions
    public static final String JOIN_ACTION = "JO";
    public static final String PRESENT_ACTION = "PR";
    public static final String LEAVE_ACTION = "LE";
    public static final String ABSENT_ACTION = "AB";
    public static final String MUTE_ACTION = "MU";
    public static final String CREATE_ACTION = "CR";
    public static final String END_ACTION = "EN";

    public static final int DEFAULT_AUDIO_CALL_PORT = 50000;

    //Port Allocation for Broadcast
    public static final int MARK_PRESENCE_BROADCAST_PORT = 50001;
    public static final int MARK_ABSENCE_BROADCAST_PORT = 50002;
    public static final int MUTE_UNMUTE_BROADCAST_PORT = 50003;
    public static final int MARK_CREATE_BROADCAST_PORT = 50004;

    //Port Allocation for Multicast
    public static final int MARK_PRESENCE_MULTICAST_PORT = 50001;
    public static final int MARK_ABSENCE_MULTICAST_PORT = 50002;
    public static final int MUTE_UNMUTE_MULTICAST_PORT = 50003;
    public static final int MARK_END_MULTICAST_PORT = 50005;
    //Audio Call configurations
    public static final int SAMPLE_RATE = 8000; // Hertz
    public static final int SAMPLE_INTERVAL = 20; // Milliseconds
    public static final int SAMPLE_SIZE = 2; // Bytes

    public static final int BROADCAST_BUF_SIZE = 1024;
    public static final int MULTICAST_BUF_SIZE = 1024;

    //Logging Tags
    public static final String LECTURE_SESSION_PAGE_LOG_TAG = "LectureSessionPage";
    public static final String GROUP_DISCUSSION_PAGE_LOG_TAG = "GroupDiscussionPage";
    public static final String GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG = "GroupDiscussionLobbyPage";
    public static final String AUDIO_CALL_LOG_TAG = "AudioCall";
    public static final String JOIN_MEETING_LOG_TAG = "JoinMeeting";
    public static final String LEAVE_MEETING_LOG_TAG = "LeaveMeeting";
    public static final String MUTE_UNMUTE_LOG_TAG = "MuteUnmute";
    public static final String CREATE_MEETING_LOG_TAG = "CreateMeeting";
    public static final String END_MEETING_LOG_TAG = "EndMeeting";

    public static final String MEMBER_REGISTRY_LISTENER_LOG_TAG = "MemberRegistryListener";
    public static final String PEER_DISCOVERY_MANAGER_LOG_TAG = "PeerDiscoveryManagerPage";
    public static final int SERVICE_INFO_PORT = 8856;
    public static final String SERVICE_INFO_TYPE = "_sgd_jmdns._udp.local.";
    public static final String SERVICE_INFO_NAME = "sgd_jmdns_service";
    public static final int DISCOVERY_INTERVAL_MS = 10000;

}
