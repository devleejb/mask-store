package com.devleejb.maskstore;

import androidx.fragment.app.FragmentManager;

import com.devleejb.maskstore.ui.main.info.InfoFragment;
import com.devleejb.maskstore.ui.main.list.ListFragment;
import com.devleejb.maskstore.ui.main.map.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.map.NaverMap;

public class Application {
    public static MapFragment mapFragment;
    public static ListFragment listFragment;
    public static InfoFragment infoFragment;
    public static NaverMap naverMap;
    public static FragmentManager fragmentManager;
    public static BottomNavigationView bnv_main;
}
