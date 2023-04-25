package com.example.wifimeeting;

import static android.Manifest.permission.RECORD_AUDIO;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.page.MeetingPage;
import com.example.wifimeeting.page.StartupPage;
import com.example.wifimeeting.utils.Constants;

public class MainActivity extends AppCompatActivity implements NavigationHost {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            if(checkPermissions()) {
                appStartUp();
            } else {
                requestPermissions();
            }
        }
    }

    private void appStartUp(){
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, new StartupPage())
                .commit();
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment       Fragment to navigate to.
     * @param addToBackstack Whether or not the current fragment should be added to the backstack.
     */
    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    public void onBackPressed() {

        if(MeetingPage.backPressedListener!=null){
            MeetingPage.backPressedListener.onBackPressed();
        } else
            super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        appStartUp();
    }

    public boolean checkPermissions() {
        int permissionForRecordAudio = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return permissionForRecordAudio == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO}, Constants.REQUEST_AUDIO_PERMISSION_CODE);
    }
}
