package com.devleejb.maskstore.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.devleejb.maskstore.Application;
import com.devleejb.maskstore.R;
import com.devleejb.maskstore.ui.main.info.InfoFragment;
import com.devleejb.maskstore.ui.main.list.ListFragment;
import com.devleejb.maskstore.ui.main.map.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private ListFragment listFragment;
    private InfoFragment infoFragment;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        mapFragment = new MapFragment();
        listFragment = new ListFragment();
        infoFragment = new InfoFragment();

        Application.mapFragment = mapFragment;
        Application.listFragment = listFragment;
        Application.fragmentManager = fragmentManager;
        Application.infoFragment = infoFragment;

        bottomNavigationView = findViewById(R.id.bnv_main);
        Application.bnv_main = bottomNavigationView;

        fragmentManager.beginTransaction().replace(R.id.fl_main, mapFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_main, listFragment).hide(listFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_main, infoFragment).hide(infoFragment).commit();

        // BottomNavigationView OnClick
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map:
                        fragmentManager.beginTransaction().show(mapFragment).commit();
                        fragmentManager.beginTransaction().hide(listFragment).commit();
                        fragmentManager.beginTransaction().hide(infoFragment).commit();
                        break;
                    case R.id.list:
                        fragmentManager.beginTransaction().hide(mapFragment).commit();
                        fragmentManager.beginTransaction().show(listFragment).commit();
                        fragmentManager.beginTransaction().hide(infoFragment).commit();
                        break;
                    case R.id.information:
                        fragmentManager.beginTransaction().hide(mapFragment).commit();
                        fragmentManager.beginTransaction().hide(listFragment).commit();
                        fragmentManager.beginTransaction().show(infoFragment).commit();
                }

                return true;
            }
        });
    }
}


