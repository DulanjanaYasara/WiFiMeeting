package com.example.wifimeeting.utils;

public class Constants {

    public static final String DEFAULT_PORT = "9000";
    public static final int SMALL_GROUP_DISCUSSION_MEMBER_MAX_COUNT = 2;
    public static final String MEMBERS_SEPARATOR = ", ";
    public static final int PASSWORD_MAX_LENGTH = 12;
    public static final int MUTE_UNMUTE_BUTTON_THRESHOLD_MILLISECONDS = 3000;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    //    Lecturer Credentials
    public static final String SEC_USERNAME = "chami";
    public static final String SEC_PWD = "1234";

    //Actions
    public static final String JOIN = "JO";
    public static final String PRESENT = "PR";
    public static final String LEAVE = "LE";
    public static final String ABSENT = "AB";
    public static final String MUTE = "MU";

    //Port Allocation
    public static final String AUDIO_CALL_BROADCAST_PORT = "50000";
    public static final String MARK_PRESENCE_BROADCAST_PORT = "50001";
    public static final String MARK_ABSENCE_BROADCAST_PORT = "50002";
    public static final String MUTE_UNMUTE_BROADCAST_PORT = "50003";

}
