package ua.rostopira.virtualpointer;

import android.hardware.SensorManager;
import android.os.Bundle;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SensorFusion sensorFusion;

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
        //Fill with white on touch
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        btn.setImageResource(R.drawable.pressed_btn);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        btn.setImageResource(R.drawable.tap_btn);
                        return true;
                }
                return false;
            }
        });
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                S.get().send("L");
                return true;
            }
        });

        Switch toggle = (Switch) findViewById(R.id.toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = (TextView) findViewById(R.id.status_text);
                if (isChecked) {
                    //Find server
                    UDPScanner scanner = new UDPScanner();
                    scanner.execute((Void) null);
                    UDPBroadcast broadcast = new UDPBroadcast();
                    broadcast.execute((Void) null);
                    tv.setText("Searching server...");
                    try {
                        S.get().IP = scanner.get();
                        broadcast.cancel(false);
                        if (S.get().IP==null)
                            throw new NullPointerException();
                        else {
                            tv.setText("Server " + S.get().IP.getHostAddress());
                            sensorFusion.start();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Failed to find a server");
                        Toast.makeText(getBaseContext(), "Failed to find server", Toast.LENGTH_LONG).show();
                        buttonView.setChecked(false);
                    }
                } else {
                    tv.setText("Service is OFF");
                    sensorFusion.stop();
                }
            }
        });

        sensorFusion = new SensorFusion((SensorManager)getSystemService(SENSOR_SERVICE));
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBtnClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                S.get().sendKey(KeyEvent.KEYCODE_BACK);
                return;
            case R.id.home_btn:
                S.get().sendKey(KeyEvent.KEYCODE_HOME);
                return;
            case R.id.recent_apps_btn:
                S.get().sendKey(KeyEvent.KEYCODE_APP_SWITCH);
                return;
            case R.id.center_btn:
                S.get().send("C");
                return;
            case R.id.tap_button:
                S.get().send("T");
                return;
        }
    }

}