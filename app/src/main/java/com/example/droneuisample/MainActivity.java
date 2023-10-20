package com.example.droneuisample;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.TelemetryData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.CommandInt;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.GpsRawInt;
import io.dronefleet.mavlink.common.LocalPositionNed;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MavDoRepositionFlags;
import io.dronefleet.mavlink.common.MavFrame;
import io.dronefleet.mavlink.common.MissionCount;
import io.dronefleet.mavlink.common.MissionItemInt;
import io.dronefleet.mavlink.common.NavControllerOutput;
import io.dronefleet.mavlink.common.SetPositionTargetGlobalInt;
import io.dronefleet.mavlink.common.SetPositionTargetLocalNed;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.protocol.MavlinkPacket;


@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener  {//GoogleMap.OnMarkerDragListener
    String [] item = {"Guided","Loiter","RTL","Land","Auto","AltHold"};
    Button btn;
    static MainActivity INSTANCE;
    int distanceToWayPoint;
    TextView tvLat,tvLon,tvAlt,tvDwp,tvBatteryVtg,tvBatteryCn,tvGroundSpeed,tvVerticalSpeed,tvYaw,tvDistanceToHome;
    LinearLayout btnArmDisarm,btnTakeOff,btnMission,overlay;
    ImageButton btnZoomIn,btnZoomOut,btnConnection;
    MaterialCardView swapContainer;
    Spinner spinner;
    ImageView blinker,btnUp2,btnDown2,btnLeft2,btnRight2,btnSpinner,btnUp;
    CommandLong commandLong;
    String serverIp;
    int serverPort;
    Socket socket ;
    InputStream inputStream;
    OutputStream outputStream;
    MavlinkMessage mavlinkMessage;
    MavlinkConnection mavlinkConnection;
    MavlinkPacket mavlinkPacket;
    boolean isArm,isTakeOff,flag;
    private GoogleMap googleMap;
    double doubleLatitude,doubleLongitude,doubleAltitude,voltage,current;
    int intLatitude,intLongitude, intAltitude,lat,lon;
    float alt;
    List<Marker> markerList = new ArrayList<>();
    List<MissionItemInt> wayPointList = new ArrayList<>();
    List<Polyline> polylineList = new ArrayList<>();
    LatLng homeLocation;
    MissionItemInt missionItemInt100;
    MissionItemInt missionItemInt;
    boolean isConnect = false;
    boolean isMainThread;
    SupportMapFragment mapFragment;
    Button b;
    int position;
    float altError = 0.00f;
    FragmentContainerView view;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    CommandInt commandInt;
    int lats,lons;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        INSTANCE = this;
        btnArmDisarm = findViewById(R.id.btnArmDisarm);
        btnTakeOff = findViewById(R.id.btnTakeOff);
        blinker = findViewById(R.id.blinker);
        btnMission = findViewById(R.id.btnMission);
        spinner = findViewById(R.id.spinner);
        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        tvAlt = findViewById(R.id.tvAlt);
        tvDwp = findViewById(R.id.tvDwp);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        swapContainer = findViewById(R.id.swapContainer);
        overlay = findViewById(R.id.overlay);
        btnConnection = findViewById(R.id.btnConnection);
        btnUp2 = findViewById(R.id.btnUp2);
        btnDown2 = findViewById(R.id.btnDown2);
        btnLeft2 = findViewById(R.id.btnLeft2);
        btnRight2 = findViewById(R.id.btnRight2);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        btnSpinner = findViewById(R.id.btnSpinner);
        tvBatteryVtg = findViewById(R.id.tvBatteryVtg);
        tvBatteryCn = findViewById(R.id.tvBatteryCn);
        tvGroundSpeed = findViewById(R.id.tvGroundSpeed);
        tvVerticalSpeed = findViewById(R.id.tvVerticalSpeed);
        tvYaw = findViewById(R.id.tvYaw);
        tvDistanceToHome = findViewById(R.id.tvDistanceToHome);
        view = findViewById(R.id.map);
        btnUp = findViewById(R.id.btnUp);


        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item,item);
        stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(stringArrayAdapter);
