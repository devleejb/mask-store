package com.devleejb.maskstore.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.devleejb.maskstore.R;
import com.devleejb.maskstore.client.ClientHelper;
import com.devleejb.maskstore.client.Store;
import com.devleejb.maskstore.etc.PermissionManager;
import com.devleejb.maskstore.etc.StockHelper;
import com.devleejb.maskstore.ui.intro.IntroActivity;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    MapView mapView;
    LatLng latLng; // store을 불러온 시점의 위도 경도
    List<Store> stores; // 불러온 store 정보
    ArrayList<Marker> markers = new ArrayList<>();  // 활성화 되어있는 Marker
    InfoWindow activeInfoWindow; // 활성화 되어있는 InfoWindow
    View inc_setting;
    CheckBox cb_plenty, cb_some, cb_few, cb_empty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            startActivity(i);
                        }
                    });

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

        mapView = findViewById(R.id.mapView);
        inc_setting = findViewById(R.id.inc_setting);

        cb_plenty = inc_setting.findViewById(R.id.cb_plenty);
        cb_some = inc_setting.findViewById(R.id.cb_some);
        cb_few = inc_setting.findViewById(R.id.cb_few);
        cb_empty = inc_setting.findViewById(R.id.cb_empty);

        cb_plenty.setChecked(true);
        cb_some.setChecked(true);
        cb_few.setChecked(true);
        cb_empty.setChecked(true);

        mapView.getMapAsync(this);

        // 위치 권한
        PermissionManager.getPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        UiSettings uiSettings = naverMap.getUiSettings();
        CameraUpdate cameraUpdate = CameraUpdate.zoomTo(16.0);

        // 기본 줌
        naverMap.moveCamera(cameraUpdate);
        // 주요 건물 표시
        naverMap.setLayerGroupEnabled("LAYER_GROUP_BUILDING", true);
        // 나침반 제거
        uiSettings.setCompassEnabled(false);
        // 스케일 바 표시
        uiSettings.setScaleBarEnabled(true);
        // 현위치 버튼 표시
        uiSettings.setLocationButtonEnabled(true);
        // Location Source 설정
        naverMap.setLocationSource(new FusedLocationSource(this, 0));
        // 위치 추적이 활성화되고, 현위치 오버레이, 카메라의 좌표, 베어링이 사용자의 위치 및 방향을 따라 이동
        naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
        // 카메라 줌 레벨 제한
        naverMap.setMinZoom(14.0);
        naverMap.setMaxZoom(18.0);
        // 현재 위치
        latLng = new LatLng(0, 0);

        // 체크박스 체크 여부가 바뀌면 Marker를 새로 찍음
        cb_plenty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateMarkers(naverMap);
            }
        });
        cb_some.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateMarkers(naverMap);
            }
        });
        cb_few.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateMarkers(naverMap);
            }
        });
        cb_empty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateMarkers(naverMap);
            }
        });

        // 카메라의 위치가 변경 될 경우
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                if (naverMap.getCameraPosition().zoom >= 15.0) {
                    LatLng newLatLng = getCameraPosition(naverMap);

                    // 기존의 위치에서 3km 이상 벗어난 경우 새로운 정보를 불러옴
                    if (!isWithIn3km(getCameraPosition(naverMap))) {
                        latLng = newLatLng;
                        stores = ClientHelper.getStoresByLoc(newLatLng.latitude, newLatLng.longitude, 5000);
                    }

                    // 화면에 적절한 Marker를 표시함
                    updateMarkers(naverMap);
                }
            }
        });
    }

    // CameraPosition으로부터 위도 경도 정보를 반환함
    public LatLng getCameraPosition(NaverMap naverMap) {
        return new LatLng(naverMap.getCameraPosition().target.latitude, naverMap.getCameraPosition().target.longitude);
    }


    // Store를 불러온 후 반경 3km 이상 벗어났는지 확인
    public boolean isWithIn3km(LatLng newLatLng) {
        final double REFERANCE_LAT_X3 = 3 / 109.958489129649955;
        final double REFERANCE_LNG_X3 = 3 / 88.74;

        boolean withinSightMarkerLat = Math.abs(latLng.latitude - newLatLng.latitude) <= REFERANCE_LAT_X3;
        boolean withinSightMarkerLng = Math.abs(latLng.longitude - newLatLng.longitude) <= REFERANCE_LNG_X3;
        return withinSightMarkerLat && withinSightMarkerLng;
    }

    // 가로 1.5km, 세로 2.5km 안에 있는 정보만 표시
    public boolean isWithInSight(LatLng latLng, LatLng newLatLng) {
        final double REFERANCE_LAT = 1.5 / 109.958489129649955;
        final double REFERANCE_LNG_X2 = 2.5 / 88.74;

        boolean withinSightMarkerLat = Math.abs(latLng.latitude - newLatLng.latitude) <= REFERANCE_LAT;
        boolean withinSightMarkerLng = Math.abs(latLng.longitude - newLatLng.longitude) <= REFERANCE_LNG_X2;
        return withinSightMarkerLat && withinSightMarkerLng;
    }

    // 화면에 보이는 Marker만 표시
    public void updateMarkers(final NaverMap naverMap) {
        // Marker 모두 없앰
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).setMap(null);
        }

        // 활성화 Marker 삭제
        markers.clear();

        if (stores == null) {
            return;
        }

        for (int i = 0; i < stores.size(); i++) {
            final Store store = stores.get(i);
            LatLng storeLoc = new LatLng(store.lat, store.lng);

            // 시야 안에 존재
            if (isWithInSight(getCameraPosition(naverMap), storeLoc) && isInScope(store.remain_stat)) {
                final Marker marker = new Marker();

                marker.setPosition(storeLoc);
                marker.setMap(naverMap);
                marker.setIcon(MarkerIcons.BLACK);
                marker.setIconTintColor(StockHelper.stockToColor(store.remain_stat));
                marker.setMinZoom(15.0);
                markers.add(marker);

                // onClickListener
                marker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        // InfoWindow가 존재한다면 지우고, 그렇지 않다면 새로 만듦
                        if (marker.getInfoWindow() == null) {
                            InfoWindow infoWindow = new InfoWindow();

                            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
                                @NonNull
                                @Override
                                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                    return store.name;
                                }
                            });
                            infoWindow.open(marker);

                            // InfoWindow OnClick
                            infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                    builder.setTitle("판매처 정보")
                                            .setMessage("\n상호명 : " + store.name +
                                                    "\n\n주소 : " + store.addr +
                                                    "\n\n판매처 유형 : " + StockHelper.typeToKor(store.type) +
                                                    "\n\n재고 : " + StockHelper.stockToKor(store.remain_stat) +
                                                    "\n\n입고 시간 : " + StockHelper.timeToKor(store.stock_at) +
                                                    "\n\n갱신 시간 : " + StockHelper.timeToKor(store.created_at) + "\n");

                                    builder.setPositiveButton("OK", null);
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();

                                    return true;
                                }
                            });

                            // 기존 활성된 InfoWindow 지움
                            if (activeInfoWindow != null) {
                                activeInfoWindow.close();
                            }

                            activeInfoWindow = infoWindow;
                        } else {
                            marker.getInfoWindow().close();
                            activeInfoWindow = null;
                        }

                        return true;
                    }
                });
            }
        }
    }

    public boolean isInScope(String remain_stat) {
        if (remain_stat == null && cb_empty.isChecked()) {
            return true;
        } else if (remain_stat == null && !cb_empty.isChecked()) {
            return false;
        }

        switch (remain_stat) {
            case "plenty":
                if (cb_plenty.isChecked()) {
                    return true;
                } else {
                    return false;
                }
            case "some":
                if (cb_some.isChecked()) {
                    return true;
                } else {
                    return false;
                }
            case "few":
                if (cb_few.isChecked()) {
                    return true;
                } else {
                    return false;
                }
            case "empty":
            case "break":
                if (cb_empty.isChecked()) {
                    return true;
                } else {
                    return false;
                }
            default:
                return true;
        }
    }
}
