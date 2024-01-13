    package com.example.project_cm.Fragments;

    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageButton;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.project_cm.Activities.HomeActivity;
    import com.example.project_cm.FragmentChangeListener;
    import com.example.project_cm.Fragments.DeviceSetup.DevSetupInitial;
    import com.example.project_cm.Fragments.HomeScreenFragment;
    import com.example.project_cm.MQTTHelper;

    import com.example.project_cm.User;
    import com.example.project_cm.ViewModels.UserViewModel;
    import com.example.project_cm.utils.ClientNameUtil;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;
    import com.google.firebase.firestore.QueryDocumentSnapshot;

    //import com.example.project_cm.Adapters.DeviceManagerAdapter;
    import com.example.project_cm.R;
    import com.example.project_cm.ViewModels.DeviceViewModel;
    import com.example.project_cm.ViewModels.UserViewModel;
    import com.example.project_cm.ViewModels.DeviceManagerViewModel;
    import com.example.project_cm.Adapters.DeviceManagerAdapter;
    import com.example.project_cm.ViewModels.PetProfileViewModel;
    import java.util.ArrayList;
    import com.example.project_cm.FragmentChangeListener;
    public class DeviceManagementFragment extends Fragment {
        private com.example.project_cm.FragmentChangeListener fragmentChangeListener;
        private DeviceManagerViewModel deviceManagerViewModel;
        private DeviceViewModel deviceViewModel;
        private UserViewModel userViewModel;
        private RecyclerView recyclerView;
        private ImageButton addDeviceButton;
        private DeviceManagerAdapter adapter;
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            this.fragmentChangeListener = (HomeActivity)inflater.getContext();
            return inflater.inflate(R.layout.device_management, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Toolbar toolbar = view.findViewById(R.id.toolbar);
            toolbar.setTitle(" ");

            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> {
                if (fragmentChangeListener != null) {
                    fragmentChangeListener.replaceFragment(new MenuFragment());
                }
            });

            PetProfileViewModel petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
            deviceManagerViewModel = new ViewModelProvider(requireActivity()).get(DeviceManagerViewModel.class);

            recyclerView = view.findViewById(R.id.DeviceViewHolder);
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());

            adapter = new DeviceManagerAdapter(new ArrayList<>(), petProfileViewModel, layoutInflater);
            recyclerView.setAdapter(adapter);

            addDeviceButton = view.findViewById(R.id.AddDevice);
            addDeviceButton.setOnClickListener(v -> addNewDevice());

            String userId = getUserIdFromUserViewModel();
            Log.d("DeviceManagement", "UserID: " + userId);
            deviceManagerViewModel.checkUserHasDevice(userId);

            deviceManagerViewModel.getDevicesLiveData().observe(getViewLifecycleOwner(), devices -> {
                Log.d("DeviceManagement", "Dispositivos carregados: " + devices.size());
                adapter.setDevices(devices);
                adapter.notifyDataSetChanged();
            });

            addDeviceButton.setOnClickListener(v -> {
                if (getActivity() instanceof FragmentChangeListener) {
                    ((FragmentChangeListener) getActivity()).replaceFragment(new DevSetupInitial());
                }
            });
        }
        private void addNewDevice() {
            if (getActivity() instanceof FragmentChangeListener) {
                ((FragmentChangeListener) getActivity()).replaceFragment(new DevSetupInitial());
            }
        }

        private String getUserIdFromUserViewModel() {
            return userViewModel.getCurrentUser().getValue().getUserID();
        }
    }



