package ua.rostopira.virtualpointer;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ua.rostopira.virtualpointer.sensorFusion.ImprovedOrientationSensor2Provider;
import ua.rostopira.virtualpointer.sensorFusion.OrientationProvider;
import ua.rostopira.virtualpointer.sensorFusion.Quaternion;

public class MainActivity extends AppCompatActivity {

    OrientationProvider currentOrientationProvider;
    static double[] center = { 0, 0 };
    static double[] angles = { 0, 0 };
    UDPSender UDPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UDPS = new UDPSender();
        currentOrientationProvider = new ImprovedOrientationSensor2Provider((SensorManager)getSystemService(SENSOR_SERVICE));

        final ImageView mFrame = (ImageView) findViewById(R.id.tap_button);
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams mParams;
                mParams = mFrame.getLayoutParams();
                mParams.height = mFrame.getWidth();
                mFrame.setLayoutParams(mParams);
                mFrame.postInvalidate();
            }
        });
        mFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mFrame.setImageResource(R.drawable.pressed_btn);
                        UDPS.Send("P"+Angles());
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mFrame.setImageResource(R.drawable.tap_btn);
                        UDPS.Send("R"+Angles());
                        return true;
                }
                return false;
            }
        });
    }

    MenuItem btnService;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main, menu );
        btnService = menu.findItem(R.id.action_service);

        return true;
    }

    boolean isOn = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_service) {
            //if (serviceRunning(VirtualPointerService.class)) {
            if (!isOn) { //TODO
                currentOrientationProvider.start();
                btnService.setIcon(R.drawable.ic_pause_white_36dp);
            } else {
                currentOrientationProvider.stop();
                btnService.setIcon(R.drawable.ic_play_arrow_white_36dp);
            }
            isOn = !isOn;
        }

        if (id == R.id.action_setip) {
            setIP();
        }

        return super.onOptionsItemSelected(item);
    }

    public void rightClick(View view) { UDPS.Send("RC"+ Angles()); }

    public void center(View view) {
        center = angles;
    }

    static public void onPositionChanged(Quaternion q) {
        angles = q.toEulerAngles();
        angles[0] += center[0];
        angles[1] += center[1];
        new UDPSender().Send("M"+Angles() + " " + Long.toString(SystemClock.uptimeMillis()));
    }

    static String Angles() {
        return " " + Double.toString(angles[0]) + " " + Double.toString(angles[1]);
    }

    public void setIP() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Set server IP");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UDPSender.IP = input.getText().toString();
                    }
                });
        alertDialog.show();
    }

}
