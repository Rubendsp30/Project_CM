package com.example.project_cm.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Activities.LoginActivity;

import com.example.project_cm.Device;
import com.example.project_cm.R;
import com.example.project_cm.Adapters.HomeAdapter;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.UserViewModel;

import java.util.ArrayList;

public class HomeScreenFragment extends Fragment {


    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;

    ViewPager2 viewPagerHome;
    ArrayList<Device> viewPagerItemDeviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the login fragment layout
        View view = inflater.inflate(R.layout.home_screen, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        try {
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        } catch (Exception e) {
            Log.e("HomeScreenFragment", "Error creating UserViewModel: " + e.getMessage());
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarHomeScreen);
        setHasOptionsMenu(true);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitleTextColor(getResources().getColor(R.color.green_400));
        toolbar.setTitle("Pet Feeder");

        // Fetch devices for the current user
        String currentUserId = userViewModel.getCurrentUser().getValue().getUserID();
        DeviceViewModel deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        viewPagerHome = view.findViewById(R.id.viewPagerHome);
        viewPagerItemDeviceList = new ArrayList<>();
        HomeAdapter homeAdapter = new HomeAdapter(viewPagerItemDeviceList,  getChildFragmentManager(),deviceViewModel);
        viewPagerHome.setAdapter(homeAdapter);
        viewPagerHome.setOffscreenPageLimit(2);
        viewPagerHome.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        deviceViewModel.getDevicesForUser(currentUserId).observe(getViewLifecycleOwner(), devices -> {
            viewPagerItemDeviceList.clear();
            viewPagerItemDeviceList.addAll(devices);
            homeAdapter.notifyDataSetChanged();

        });



    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu for this fragment
        inflater.inflate(R.menu.menu_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item selections
        if (item.getItemId() == R.id.action_logout) {
            logoutUser();
        }
        /* else if (item.getItemId() == R.id.action_menu) {

        }*/
        return true;
    }

    public void logoutUser() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("loggedInUserId");
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