//        spinner.setOnItemSelectedListener(this);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapterView.getItemAtPosition(i).toString();
                position = i;
                Toast.makeText(MainActivity.this,value,Toast.LENGTH_SHORT).show();
//                Log.d("Posonselect", String.valueOf(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                Log.d("not selected","not selected");
            }
        });

        btnSpinner.setOnClickListener(view -> {
            if(isMainThread){
//                Log.d("PosOnClick", String.valueOf(position));
                new Thread(new ModesChange(position)).start();
            }
        });


        //Connection Button
        btnConnection.setTag(0);
        btnConnection.setOnClickListener(view -> {
            final int status = (Integer)view.getTag();
            switch(status){
                case 0:
                    btnConnection.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                    blinker.setBackgroundColor(Color.GREEN);
                    manageBlinkEffect(blinker);
                    view.setTag(1);
                    break;
                case 1:
                    btnConnection.setImageTintList(ColorStateList.valueOf(Color.RED));
                    blinker.clearAnimation();
                    blinker.setBackgroundColor(Color.RED);
                    view.setTag(0);
                    break;
            }
            new Thread(new Connection()).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mapFragment.getMapAsync(this);
        });

        //Arm/Disarm Button
        btnArmDisarm.setOnClickListener(view ->  {
            new Thread(new ArmDisarm()).start();
        });

        //TakeOff Button
        btnTakeOff.setOnClickListener(view ->  {
            new Thread(new TakeOff()).start();
        });

        //Send Mission Button
        btnMission.setOnClickListener(view ->  {
            new Thread(new Mission()).start();
        });
        btnZoomIn.setOnClickListener(view -> {
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        });
        btnZoomOut.setOnClickListener(view -> {
            googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        });
        swapContainer.setOnClickListener(view -> {
           /* overlay.setVisibility(View.INVISIBLE);
           getSupportFragmentManager().beginTransaction().replace(R.id.map, new video_fragment()).commit();*/
            Intent intent = new Intent(this, VideoActivity.class);
            startActivity(intent);
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Reposition()).start();
            }
        });



        btnUp2.setOnTouchListener((view, motionEvent) -> {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                btnUp2.setAlpha(0.5f);
                flag = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                flag = false;
                btnUp2.setAlpha(1.0f);
            }
            new Thread(new Forward()).start();
            return true;
        });


        btnDown2.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                flag = true;
                btnDown2.setAlpha(0.5f);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                flag = false;
                btnDown2.setAlpha(1.0f);
            }
            new Thread(new Reverse()).start();
            return true;
        });

        btnLeft2.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                flag = true;
                btnLeft2.setAlpha(0.5f);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                flag = false;
                btnLeft2.setAlpha(1.0f);
            }
            new Thread(new Left()).start();
            return true;
        });

        btnRight2.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                flag = true;
                btnRight2.setAlpha(0.5f);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                flag = false;
                btnRight2.setAlpha(1.0f);
            }
            new Thread(new Right()).start();
            return true;
        });


/*        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, btn);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.on_click_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Toast message on menu item clicked
                        Toast.makeText(MainActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });*/

    }

    public void manageBlinkEffect(ImageView imageView){
        Animation animation =  new AlphaAnimation(0.0f,1.0f);
        animation.setDuration(100);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        imageView.startAnimation(animation);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(this);
//        googleMap.setOnMarkerDragListener(this);

        homeLocation = new LatLng(doubleLatitude, doubleLongitude);
//        Log.d("mapLat",homeLocation.toString());

        markerList.add(markerList.size(),googleMap.addMarker(new MarkerOptions().position(homeLocation)));

        missionItemInt = MissionItemInt.builder().frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT).command(MavCmd.MAV_CMD_NAV_WAYPOINT).x(intLatitude).y(intLongitude).z(50).seq(wayPointList.size()).autocontinue(0).build();
        wayPointList.add(wayPointList.size(),missionItemInt);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(homeLocation));

        googleMap.animateCamera( CameraUpdateFactory.zoomTo(20));

        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));
        lats = (int) (latLng.latitude*1E7);
        lons = (int) (latLng.longitude*1E7);
//        Log.d("lttlonn",lt+" "+ln);
    }



    /*This is a on map click create a marker*/
