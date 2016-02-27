package ua.rostopira.virtualpointer;

import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    SensorFusion sensorFusion;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create round button
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
        //Button touch reaction
        mFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mFrame.setImageResource(R.drawable.pressed_btn);
                        S.get().send("T");
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mFrame.setImageResource(R.drawable.tap_btn);
                        S.get().send("R");
                        return true;
                }
                return false;
            }
        });

        //TODO: server autodetection
        sensorFusion = new SensorFusion((SensorManager)getSystemService(SENSOR_SERVICE));
        sensorFusion.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_setip) {
            setIP();
            sensorFusion.start(); //TODO: add normal START button. Big and red maybe
        }

        return super.onOptionsItemSelected(item);
    }

    public void rightClick(View view) {
        S.get().send("L");
    }

    public void center(View view) {
        S.get().send("C");
    }

    public void back(View view) {
        S.get().send("B");
    }

    public void home(View view) {
        S.get().send("H");
    }

    public void recent(View view) {
        S.get().send("A");
    }

    /** This method creates dialog in which user can specify the IP address of device with
     *  Virtual Pointer Server running.
     *  TODO: Save that value to shared preferences
     */
    public void setIP() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Set server IP");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.append("192.168.1.");
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        S.get().setIP(input.getText().toString());
                    }
                });
        alertDialog.show();
    }

}