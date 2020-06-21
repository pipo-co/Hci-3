package com.example.hci_3.repositories;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.hci_3.api.ApiClient;
import com.example.hci_3.api.Device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeviceRepository {

    private static DeviceRepository instance;
    private ApiClient apiClient;
    private MutableLiveData<List<MutableLiveData<Device>>> devices;
    private Map<String, MutableLiveData<Device>> idToDeviceMap;
    //private Handler handler;

    private DeviceRepository(){
        apiClient = ApiClient.getInstance();
        devices = new MutableLiveData<>();
        idToDeviceMap = new HashMap<>();

        // Para mi tiene que ser un alarmManager
//        handler = new Handler();
//        int delay = 60000;
//
//        handler.postDelayed(new Runnable(){
//            @Override
//            public void run(){
//                handler.postDelayed(this, delay);
//                updateDevices();
//            }
//        }, delay);
    }

    public static synchronized DeviceRepository getInstance() {
        if (instance == null) {
            instance = new DeviceRepository();
        }
        return instance;
    }

    public MutableLiveData<List<MutableLiveData<Device>>> getDevices(){
        return devices;
    }

    public void executeAction(String deviceId, String actionName, List<Object> params, ApiClient.SuccessHandler<Boolean> responseHandler, ApiClient.ErrorHandler errorHandler){
        apiClient.executeAction(deviceId, actionName, params, success -> {

            if(success) {
                MutableLiveData<Device> ldDevice = idToDeviceMap.get(deviceId);
                assert ldDevice != null;
                Device device = ldDevice.getValue();
                assert device != null;
                apiClient.getDeviceState(deviceId, device.getState().getClass(), state -> {
                    device.setState(state);
                    ldDevice.postValue(device);
                    responseHandler.handle(true);
                }, (m, c) -> Log.w("uncriticalError", "Failed to get device state: " + m + " Code: " + c));

            } else
                responseHandler.handle(false);

        }, errorHandler);
    }

    public void executeAction(String deviceId, String actionName, List<Object> params, ApiClient.ErrorHandler errorHandler){
        executeAction(deviceId, actionName, params, bool -> {}, errorHandler);
    }

    public void updateDevices(){
        Log.v("pruebaBroadcast", "initUpdate");
        apiClient.getDevices(
                devices -> new Thread(() -> updateDeviceList(devices)).start(),
                (m, c) -> Log.w("uncriticalError", "Failed to get devices: " + m + " Code: " + c)
        );
    }

    private void updateDeviceList(List<Device> devs){
        Log.v("pruebaBroadcast", "initDeviceUpdate");
        Map<String, MutableLiveData<Device>> auxMap = new HashMap<>();

        List<MutableLiveData<Device>> ans = devs.stream().map(dev -> {
            MutableLiveData<Device> liveData = null;

            if(idToDeviceMap.containsKey(dev.getId()))
                liveData = idToDeviceMap.get(dev.getId());

            if(liveData == null)
                liveData = new MutableLiveData<>();

            auxMap.put(dev.getId(), liveData);
            liveData.postValue(dev);

            return liveData;
        }).collect(Collectors.toList());

        idToDeviceMap = auxMap;

        devices.postValue(ans);

        Log.v("pruebaBroadcast", "updatedDevices!");

        if(Looper.myLooper() == Looper.getMainLooper())
            Log.v("pruebaBroadcast", "Main thread!!!");
    }

//    public enum DeviceDataState {
//        UNCHANGED, UPDATED, REMOVED, ADDED
//    }
}