/*    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        *//*ADD MARKER*//*
        markerList.add(markerList.size(),googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true)));


        *//*DRAW POLYLINE ALL IN ONE*//*
        for (Polyline polyline:
                polylineList) {
            polyline.remove();
        }
        polylineList.clear();
        for(int i=1;i<markerList.size();i++){
            if(i==markerList.size()-1){
                polylineList.add(polylineList.size(),googleMap.addPolyline(new PolylineOptions().add(markerList.get(i).getPosition(), markerList.get(0).getPosition()).width(5).color(Color.YELLOW).geodesic(true)));
            }
            polylineList.add(polylineList.size(),googleMap.addPolyline(new PolylineOptions().add(markerList.get(i-1).getPosition(), markerList.get(i).getPosition()).width(5).color(Color.YELLOW).geodesic(true)));
        }


        *//*SAVE MISSION WAYPOINTS*//*
        lat= (int)(markerList.get(markerList.size()-1).getPosition().latitude*1E7);
        lon = (int)(markerList.get(markerList.size()-1).getPosition().longitude*1E7);
        wayPointList.add(wayPointList.size(),MissionItemInt.builder().frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT).command(MavCmd.MAV_CMD_NAV_WAYPOINT).x(lat).y(lon).z(50).seq(wayPointList.size()).current(1).build());

    }*/








/*This is a drag drop marker*/
/*
  @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        *//*REMOVE ALL POLYLINES*//*
        for (Polyline polyline:
                polylineList) {
            polyline.remove();
        }
        polylineList.clear();

        *//*DRAW ALL POLYLINES*//*
        for(int i=1;i<markerList.size();i++){
            if(i==markerList.size()-1){
                polylineList.add(polylineList.size(),googleMap.addPolyline(new PolylineOptions().add(markerList.get(i).getPosition(), markerList.get(0).getPosition()).width(5).color(Color.YELLOW).geodesic(true)));
            }
            polylineList.add(polylineList.size(),googleMap.addPolyline(new PolylineOptions().add(markerList.get(i-1).getPosition(), markerList.get(i).getPosition()).width(5).color(Color.YELLOW).geodesic(true)));
        }
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }*/











