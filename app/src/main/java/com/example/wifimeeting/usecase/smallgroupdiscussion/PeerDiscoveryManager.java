//package com.example.wifimeeting.usecase.smallgroupdiscussion;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import com.example.wifimeeting.utils.Constants;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.util.Hashtable;
//import java.util.LinkedHashMap;
//
//import javax.jmdns.JmDNS;
//import javax.jmdns.ServiceEvent;
//import javax.jmdns.ServiceInfo;
//import javax.jmdns.ServiceListener;
//
//public class PeerDiscoveryManager {
//
//    private JmDNS jmdns = null;
//    private ServiceListener listener = null;
//    private ServiceInfo serviceInfo;
//    private Handler mHandler;
//    private Listener mListener;
//    private LinkedHashMap<String, Boolean> memberHashMap;
//    private InetAddress myIp;
//    PeerDiscovery peerDiscovery;
//
//    public interface Listener {
//        void onDeviceListUpdated(LinkedHashMap<String, Boolean> memberHashMap);
//    }
//
//    public PeerDiscoveryManager(Listener listener, InetAddress ipAddress) {
//        mHandler = new Handler(Looper.getMainLooper());
//        memberHashMap = new LinkedHashMap<>();
//        mListener = listener;
//        myIp = ipAddress;
//    }
//
//    public void registerService(String memberName, String toggleSound) {
//
//        Thread joinMeetingThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (jmdns == null) {
//                        createJmDNS();
//                        String props = memberName + toggleSound;
//
//                        serviceInfo = ServiceInfo.create(Constants.SERVICE_INFO_TYPE, Constants.SERVICE_INFO_NAME, Constants.SERVICE_INFO_PORT, props);
//                        jmdns.registerService(serviceInfo);
//                        Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Registered the service :" + serviceInfo.getName());
//                    }
//                } catch (Exception e) {
//                    Log.e(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Exception in Register Service using mDNS: " + e);
//                }
//            }
//        });
//        joinMeetingThread.start();
//    }
//
//    private synchronized void createJmDNS() {
//        if (jmdns == null) {
//            try {
//                jmdns = JmDNS.create(myIp);
//                Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Created JmDNS");
//            } catch (IOException e) {
//                Log.e(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Exception in creation of mDNS: " + e);
//            }
//        }
//    }
//
//    public void unRegisterService(String memberName) {
//
//        Thread leaveMeetingThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (jmdns != null) {
//                        if (listener != null) {
//                            jmdns.removeServiceListener(Constants.SERVICE_INFO_TYPE, listener);
//                            listener = null;
//                        }
//                        if (peerDiscovery  != null) peerDiscovery.stop();
//                        if(serviceInfo!=null){
//                            jmdns.unregisterService(serviceInfo);
//                        }
//                        jmdns.unregisterAllServices();
//                        jmdns.close();
//
//                        jmdns = null;
//                        Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Unregistered the service");
//                    }
//                    mHandler.removeCallbacksAndMessages(null);
//                } catch (Exception ex) {
//                    Log.e(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Exception in Unregister Service using mDNS: " + ex);
//                }
//            }
//        });
//        leaveMeetingThread.start();
//    }
//
//    public void toggleSound(String memberName, String toggleSound) {
//
//        Thread toggleSoundThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (serviceInfo != null) {
//                        // Update the service properties
//                        String updatedProps = memberName + toggleSound;
//                        serviceInfo.setText(updatedProps.getBytes());
//
//                        jmdns.unregisterService(serviceInfo);
//                        jmdns.registerService(serviceInfo);
//                        Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Service props changed and restarted");
//                    }
//
//                    // Wait for a while to see the updated properties in action
//                    Thread.sleep(500);
//                } catch (Exception ex) {
//                    Log.e(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Exception in Toggle Sound using mDNS: " + ex);
//                }
//            }
//        });
//        toggleSoundThread.start();
//    }
//
//    public void startDiscovery(){
//        peerDiscovery = new PeerDiscovery();
//        Thread startDiscoveryThread = new Thread(peerDiscovery);
//        startDiscoveryThread.start();
//    }
//
//    public class PeerDiscovery implements Runnable {
//        private volatile boolean exit = false;
//
//        @Override
//        public void run() {
//            try {
//                if (jmdns == null) {
//                    createJmDNS();
//                }
//                while (!exit) {
//                    jmdns.addServiceListener(Constants.SERVICE_INFO_TYPE, new ServiceListener() {
//                        @Override
//                        public void serviceResolved(ServiceEvent ev) {
//                            ServiceInfo serviceInfo = ev.getInfo();
//                            String props = serviceInfo.getTextString();
//
//                            if(props.equals(""))
//                                return;
//
//                            String nameValue = props.substring(0,props.length()-1);
//                            Boolean toggleSound = props.endsWith("Y");
//
//                            if (memberHashMap.containsKey(nameValue)){
//                                if((memberHashMap.get(nameValue) != toggleSound)) {
//                                    memberHashMap.put(nameValue, toggleSound);
//                                    updateDeviceList();
//                                    Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Service resolved (updation): " + ev.getInfo().getName());
//                                }
//
//                            } else {
//                                memberHashMap.put(nameValue, toggleSound);
//                                updateDeviceList();
//                                Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Service resolved (addition): " + ev.getInfo().getName());
//                            }
//                        }
//
//                        @Override
//                        public void serviceRemoved(ServiceEvent ev) {
//                            ServiceInfo serviceInfo = ev.getInfo();
//                            String props = serviceInfo.getTextString();
//
//                            if(props.equals(""))
//                                return;
//
//                            String nameValue = props.substring(0,props.length()-1);
//                            if (memberHashMap.containsKey(nameValue)) {
//                                memberHashMap.remove(nameValue);
//                                updateDeviceList();
//                                Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Service removed: " + ev.getInfo().getName());
//                            }
//                        }
//
//                        @Override
//                        public void serviceAdded(ServiceEvent event) {
//                            jmdns.requestServiceInfo(event.getType(), event.getName(), 1000);
////                            Log.i(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Service added: " + event.getInfo().getName());
//                        }
//                    });
//                    Thread.sleep(Constants.DISCOVERY_INTERVAL_MS);
//                }
//
//            } catch (Exception e) {
//                Log.e(Constants.PEER_DISCOVERY_MANAGER_LOG_TAG, "Exception start discovery using mDNS: " + e);
//            }
//        }
//
//        public void stop() {
//            exit = true;
//        }
//    }
//
//    private void updateDeviceList() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                mListener.onDeviceListUpdated(memberHashMap);
//            }
//        });
//    }
//}