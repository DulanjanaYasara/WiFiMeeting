package com.example.wifimeeting.components.membercard;

public class MemberCharacteristics {

    private boolean isMute;
    private Long lastMemberJoinTime;

    public MemberCharacteristics(boolean isMute) {
        this.isMute = isMute;
        this.lastMemberJoinTime = System.currentTimeMillis();
    }

    public Long getLastMemberJoinTime() {
        return lastMemberJoinTime;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setIsMute(boolean isMute) {
        this.isMute = isMute;
    }
}
