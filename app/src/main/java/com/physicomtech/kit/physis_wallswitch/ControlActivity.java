package com.physicomtech.kit.physis_wallswitch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.physicomtech.kit.physis_wallswitch.customize.OnSingleClickListener;
import com.physicomtech.kit.physis_wallswitch.customize.SerialNumberView;
import com.physicomtech.kit.physis_wallswitch.dialog.LoadingDialog;
import com.physicomtech.kit.physis_wallswitch.hepler.PHYSIsPreferences;
import com.physicomtech.kit.physis_wallswitch.wifi.WIFIManager;
import com.physicomtech.kit.physislibrary.PHYSIsMQTTActivity;

public class ControlActivity extends PHYSIsMQTTActivity {

    private static final String TAG = "SwitchActivity";

    SerialNumberView snvSetup;
    Button btnConnect, btnWiFiSetup, btnSwitchOn, btnSwitchOff;

    private static final String PUB_TOPIC = "Wall";
    private static final String WALL_SWITCH_ON = "1";
    private static final String WALL_SWITCH_OFF = "2";

    private PHYSIsPreferences preferences;
    private String serialNumber = null;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        init();
    }

    @Override
    protected void onMQTTConnectedStatus(boolean result) {
        super.onMQTTConnectedStatus(result);
        setConnectedResult(result);
    }

    @Override
    protected void onMQTTDisconnected() {
        super.onMQTTDisconnected();
        setConnectedResult(false);
    }

    /*
                Event
         */
    final SerialNumberView.OnSetSerialNumberListener onSetSerialNumberListener = new SerialNumberView.OnSetSerialNumberListener() {
        @Override
        public void onSetSerialNumber(String serialNum) {
            preferences.setPhysisSerialNumber(serialNumber = serialNum);
            Log.e(TAG, "# Set Serial Number : " + serialNumber);
        }
    };

    final OnSingleClickListener onClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()){
                case R.id.btn_wifi_setup:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!WIFIManager.getLocationProviderStatus(getApplicationContext())){
                        Toast.makeText(getApplicationContext(), "위치 상태를 활성화하고 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Start Setup Activity
                    startActivity(new Intent(ControlActivity.this, SetupActivity.class)
                            .putExtra("SERIALNUMBER", serialNumber));
                    break;
                case R.id.btn_connect:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(isConnected){
                        disconnectMQTT();
                    }else{
                        LoadingDialog.show(ControlActivity.this, "Connecting..");
                        connectMQTT();
                    }
                    break;
                case R.id.btn_switch_on:
                    if(isConnected)
                        publish(serialNumber, PUB_TOPIC, WALL_SWITCH_ON);
                    break;
                case R.id.btn_switch_off:
                    if(isConnected)
                        publish(serialNumber, PUB_TOPIC, WALL_SWITCH_OFF);
                    break;
            }
        }
    };

    /*
            Helper Methods
     */
    @SuppressLint("SetTextI18n")
    private void setConnectedResult(boolean state){
        LoadingDialog.dismiss();

        isConnected = state;
        if(isConnected){
            btnConnect.setText("Disconnect");
        }else{
            btnConnect.setText("Connect");
        }

        String toastMsg;
        if(isConnected) {
            toastMsg = "PHYSIs MQTT Broker와 연결되었습니다.";
        } else {
            toastMsg = "PHYSIs MQTT Broker와 연결이 실패/종료되었습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
    }

    private void init() {
        preferences = new PHYSIsPreferences(getApplicationContext());
        serialNumber = preferences.getPhysisSerialNumber();

        snvSetup = findViewById(R.id.snv_setup);
        snvSetup.setSerialNumber(serialNumber);
        snvSetup.showEditView(serialNumber == null);
        snvSetup.setOnSetSerialNumberListener(onSetSerialNumberListener);

        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(onClickListener);
        btnWiFiSetup = findViewById(R.id.btn_wifi_setup);
        btnWiFiSetup.setOnClickListener(onClickListener);
        btnSwitchOn = findViewById(R.id.btn_switch_on);
        btnSwitchOn.setOnClickListener(onClickListener);
        btnSwitchOff = findViewById(R.id.btn_switch_off);
        btnSwitchOff.setOnClickListener(onClickListener);
    }
}