/*    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch(i){
            case 0:
                Toast.makeText(this,"0",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this,"1",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this,"2",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this,"3",Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(this,"4",Toast.LENGTH_SHORT).show();
                break;
            case 5:
                Toast.makeText(this,"5",Toast.LENGTH_SHORT).show();
                break;
            case 6:
                Toast.makeText(this,"6",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }*/

    //CONNECTION CLASS
    class Connection implements Runnable {
        @Override
        public void run() {
            serverIp = "192.168.100.124";
            serverPort = 14550;
            try {
                if(isConnect){
                    socket = null;
                    isMainThread = false;
                    inputStream.close();
                    outputStream.close();
                }else{
                    socket = new Socket(serverIp, serverPort);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    isMainThread = true;
                }
                isConnect = !isConnect;


                //create a mavlink connection and connect to InputStream and OutputStream
                mavlinkConnection = MavlinkConnection.create(inputStream, outputStream);
                isArm = true;

                //read InputStream
                while ((mavlinkMessage = mavlinkConnection.next()) != null) {

//                    clone mavlink packet into rawBytes
                    byte[] arr = mavlinkMessage.getRawBytes();

//                    create a MavlinkPacket
                    mavlinkPacket = MavlinkPacket.fromV2Bytes(arr);
//                   Object object =  mavlinkMessage.getPayload();
//                   Log.d("object",object.toString());

                    if(mavlinkPacket.getMessageId()==75 || mavlinkPacket.getMessageId()==76 || mavlinkPacket.getMessageId()==77 || mavlinkPacket.getMessageId()==47 || mavlinkPacket.getMessageId()==73 || mavlinkPacket.getMessageId()==32 || mavlinkPacket.getMessageId()==64 || mavlinkPacket.getMessageId()==89|| mavlinkPacket.getMessageId()==86 || mavlinkPacket.getMessageId()==84 || mavlinkPacket.getMessageId()==48||mavlinkPacket.getMessageId()==33||mavlinkPacket.getMessageId()==63){
                        Object obj = mavlinkMessage.getPayload();
                        Log.d("obj",obj.toString());
                    }




//                display received messages
                if (mavlinkPacket.getMessageId() == 24) {
                    GpsRawInt latLon = (GpsRawInt) mavlinkMessage.getPayload();
                    intLatitude = latLon.lat();
                    doubleLatitude = intLatitude * 1E-7;
                    intLongitude = latLon.lon();
                    doubleLongitude = intLongitude * 1E-7;

                    runOnUiThread(() -> {
                        tvLat.setText(String.format("%.5f",doubleLatitude)+" °");
                        tvLon.setText(String.format("%.5f",doubleLongitude)+" °");
                    });
                    }
                if(mavlinkPacket.getMessageId() == 1){
                    SysStatus sysStatus = (SysStatus) mavlinkMessage.getPayload();
                    voltage = sysStatus.voltageBattery() * 1E-3;
                    current = sysStatus.currentBattery() * 1E-2;
                    runOnUiThread(() -> {
                        tvBatteryVtg.setText("Voltage - "+String.format("%.2f",voltage)+" V");
                        tvBatteryCn.setText("Current - "+String.format("%.2f",current)+" A");
                    });
                }
                if(mavlinkPacket.getMessageId()==32){
                    LocalPositionNed localPositionNed = (LocalPositionNed) mavlinkMessage.getPayload();
                    alt =  localPositionNed.z();
//                    Log.d("add", String.valueOf(Math.round(alt)+Math.round(altError)));
                    float verticalSpeed = localPositionNed.vz()*-1;
                    float groundSpeed = localPositionNed.vx();
                    float distanceToHome = localPositionNed.x();
                    runOnUiThread(()->{
                        tvAlt.setText(String.format("%.2f",Math.abs(alt))+" m");
                        tvVerticalSpeed.setText(String.format("%.2f",verticalSpeed)+" m/s");
                        tvGroundSpeed.setText(String.format("%.2f",Math.abs(groundSpeed))+" m/s");
                        tvDistanceToHome.setText(String.format("%.2f",Math.abs(distanceToHome)));
                    });
                    }
                if(mavlinkPacket.getMessageId() == 33){
                    GlobalPositionInt globalPositionInt = (GlobalPositionInt) mavlinkMessage.getPayload();
                    double yaw = globalPositionInt.hdg() * 1E-2;
                    runOnUiThread(() -> {
                        tvYaw.setText(String.format("%.2f",yaw));
                    });
                }
                    if(mavlinkPacket.getMessageId() == 62){
                        NavControllerOutput navControllerOutput = (NavControllerOutput) mavlinkMessage.getPayload();
                        altError = navControllerOutput.altError();
                        distanceToWayPoint = navControllerOutput.wpDist();
                        String string =decimalFormat.format(distanceToWayPoint)+" m";
                        runOnUiThread(() -> {
                            tvDwp.setText(string);
                        });
                    }
                }
            }catch (Exception e) {
                isArm = false;
                e.printStackTrace();
            }
        }
    }

