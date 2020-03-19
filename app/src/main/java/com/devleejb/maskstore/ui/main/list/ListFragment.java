package com.devleejb.maskstore.ui.main.list;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devleejb.maskstore.R;
import com.devleejb.maskstore.client.ClientHelper;
import com.devleejb.maskstore.client.Store;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.naver.maps.geometry.LatLng;

import java.util.List;

public class ListFragment extends Fragment {
    CardView cv_distance;
    TextView tv_distance, tv_info;
    RecyclerView rcv_list;
    LocationManager locationManager;
    ExtendedFloatingActionButton fab_update;
    ListAdapter listAdapter;
    ToggleButton tg_available;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cv_distance = view.findViewById(R.id.cv_distance);
        tv_distance = view.findViewById(R.id.tv_distance);
        rcv_list = view.findViewById(R.id.rcv_list);
        fab_update = view.findViewById(R.id.fab_update);
        tg_available = view.findViewById(R.id.tg_available);
        tv_info = view.findViewById(R.id.tv_info);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        cv_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), tv_distance);

                getActivity().getMenuInflater().inflate(R.menu.menu_distance, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        tv_distance.setTextColor(Color.BLACK);

                        switch (menuItem.getItemId()) {
                            case R.id.dis_100m:
                                tv_distance.setText("100m");
                                break;
                            case R.id.dis_500m:
                                tv_distance.setText("500m");
                                break;
                            case R.id.dis_1km:
                                tv_distance.setText("1km");
                                break;
                            case R.id.dis_3km:
                                tv_distance.setText("3km");
                                break;
                            case R.id.dis_5km:
                                tv_distance.setText("5km");
                                break;
                        }

                        updateList();

                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        rcv_list.setLayoutManager(new LinearLayoutManager(getContext()));

        // 새로고침
        fab_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_distance.getText().equals("나와 떨어진 거리를 선택하세요.")) {
                    Toast.makeText(getContext(), "나와 떨어진 거리를 선택하세요.", Toast.LENGTH_LONG).show();
                } else {
                    updateList();
                }
            }
        });

        // 활성화되면 재고가 존재하는 판매처만 보여줌
        tg_available.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rcv_list.getLayoutManager().scrollToPosition(0);

                if (b) {
                    tg_available.setTextColor(Color.WHITE);
                } else {
                    tg_available.setTextColor(Color.parseColor("#707070"));
                }

                if (listAdapter != null) {
                    if (b) {
                        listAdapter.setSoldOutView(true);
                        listAdapter.notifyDataSetChanged();
                    } else {
                        listAdapter.setSoldOutView(false);
                        listAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "나와 떨어진 거리를 선택하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public LatLng getLocation() {
        LatLng latLng = null;

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // 현재 위치를 알 수 있는 경우
            if (location != null) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            }
        }

        return latLng;
    }

    public int getMeter() {
        switch (tv_distance.getText().toString()) {
            case "100m":
                return 100;
            case "500m":
                return 500;
            case "1km":
                return 1000;
            case "3km":
                return 3000;
            case "5km":
                return 5000;
            default:
                return 0;
        }
    }

    public void updateList() {
        LatLng latLng = getLocation();
        List<Store> stores;

        if (latLng == null) {
            Toast.makeText(getContext(), "현재 위치를 알 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if (tv_distance.getText().toString().equals("나와 떨어진 거리")) {
            Toast.makeText(getContext(), "나와 떨어진 거리를 선택하세요.", Toast.LENGTH_LONG).show();
            return;
        }

        stores = ClientHelper.getStoresByLoc(latLng.latitude, latLng.longitude, 5000);

        if (stores == null) {
            Toast.makeText(getContext(), "정보를 읽어올 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if (listAdapter == null) {
            listAdapter = new ListAdapter(ClientHelper.getStoresByLoc(latLng.latitude, latLng.longitude, getMeter()), latLng, getContext(), tv_info);
            rcv_list.setAdapter(listAdapter);
        } else {
            listAdapter.setLatLng(latLng);
            listAdapter.setList(ClientHelper.getStoresByLoc(latLng.latitude, latLng.longitude, getMeter()));
            listAdapter.notifyDataSetChanged();
        }

        rcv_list.getLayoutManager().scrollToPosition(0);

        Toast.makeText(getContext(), "목록을 업데이트 하였습니다.", Toast.LENGTH_LONG).show();
    }
}
