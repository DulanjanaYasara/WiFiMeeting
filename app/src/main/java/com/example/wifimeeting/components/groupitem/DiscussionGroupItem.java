package com.example.wifimeeting.components.groupitem;

import java.net.InetAddress;
import java.util.Objects;

public class DiscussionGroupItem implements Comparable<DiscussionGroupItem>{

    private String groupName;
    private InetAddress multicastGroupAddress;
    private String noOfMembers;
    private Long heartBeatReceivedTime;

    public DiscussionGroupItem(String groupName, InetAddress multicastGroupAddress, String noOfMembers, Long heartBeatReceivedTime) {
        this.groupName = groupName;
        this.multicastGroupAddress = multicastGroupAddress;
        this.noOfMembers = noOfMembers;
        this.heartBeatReceivedTime = heartBeatReceivedTime;
    }

    public DiscussionGroupItem(String groupName){
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public InetAddress getMulticastGroupAddress() {
        return multicastGroupAddress;
    }

    public void setMulticastGroupAddress(InetAddress multicastGroupAddress) {
        this.multicastGroupAddress = multicastGroupAddress;
    }

    public String getNoOfMembers() {
        return noOfMembers;
    }

    public void setNoOfMembers(String noOfMembers) {
        this.noOfMembers = noOfMembers;
    }

    public Long getHeartBeatReceivedTime() {
        return heartBeatReceivedTime;
    }

    public void setHeartBeatReceivedTime(Long heartBeatReceivedTime) {
        this.heartBeatReceivedTime = heartBeatReceivedTime;
    }

    @Override
    public String toString() {
        return groupName;
    }

    @Override
    public int compareTo(DiscussionGroupItem item) {
        // Compare based on no of members
        if (Integer.parseInt(this.noOfMembers) > Integer.parseInt(item.getNoOfMembers())) {
            return 1;
        } else if (Integer.parseInt(this.noOfMembers) < Integer.parseInt(item.getNoOfMembers())) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DiscussionGroupItem)) return false;
        DiscussionGroupItem other = (DiscussionGroupItem) obj;
        return Objects.equals(groupName, other.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName);
    }
}
