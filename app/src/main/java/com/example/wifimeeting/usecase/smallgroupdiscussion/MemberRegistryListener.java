package com.example.wifimeeting.usecase.smallgroupdiscussion;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.wifimeeting.utils.Constants;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.LinkedHashMap;

public class MemberRegistryListener extends DefaultRegistryListener {

    private Handler mHandler;
    private UiUpdateListener uiUpdateListener;
    private LinkedHashMap<String, Boolean> memberHashMap;

    public interface UiUpdateListener {
        void onDeviceListUpdated(LinkedHashMap<String, Boolean> memberHashMap);
    }

    public MemberRegistryListener(UiUpdateListener listener) {
        mHandler = new Handler(Looper.getMainLooper());
        memberHashMap = new LinkedHashMap<>();
        uiUpdateListener = listener;
    }

    /* Discovery performance optimization for very slow Android devices! */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Discovery started: " + device.getDisplayString());
        deviceAdded(device);
    }

    @Override

    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        Log.e(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
        deviceRemoved(device);
    }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Remote device available: " + device.getDisplayString());
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Remote device removed: " + device.getDisplayString());
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Local device added: " + device.getDisplayString());
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Local device removed: "+ device.getDisplayString());
        deviceRemoved(device);
    }

    public void deviceAdded(final Device device) {
        String nameValue =
                device.getDetails() != null && device.getDetails().getFriendlyName() != null ?
                        device.getDetails().getFriendlyName() :
                        device.getDisplayString();
        memberHashMap.put(nameValue, true);
        updateDeviceList();

        if (memberHashMap.containsKey(nameValue)) {
            Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Member updated: " + nameValue);
        } else {
            Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Member added: " + nameValue);
        }

    }

    public void deviceRemoved(final Device device) {
        String nameValue =
                device.getDetails() != null && device.getDetails().getFriendlyName() != null ?
                        device.getDetails().getFriendlyName() :
                        device.getDisplayString();
        memberHashMap.remove(nameValue);
        updateDeviceList();
        Log.i(Constants.MEMBER_REGISTRY_LISTENER_LOG_TAG, "Member removed: " + nameValue);
    }

    private void updateDeviceList() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                uiUpdateListener.onDeviceListUpdated(memberHashMap);
            }
        });
    }
}
