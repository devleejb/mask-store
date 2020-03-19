package com.devleejb.maskstore.ui.main.list;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.devleejb.maskstore.Application;
import com.devleejb.maskstore.R;
import com.devleejb.maskstore.client.Store;
import com.devleejb.maskstore.etc.StockHelper;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;

import java.util.Collections;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private Context context;
    private List<Store> storeList;
    private LatLng latLng;
    private boolean isSoldOutView;
    private TextView tv_info;
    private int availableSize;

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView tv_store, tv_stock, tv_stock_time, tv_update_time, tv_distance;
        Button btn_map, btn_more;
        CardView cv_mask;

        public ListViewHolder(View v) {
            super(v);

            tv_store = v.findViewById(R.id.tv_store);
            tv_stock = v.findViewById(R.id.tv_stock);
            tv_stock_time = v.findViewById(R.id.tv_stock_time);
            tv_update_time = v.findViewById(R.id.tv_update_time);
            tv_distance = v.findViewById(R.id.tv_distance);

            btn_map = v.findViewById(R.id.btn_map);
            btn_more = v.findViewById(R.id.btn_more);

            cv_mask = v.findViewById(R.id.cv_mask);
        }
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setList(List<Store> storeList) {
        Location src = new Location("src");

        this.storeList = storeList;
        availableSize = 0;

        src.setLatitude(latLng.latitude);
        src.setLongitude(latLng.longitude);

        // 거리 계산
        for (int i = 0; i < storeList.size(); i++) {
            Store store = storeList.get(i);
            Location dest = new Location("dest");

            dest.setLatitude(store.lat);
            dest.setLongitude(store.lng);

            store.distance = StockHelper.getDistance(src, dest);

            // 재고가 존재하는 판매처
            if (!(store.remain_stat == null || store.remain_stat.equals("empty") || store.remain_stat.equals("break"))) {
                availableSize++;
            }
        }

        // 정렬하여 사용
        Collections.sort(storeList);
    }

    public void setSoldOutView(boolean isSoldOutView) {
        this.isSoldOutView = isSoldOutView;
    }


    public ListAdapter(List<Store> storeList, LatLng latLng, Context context, TextView tv_info) {
        Location src = new Location("src");

        availableSize = 0;

        src.setLatitude(latLng.latitude);
        src.setLongitude(latLng.longitude);

        this.storeList = storeList;
        this.latLng = latLng;
        this.context = context;
        this.tv_info = tv_info;

        // 거리 계산
        for (int i = 0; i < storeList.size(); i++) {
            Store store = storeList.get(i);
            Location dest = new Location("dest");

            dest.setLatitude(store.lat);
            dest.setLongitude(store.lng);

            store.distance = StockHelper.getDistance(src, dest);

            // 재고가 존재하는 판매처
            if (!(store.remain_stat == null || store.remain_stat.equals("empty") || store.remain_stat.equals("break"))) {
                availableSize++;
            }
        }

        // 정렬하여 사용
        Collections.sort(storeList);
    }

    @Override
    public int getItemCount() {
        if (isSoldOutView) {
            tv_info.setText("조건을 만족하는 판매처 " + availableSize + "곳");
        } else {
            tv_info.setText("조건을 만족하는 판매처 " + storeList.size() + "곳");
        }

        return storeList.size();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_mask, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ListViewHolder holder, int position) {
        final Store store = storeList.get(position);

        if (isSoldOutView && (store.remain_stat == null || store.remain_stat.equals("empty") || store.remain_stat.equals("break"))) {
            holder.cv_mask.setVisibility(View.GONE);
        } else {
            Location src = new Location("src");
            Location dest = new Location("dest");

            // 기준 위도 경도
            src.setLatitude(latLng.latitude);
            src.setLongitude(latLng.longitude);

            // 도착지 위도 경도
            dest.setLatitude(store.lat);
            dest.setLongitude(store.lng);

            holder.tv_store.setText(store.name);
            holder.tv_stock.setText(StockHelper.stockToKor(store.remain_stat));
            holder.tv_stock_time.setText("입고 시간 : " + StockHelper.timeToKor(store.stock_at));
            holder.tv_update_time.setText("갱신 시간 : " + StockHelper.timeToKor(store.created_at));
            holder.tv_distance.setText(store.distance + "m");

            holder.tv_stock.setTextColor(StockHelper.stockToColor(store.remain_stat));

            // 자세히 보기
            holder.btn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
                }
            });

            // 지도에서 보기
            holder.btn_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(store.lat, store.lng))
                            .animate(CameraAnimation.Fly);

                    Application.bnv_main.setSelectedItemId(R.id.map);

                    Application.fragmentManager.beginTransaction().show(Application.mapFragment).commit();
                    Application.fragmentManager.beginTransaction().hide(Application.listFragment).commit();

                    Application.naverMap.moveCamera(cameraUpdate);
                }
            });
        }
    }
}
