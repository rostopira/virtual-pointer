package ua.rostopira.virtualpointer;

import android.content.Intent;
import android.hardware.SensorManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private SensorFusion sensorFusion;
    private GestureDetectorCompat mDetector;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        //On/off toggle
        Switch toggle = (Switch) findViewById(R.id.toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = (TextView) findViewById(R.id.status_text);
                if (isChecked) {
                    try {
                        UDPBroadcast broadcast = new UDPBroadcast();
                        broadcast.execute(getBroadcastAddress());
                        if (broadcast.get(2, TimeUnit.SECONDS) == null) {
                            tv.setText("Failed");
                            buttonView.setChecked(false);
                            buttonView.postInvalidate();
                        } else {
                            tv.setText("Server " + broadcast.get().getHostAddress());
                            sensorFusion.start();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Broadcast error");
                    }
                } else {
                    tv.setText("Service is OFF");
                    sensorFusion.stop();
                }
            }
        });

        sensorFusion = new SensorFusion((SensorManager)getSystemService(SENSOR_SERVICE));
        //toggle.toggle();
    }

    public InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBtnClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                new UDPSender().execute("K", String.valueOf(KeyEvent.KEYCODE_BACK));
                break;
            case R.id.home_btn:
                new UDPSender().execute("K", String.valueOf(KeyEvent.KEYCODE_HOME));
                break;
            case R.id.recent_apps_btn:
                new UDPSender().execute("K", String.valueOf(KeyEvent.KEYCODE_APP_SWITCH));
                break;
            case R.id.center_btn:
                new UDPSender().execute("C");
                break;
        }
    }

    /**
     * Use volume keys to change volume on server
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    new UDPSender().execute("K", String.valueOf(KeyEvent.KEYCODE_VOLUME_UP));
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    new UDPSender().execute("K", String.valueOf(KeyEvent.KEYCODE_VOLUME_DOWN));
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            new UDPSender().execute("T");
            return true;
        }
        @Override
        public void onLongPress(MotionEvent event) {
            new UDPSender().execute("L");
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            new UDPSender().execute("S",String.valueOf(distanceX),String.valueOf(distanceY));
            return true;
        }
    }
}