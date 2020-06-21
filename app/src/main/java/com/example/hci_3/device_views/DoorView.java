package com.example.hci_3.device_views;

import android.content.Context;
import android.graphics.Color;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;

import com.example.hci_3.R;
import com.example.hci_3.api.Device;
import com.example.hci_3.api.DeviceStates.DoorState;

public class DoorView extends DeviceView {

    private CardView cardView;
    private ConstraintLayout expandableLayout;
    private ImageButton extendBtn;
    private TextView mDevName, mState,  mLocation;
    private Button mOpen, mBlock;

    public DoorView(Context context) {
        super(context);
    }

    public DoorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DoorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        LayoutInflater.from(context).inflate(R.layout.door_view, this, true);

        cardView = findViewById(R.id.cardView);
        expandableLayout = findViewById(R.id.expandableLayout);
        extendBtn = findViewById(R.id.expandBtn);
        mDevName = findViewById(R.id.door_name);
        mLocation = findViewById(R.id.door_location);
        mState = findViewById(R.id.onStateDoor);
        mOpen = findViewById(R.id.door_open);
        mBlock = findViewById(R.id.door_block);

    }

    @Override
    public void setDevice(LiveData<Device> device) {
        super.setDevice(device);



        extendBtn.setOnClickListener(v -> {
            if (expandableLayout.getVisibility() == View.GONE){
                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                expandableLayout.setVisibility(View.VISIBLE);
                // Falta rotar la flecha
            } else {
                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                expandableLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDeviceRefresh(Device device) {
        DoorState state = (DoorState) device.getState();

        mDevName.setText(getParsedName(device.getName()));


        mState.setText(getResources().getString(R.string.door_state,
                state.getStatus().equals("opened")? getResources().getString(R.string.abierta) : getResources().getString(R.string.cerrada), state.getLock().equals("locked")? getResources().getString(R.string.bloqueada) : getResources().getString(R.string.desbloqueada)));


        mLocation.setText(getResources().getString(R.string.disp_location,
                getParsedName(device.getRoom().getName()),
                device.getRoom().getHome().getName()));

        if(state.getStatus().equals("opened")) {
            mBlock.setClickable(false);
            mBlock.setBackgroundColor(Color.parseColor("#71A69A"));
        }
        else {
            mBlock.setClickable(true);
            mBlock.setBackgroundColor(Color.parseColor("#72E1C7"));
        }
        if(state.getLock().equals("locked")) {
            mOpen.setClickable(false);
            mOpen.setBackgroundColor(Color.parseColor("#71A69A"));
        }
        else {
            mOpen.setClickable(true);
            mOpen.setBackgroundColor(Color.parseColor("#72E1C7"));
        }

        mOpen.setOnClickListener(v -> {
            DoorState aux = ((DoorState)device.getState());
            if(aux.getLock().equals("unlocked")) {
                if (aux.getStatus().equals("closed")) {
                    open();
                } else
                    close();
            }
            else
                Toast.makeText(context, getResources().getString(R.string.open_error), Toast.LENGTH_LONG).show();
        });

        mOpen.setText(getResources().getString(R.string.state,
                state.getStatus().equals("opened")? getResources().getString(R.string.cerrar) : getResources().getString(R.string.abrir)));

        mBlock.setOnClickListener(v -> {
            DoorState aux = ((DoorState)device.getState());
            if(aux.getStatus().equals("closed")){
                if(aux.getLock().equals("unlocked")) {
                    lock();
                }
                else
                    unlock();
            }
            else
                Toast.makeText(context, getResources().getString(R.string.block_error), Toast.LENGTH_LONG).show();
        });

        mBlock.setText(getResources().getString(R.string.state,
                state.getLock().equals("lock")? getResources().getString(R.string.desbloquer) : getResources().getString(R.string.bloquear)));
    }
    private void open(){
        Log.v("door1", "estoy aca");
        executeAction("open", this::handleError);
    }
    private void close(){
        Log.v("door2", "estoy aca");
        executeAction("close", this::handleError);
    }

    private void lock(){
        executeAction("lock", this::handleError);
    }
    private void unlock(){
        executeAction("unlock", this::handleError);
    }


}