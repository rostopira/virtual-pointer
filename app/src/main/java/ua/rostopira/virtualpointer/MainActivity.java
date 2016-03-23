package ua.rostopira.virtualpointer;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private SensorFusion sensorFusion;
    private boolean timerRunning = false;
    private CountDownTimer longPressTimer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create round button
        final ImageView btn = (ImageView) findViewById(R.id.tap_button);
        btn.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams mParams;
                mParams = btn.getLayoutParams();
                mParams.height = btn.getWidth();
                btn.setLayoutParams(mParams);
                btn.postInvalidate();
            }
        });

        longPressTimer = new CountDownTimer(500, 500) {
            @Override public void onTick(long millisUntilFinished) {}
            @Override public void onFinish() {
                btn.setImageResource(R.drawable.tap_btn);
                btn.postInvalidate();
                new UDPSender().execute("L");
                timerRunning = false;
            }
        };
        //Fill with white on touch and start/stop timer
        /*
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        btn.setImageResource(R.drawable.pressed_btn);
                        longPressTimer.start();
                        timerRunning = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        if (timerRunning) {
                            longPressTimer.cancel();
                            timerRunning = false;
                            btn.setImageResource(R.drawable.tap_btn);
                            new UDPSender().execute("T");
                        }
                        return true;
                }
                return false;
            }
        });
        */

        //On/off toggle
        Switch toggle = (Switch) findViewById(R.id.toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = (TextView) findViewById(R.id.status_text);
                if (isChecked) {
                    UDPBroadcast broadcast = new UDPBroadcast();
                    broadcast.execute((Void)null);
                    try {
                        if (broadcast.get(2, TimeUnit.SECONDS) == null) {
                            tv.setText("Failed");
                            buttonView.setChecked(false);
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
}