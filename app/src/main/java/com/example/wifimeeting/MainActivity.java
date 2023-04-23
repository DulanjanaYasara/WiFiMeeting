package com.example.wifimeeting;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.page.MeetingPage;
import com.example.wifimeeting.page.StartupPage;

public class MainActivity extends AppCompatActivity implements NavigationHost {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new StartupPage())
                    .commit();
        }
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
}
