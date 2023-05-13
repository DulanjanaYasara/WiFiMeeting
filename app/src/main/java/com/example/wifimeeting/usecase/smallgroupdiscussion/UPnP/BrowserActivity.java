//package com.example.wifimeeting.usecase.smallgroupdiscussion.UPnP;
//
//import android.app.ListActivity;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ArrayAdapter;
//import android.widget.Toast;
//
//import com.example.wifimeeting.R;
//
//import org.fourthline.cling.android.AndroidUpnpService;
//import org.fourthline.cling.android.AndroidUpnpServiceImpl;
//import org.fourthline.cling.android.FixedAndroidLogHandler;
//import org.fourthline.cling.model.meta.Device;
//
//public class BrowserActivity extends ListActivity {
//    private ArrayAdapter<DeviceDisplay> listAdapter;
//    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
//    private AndroidUpnpService upnpService;
//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            upnpService = (AndroidUpnpService) service;
//            // Clear the list
//            listAdapter.clear();
//            // Get ready for future device advertisements
//            upnpService.getRegistry().addListener(registryListener);
//            // Now add all devices to the list we already know about
//            for (Device device : upnpService.getRegistry().getDevices()) {
//                registryListener.deviceAdded(device);
//            }
//            // Search asynchronously for all devices, they will respond soon
//            upnpService.getControlPoint().search();
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            upnpService = null;
//        }
//    };
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Fix the logging integration between java.util.logging and Android internal logging
//        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());
//
//        // Now you can enable logging as needed for various categories of Cling:
//        // Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);
//        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        setListAdapter(listAdapter);
//        // This will start the UPnP service if it wasn't already started
//        getApplicationContext().bindService(new Intent(this, AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (upnpService != null) {
//            upnpService.getRegistry().removeListener(registryListener);
//        }
//        // This will stop the UPnP service if nobody else is bound to it
//        getApplicationContext().unbindService(serviceConnection);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 0, 0, R.string.searchLAN).setIcon(android.R.drawable.ic_menu_search);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case 0:
//                if (upnpService == null) break;
//                Toast.makeText(this, R.string.searchingLAN, Toast.LENGTH_SHORT).show();
//                upnpService.getRegistry().removeAllRemoteDevices();
//                upnpService.getControlPoint().search();
//                break;
//        }
//        return false;
//    }
//}