//        ARMDISARM CLASS
    class ArmDisarm implements Runnable {
        @Override
        public void run() {
            try {
                if(isArm) {
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM).confirmation(0).param1(1).param2(21196).build();
                    mavlinkConnection.send2(0, 0, commandLong);
                    btnArmDisarm.setAlpha(1.0f);
                    isArm = false;
                    isTakeOff = true;
                }else{
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM).confirmation(0).param1(0).param2(21196).build();
                    mavlinkConnection.send2(0, 0, commandLong);
                    btnArmDisarm.setAlpha(0.7f);
                    isArm = true;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }



//    TAKEOFF CLASS
    class TakeOff implements Runnable{
        @Override
        public void run() {
            try {
                if(isTakeOff) {
                    commandLong = CommandLong.builder().command(MavCmd.MAV_CMD_NAV_TAKEOFF).param7(20).build();
                    mavlinkConnection.send2(0, 0, commandLong);
                    btnTakeOff.setAlpha(1.0f);
                    isTakeOff = false;

                }else{
                    commandLong = CommandLong.builder().command(MavCmd.MAV_CMD_NAV_LAND).build();
                    mavlinkConnection.send2(0, 0, commandLong);
                    btnTakeOff.setAlpha(0.7f);
                    isTakeOff = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    MISSION CLASS
class Mission implements Runnable{
    @Override
    public void run() {
        MissionCount count = MissionCount.builder().count(wayPointList.size()).build();
        try {
            mavlinkConnection.send2(0, 0, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int a = 0; a <= wayPointList.size() - 1; a++) {
            missionItemInt100 = (MissionItemInt) wayPointList.get(a);
            try {
                mavlinkConnection.send2(0, 0, missionItemInt100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
    class ModesChange implements Runnable {
        int position;
        public ModesChange(int position){
            this.position = position;
        }
        @Override
        public void run() {
            switch(position) {
                case 0:
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(4).build();
                    break;
                case 1:
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(5).build();
                    break;
                case 2:
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(6).build();
                    break;
                case 3:
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(9).build();
                    break;
                case 4:
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(3).build();
                    break;
                case 5:
                    commandLong = CommandLong.builder().targetSystem(0).targetComponent(0).command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(2).build();
                    break;
            }
            try {
//                Log.d("cmd",commandLong.toString());

                    mavlinkConnection.send2(0, 0, commandLong);

//                    Log.d("cmdSent","Command Sent");
                } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Forward implements Runnable{
        CommandLong cmdLong;

        @Override
        public void run() {
//            tvStatus.setText("Forward");
            SetPositionTargetLocalNed cmd = SetPositionTargetLocalNed.builder().coordinateFrame(MavFrame.MAV_FRAME_LOCAL_OFFSET_NED).vx(1).build();

            try {
                while(flag) {
                    mavlinkConnection.send2(0, 0, cmd);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Reverse implements Runnable{

        @Override
        public void run() {
            SetPositionTargetLocalNed cmd = SetPositionTargetLocalNed.builder().coordinateFrame(MavFrame.MAV_FRAME_LOCAL_OFFSET_NED).vx(-0.1f).build();
            try {
                mavlinkConnection.send2(0, 0, cmd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Left implements Runnable{

        @Override
        public void run() {
            SetPositionTargetLocalNed cmd = SetPositionTargetLocalNed.builder().coordinateFrame(MavFrame.MAV_FRAME_LOCAL_OFFSET_NED).vy(-0.1f).build();
            try {
                mavlinkConnection.send2(0, 0, cmd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Right implements Runnable{

        @Override
        public void run() {
            SetPositionTargetLocalNed cmd = SetPositionTargetLocalNed.builder().coordinateFrame(MavFrame.MAV_FRAME_LOCAL_OFFSET_NED).vy(0.1f).build();
            try {
                mavlinkConnection.send2(0, 0, cmd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Reposition implements Runnable{
        CommandInt command = CommandInt.builder().command(MavCmd.MAV_CMD_NAV_GUIDED_ENABLE).param1(1).build();
        CommandInt command2 = CommandInt.builder().command(MavCmd.MAV_CMD_NAV_FOLLOW).param2(10).x(lats).y(lons).z(50).build();
        CommandInt command3 = CommandInt.builder().command(MavCmd.MAV_CMD_NAV_CONTINUE_AND_CHANGE_ALT).z(20).param1(2).build();
        CommandInt command4 = CommandInt.builder().command(MavCmd.MAV_CMD_DO_PAUSE_CONTINUE).param1(1).build();
        CommandInt command5 = CommandInt.builder().command(MavCmd.MAV_CMD_GUIDED_CHANGE_ALTITUDE).param1(1).build();
        CommandInt command6 = CommandInt.builder().command(MavCmd.MAV_CMD_CONDITION_DISTANCE).param1(distanceToWayPoint).build();

        SetPositionTargetGlobalInt setPositionTargetGlobalInt =SetPositionTargetGlobalInt.builder().coordinateFrame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT_INT).latInt(lats).lonInt(lons).alt(30).build();

        @Override
        public void run() {
            try {
//                mavlinkConnection.send2(0,0,command);
//                Thread.sleep(1000);
//                mavlinkConnection.send2(0,0,command3);
//                mavlinkConnection.send2(0,0,command6);
                mavlinkConnection.send2(0,0,command2);
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}