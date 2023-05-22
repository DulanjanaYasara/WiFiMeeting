package com.example.wifimeeting.components.moduleitem;

import java.net.InetAddress;
import java.util.Objects;

public class ModuleDetailItem {

    private String moduleCode;
    private InetAddress multicastGroupAddress;
    private Long heartbeat;

    public ModuleDetailItem(String moduleCode, InetAddress multicastGroupAddress, Long heartbeat) {
        this.moduleCode = moduleCode;
        this.multicastGroupAddress = multicastGroupAddress;
        this.heartbeat = heartbeat;
    }

    public ModuleDetailItem(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public InetAddress getMulticastGroupAddress() {
        return multicastGroupAddress;
    }

    public Long getHeartbeat() {
        return heartbeat;
    }

    @Override
    public String toString() {
        return moduleCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModuleDetailItem)) return false;
        ModuleDetailItem other = (ModuleDetailItem) obj;
        return Objects.equals(moduleCode, other.moduleCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleCode);
    }
}
