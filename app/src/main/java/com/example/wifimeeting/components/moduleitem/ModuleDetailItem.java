package com.example.wifimeeting.components.moduleitem;

public class ModuleDetailItem {

    private String moduleCode;
    private String multicastGroupAddress;
    private Long heartbeat;

    public ModuleDetailItem(String moduleCode, String multicastGroupAddress, Long heartbeat) {
        this.moduleCode = moduleCode;
        this.multicastGroupAddress = multicastGroupAddress;
        this.heartbeat = heartbeat;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getMulticastGroupAddress() {
        return multicastGroupAddress;
    }

    public Long getHeartbeat() {
        return heartbeat;
    }

    @Override
    public String toString() {
        return moduleCode;
    }
}
