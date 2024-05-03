package com.example.forgroundservicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.forgroundservicetest.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private SocketService socketService;
    private boolean isServiceBound = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText editText = view.findViewById(R.id.edit_text_input);

                binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String enteredText = editText.getText().toString();
                        Log.d(">>>", "Entered text: " + enteredText);
                        Log.d(">>>", "isServiceBound: " + isServiceBound);
                        if (isServiceBound) {
                            socketService.startListeningTo(enteredText);
                        }

                    }
                });
    }

    @Override
    public void onStart() {
        Log.d(">>>","onStart");
        super.onStart();
        Intent intent = new Intent(getActivity(), SocketService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onStop() {
        Log.d(">>>","onStop");
        super.onStop();
        // Unbind from SocketService
        if (isServiceBound) {
            getActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(">>>","onDestroyView");
        super.onDestroyView();
        binding = null;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(">>>","onServiceConnected");
            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketService = binder.getService();
            isServiceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(">>>","onServiceDisconnected");
            isServiceBound = false;
        }
    };

}