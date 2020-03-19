package com.devleejb.maskstore.ui.main.map;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.devleejb.maskstore.Application;
import com.devleejb.maskstore.R;
import com.devleejb.maskstore.client.ClientHelper;
import com.devleejb.maskstore.client.Store;
import com.devleejb.maskstore.etc.PermissionManager;
import com.devleejb.maskstore.etc.StockHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    MapView mapView;
    LatLng loadedLatLng; // store을 불러온 시점의 위도 경도
    List<Store> stores; // 불러온 store 정보
    ExtendedFloatingActionButton fab_update;
    CircleOverlay currentCircle;
    ArrayList<Marker> activeMarker;
    Marker openedMarker = null;
    boolean isVisible = true; // 이전 줌 상태에서 판매처가 표시 되었는지 여부
    CardView cv_caution;
    Button btn_zoom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        fab_update = view.findViewById(R.id.fab_update);
        cv_caution = view.findViewById(R.id.cv_caution);
        btn_zoom = view.findViewById(R.id.btn_zoom);

        mapView.getMapAsync(this);

        cv_caution.setVisibility(View.GONE);

        activeMarker = new ArrayList<>();

        // 위치 권한
        PermissionManager.getPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        UiSettings uiSettings = naverMap.getUiSettings();
        CameraUpdate cameraUpdate = CameraUpdate.zoomTo(16.0);

        Application.naverMap = naverMap;
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
        naverMap.setMinZoom(10.0);
        naverMap.setMaxZoom(19.0);
        // 현재 위치
        loadedLatLng = new LatLng(0, 0);

        // 새로고침 버튼
        fab_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadedLatLng = naverMap.getCameraPosition().target;

                stores = ClientHelper.getStoresByLoc(loadedLatLng.latitude, loadedLatLng.longitude, 5000);

                // 인터넷 X
                if (stores == null) {
                    Toast.makeText(getContext(), "정보를 읽어올 수 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (naverMap.getCameraPosition().zoom >= 15.0) {
                    Toast.makeText(getContext(), "목록을 업데이트 하였습니다.", Toast.LENGTH_LONG).show();
                    updateMarkerSight(naverMap);
                } else if (naverMap.getCameraPosition().zoom >= 13.0) {
                    Toast.makeText(getContext(), "목록을 업데이트 하였습니다.", Toast.LENGTH_LONG).show();
                    updateMarkerCircle(naverMap);
                } else {
                    Toast.makeText(getContext(), "해당 확대 레벨에서는 판매처 검색을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // 확대 버튼
        btn_zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraUpdate cameraUpdate = CameraUpdate.zoomTo(13.0);

                naverMap.moveCamera(cameraUpdate);
            }
        });

        // 카메라의 위치가 변경 될 경우
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                eraseCircle();

                if (naverMap.getCameraPosition().zoom >= 15.0) {
                    if (!isVisible) {
                        isVisible = true;

                        cv_caution.setVisibility(View.GONE);
                    }

                    updateMarkerSight(naverMap);
                } else if (naverMap.getCameraPosition().zoom >= 13.0) {
                    if (!isVisible) {
                        isVisible = true;

                        cv_caution.setVisibility(View.GONE);
                    }

                    currentCircle = new CircleOverlay();

                    currentCircle.setCenter(naverMap.getCameraPosition().target);
                    currentCircle.setRadius(600);
                    currentCircle.setColor(ColorUtils.setAlphaComponent(Color.parseColor("#303F9F"), 100));
                    currentCircle.setMap(naverMap);

                    updateMarkerCircle(naverMap);
                } else {
                    if (isVisible) {
                        Toast.makeText(getContext(), "해당 확대 레벨에서는 판매처 검색을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                        isVisible = false;

                        cv_caution.setVisibility(View.VISIBLE);

                        clearMarkers();
                    }
                }
            }
        });
    }

    // 최근에 표시된 Circle을 지움
    public void eraseCircle() {
        if (currentCircle != null) {
            currentCircle.setMap(null);
            currentCircle = null;
        }
    }

    // 활성화 되어있는 Marker 해제
    public void clearMarkers() {
        for (int i = 0; i < activeMarker.size(); i++) {
            activeMarker.get(i).setMap(null);
        }

        activeMarker.clear();
    }

    // Zoom이 15.0 이상
    public void updateMarkerSight(final NaverMap naverMap) {
        LatLng latLng = naverMap.getCameraPosition().target;

        clearMarkers();

        // 최근에 불러온 위치와 4000미터 이상 벗어난 경우 새로 정보를 받아옴
        if (loadedLatLng.distanceTo(latLng) >= 4000) {
            stores = ClientHelper.getStoresByLoc(latLng.latitude, latLng.longitude, 5000);

            loadedLatLng = latLng;
        }

        if (stores == null) {
            Toast.makeText(getContext(), "정보를 읽어올 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        // Marker 설정
        for (int i = 0; i < stores.size(); i++) {
            Store store = stores.get(i);
            LatLng storeLatLng = new LatLng(store.lat, store.lng);

            // 현재 화면 안에 존재한다면
            if (naverMap.getContentBounds().contains(storeLatLng)) {
                Marker marker = new Marker();

                // Marker 위치 설정
                marker.setPosition(storeLatLng);

                // Marker 색깔 설정
                marker.setIcon(MarkerIcons.BLACK);
                marker.setIconTintColor(StockHelper.stockToColor(store.remain_stat));

                // 회색 Marker 투명도 설정
                if (marker.getIconTintColor() == Color.parseColor("#707070")) {
                    marker.setAlpha(0.5f);
                }

                setInfoWindow(marker, store);
                activeMarker.add(marker);
            }
        }

        // Marker 띄우기
        for (int i = 0; i < activeMarker.size(); i++) {
            activeMarker.get(i).setMap(naverMap);
        }
    }

    public void updateMarkerCircle(NaverMap naverMap) {
        LatLng latLng = naverMap.getCameraPosition().target;
        LatLng circleCenter = currentCircle.getCenter();

        clearMarkers();

        // 최근에 불러온 위치와 4000미터 이상 벗어난 경우 새로 정보를 받아옴
        if (loadedLatLng.distanceTo(latLng) >= 4000) {
            stores = ClientHelper.getStoresByLoc(latLng.latitude, latLng.longitude, 5000);

            loadedLatLng = latLng;
        }

        if (stores == null) {
            Toast.makeText(getContext(), "정보를 읽어올 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        // Marker 설정
        for (int i = 0; i < stores.size(); i++) {
            Store store = stores.get(i);
            LatLng storeLatLng = new LatLng(store.lat, store.lng);

            // 현재 원 안에 존재한다면
            if (circleCenter.distanceTo(storeLatLng) <= 500) {
                Marker marker = new Marker();

                // Marker 위치 설정
                marker.setPosition(storeLatLng);

                // Marker 색깔 설정
                marker.setIcon(MarkerIcons.BLACK);
                marker.setIconTintColor(StockHelper.stockToColor(store.remain_stat));

                // 회색 Marker 투명도 설정
                if (marker.getIconTintColor() == Color.parseColor("#707070")) {
                    marker.setAlpha(0.5f);
                }

                setInfoWindow(marker, store);

                activeMarker.add(marker);
            }
        }

        // Marker 띄우기
        for (int i = 0; i < activeMarker.size(); i++) {
            activeMarker.get(i).setMap(naverMap);
        }
    }

    // Marker를 누르면 InfoWindow가 등장하고 InfoWindow를 누르면 자세한 정보 등장
    public void setInfoWindow(final Marker marker, final Store store) {
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                if (openedMarker != null && openedMarker.getInfoWindow() != null) {
                    // 활성화 되어 있는 다른 Marker의 InfoWindow 닫기
                    openedMarker.getInfoWindow().close();
                    openedMarker = null;
                }

                // InfoWindow가 열려있지 않다면 InfoWindow 표시
                if (marker.getInfoWindow() == null) {
                    InfoWindow infoWindow = new InfoWindow();
                    openedMarker = marker;

                    infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                        @NonNull
                        @Override
                        public CharSequence getText(@NonNull InfoWindow infoWindow) {
                            return store.name;
                        }
                    });

                    // InfoWindow가 눌리면 자세한 정보를 띄움
                    infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

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

                            return false;
                        }
                    });

                    infoWindow.open(marker);
                } else {
                    // InfoWindow가 열려있다면 닫기

                    openedMarker = null;
                    marker.getInfoWindow().close();
                }

                return false;
            }
        });
    }

}


