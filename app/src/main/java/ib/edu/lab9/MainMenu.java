package ib.edu.lab9;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

//Main activity
public class MainMenu extends Activity {
    //After opening the app you can choose between 2 sensors so as the attributes I've chosen these 2 new intents,each for every sensor
    private Intent accelerometer;
    private Intent gyroscope;
//After popping up the main activity the user gets to chose desired sensor(as long as it's gyroscope or accelerometer) and after clicking the right button a proper window will show up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelerometer = new Intent(getBaseContext(), Accelerometer.class);
        gyroscope=new Intent(getBaseContext(), Gyroscope.class);
    }

    public void accelerometerChosen(View view) {startActivity(accelerometer);}

    public void gyroscopeChosen(View view) {
        startActivity(gyroscope);
    }
}
