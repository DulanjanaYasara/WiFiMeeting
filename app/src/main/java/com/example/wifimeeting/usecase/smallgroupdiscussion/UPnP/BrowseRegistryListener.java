//package com.example.wifimeeting.usecase.smallgroupdiscussion.UPnP;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.example.wifimeeting.utils.Constants;
//
//import org.fourthline.cling.model.meta.Device;
//import org.fourthline.cling.model.meta.LocalDevice;
//import org.fourthline.cling.model.meta.RemoteDevice;
//import org.fourthline.cling.registry.DefaultRegistryListener;
//import org.fourthline.cling.registry.Registry;
//
//import java.util.LinkedHashMap;
//
//public class BrowseRegistryListener extends DefaultRegistryListener {
//
//    private Handler mHandler;
//    private UiUpdateListener uiUpdateListener;
//    private LinkedHashMap<String, Boolean> memberHashMap;
//
//    public interface UiUpdateListener {
//        void onDeviceListUpdated(LinkedHashMap<String, Boolean> memberHashMap);
//    }
//
//    public BrowseRegistryListener(UiUpdateListener uiUpdateListener) {
//        mHandler = new Handler(Looper.getMainLooper());
//        memberHashMap = new LinkedHashMap<>();
//        uiUpdateListener = uiUpdateListener;
//    }
//
//    /* Discovery performance optimization for very slow Android devices! */
//    @Override
//    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//        deviceAdded(device);
//    }
//
//    @Override
//
//    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
//        Log.e(Constants.BROWSE_REGISTRY_LISTENER_LOG_TAG, "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
//        deviceRemoved(device);
//    }
//    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */
//
//    @Override
//    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
//        deviceAdded(device);
//    }
//
//    @Override
//    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
//        deviceRemoved(device);
//    }
//
//    @Override
//    public void localDeviceAdded(Registry registry, LocalDevice device) {
//        deviceAdded(device);
//    }
//
//    @Override
//    public void localDeviceRemoved(Registry registry, LocalDevice device) {
//        deviceRemoved(device);
//    }
//
//    public void deviceAdded(final Device device) {
//
//        String nameValue =
//                device.getDetails() != null && device.getDetails().getFriendlyName() != null ?
//                        device.getDetails().getFriendlyName() :
//                        device.getDisplayString();
//        if (memberHashMap.containsKey(nameValue)) {
//
//
//            if ((memberHashMap.get(nameValue) != toggleSound)) {
//                memberHashMap.put(nameValue, toggleSound);
//                updateDeviceList();
//                Log.i(Constants.BROWSE_REGISTRY_LISTENER_LOG_TAG, "Device added (updation): " + ev.getInfo().getName());
//            }
//
//        } else {
//            memberHashMap.put(nameValue, toggleSound);
//            updateDeviceList();
//            Log.i(Constants.BROWSE_REGISTRY_LISTENER_LOG_TAG, "Service resolved (addition): " + ev.getInfo().getName());
//        }
//
//
//        runOnUiThread(new Runnable() {
//            public void run() {
//                DeviceDisplay d = new DeviceDisplay(device);
//                int position = listAdapter.getPosition(d);
//                if (position >= 0) {
//                    // Device already in the list, re-set new value at same position
//                    listAdapter.remove(d);
//                    listAdapter.insert(d, position);
//                } else {
//                    listAdapter.add(d);
//                }
//            }
//        });
//    }
//
//    public void deviceRemoved(final Device device) {
//        runOnUiThread(new Runnable() {
//            public void run() {
//                listAdapter.remove(new DeviceDisplay(device));
//            }
//        });
//    }
//}
