package com.example.wifimeeting.components.groupitem;

import java.net.InetAddress;

public class DiscussionGroupItem implements Comparable<DiscussionGroupItem>{

    private String groupName;
    private InetAddress multicastGroupAddress;
    private String noOfMembers;

    public DiscussionGroupItem(String groupName, InetAddress multicastGroupAddress, String noOfMembers) {
        this.groupName = groupName;
        this.multicastGroupAddress = multicastGroupAddress;
        this.noOfMembers = noOfMembers;
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
}
