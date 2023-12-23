package com.example.project_cm.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Device;
import com.example.project_cm.R;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    ArrayList<Device> viewPagerIDeviceArrayList;

    public HomeAdapter(ArrayList<Device> viewPagerIDeviceArrayList) {
        this.viewPagerIDeviceArrayList = viewPagerIDeviceArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_page_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Device viewPagerItem = viewPagerIDeviceArrayList.get(position);

        holder.deviceID.setText(viewPagerItem.getDeviceID());


    }

    @Override
    public int getItemCount() {
        return viewPagerIDeviceArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView deviceID;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceID = itemView.findViewById(R.id.textViewDevice);
        }
    }

}