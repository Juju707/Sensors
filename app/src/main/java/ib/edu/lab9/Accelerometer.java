package ib.edu.lab9;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.achartengine.model.XYSeries;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class Accelerometer extends Activity implements SensorEventListener {

    private SensorManager manager;
    private boolean isRunning=false;
    private EditText fileName;
    private EditText editTextAx;
    private EditText editTextAy;
    private EditText editTextAz;
    private Sensor accelerometer;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private Intent plot;
    private XYSeries X;
    private XYSeries Y;
    private XYSeries Z;
    private StringBuilder data=new StringBuilder();

    private int counter=0;
    private ArrayList<Double> values=new ArrayList<>();
    private double [] forward;



    @Override
    //Method for initialization upon creation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accelerometer);
        //initializing data series
        X=new XYSeries("X");
        Y=new XYSeries("Y");
        Z=new XYSeries("Z");
        //Initializing plot intent,text views and accelerometer sensor
        plot = new Intent(getBaseContext(), Plot.class);
        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer=manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        editTextAx=(EditText)findViewById(R.id.xAxis);
        editTextAy=(EditText)findViewById(R.id.yAxis);
        editTextAz=(EditText)findViewById(R.id.zAxis);
        fileName=(EditText)findViewById(R.id.editText);
        powerManager=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myapp:test");

    }
    //Start button for staring measuring acceleration and stopping it
    public void onClickAction(View view) {
        isRunning=!isRunning;
        if(isRunning)wakeLock.acquire();
        else wakeLock.release();
    }
//Method for registering the changes
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isRunning){
            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                //Setting values in fields to proper values
                float aX=event.values[0]; //Składowa x wektora przyspieszenia
                float aY=event.values[1]; //Składowa y wektora przyspieszenia
                float aZ=event.values[2]; //Składowa z wektora przyspieszenia
                float timeStamp = event.timestamp;
                //Counter for number of measurement for plots
                counter++;

                editTextAx.setText(Float.toString(aX));
                editTextAy.setText(Float.toString(aY));
                editTextAz.setText(Float.toString(aZ));

                //Adding data to series
                X.add(counter, aX);
                Y.add(counter, aY);
                Z.add(counter, aZ);
                values.add((double) aZ);
                //Adding a string of data in case of reading from file
                String toData=counter+";"+aX+";"+aY+";"+aZ+";"+timeStamp+"%";
                data.append(toData);

            }
        }
    }
    //In this app we don't change accuracy
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //Method for saving all given data in form of string in the file with given name
    public void saveRowOfData(String name, String data){
        try {
            FileOutputStream fos = openFileOutput(name, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            osw.write(data);
            osw.flush();
            osw.close();

            Toast.makeText(this,"Success!",Toast.LENGTH_LONG).show();

        } catch (java.io.IOException e) {
            Toast.makeText(this, "Failed to save,please try again",Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

    }

    public void Plot(View view) {
        //Stop measuring
        isRunning=false;

        //For some reason an array list can't be pushed forward so I have to save it in form of table
        forward=new double[values.size()];
        for (int i=0; i<forward.length; i++){
            forward[i]=values.get(i);
        }
        //We have to push all the data to new intent and then start it
        plot.putExtra("Xdata", X);
        plot.putExtra("Ydata", Y);
        plot.putExtra("Zdata", Z);
        plot.putExtra("Number of steps", forward);
        startActivity(plot);
    }

    public void saveData(View view) {
        //Stop measuring
        isRunning=false;
        String name;
        //Check for file name and set one by default if it was not given
        if(fileName.getText().toString().equals("")||fileName.getText().toString()==null){
            name="Walk";
        }else{name=fileName.getText().toString();}
        saveRowOfData(name+".txt",data.toString());
    }

    public void clearData(View view) {
        //Clear all previous data
        X.clear();
        Y.clear();
        Z.clear();
        counter=0;
        data=new StringBuilder();
        editTextAx.setText("");
        editTextAy.setText("");
        editTextAz.setText("");
        fileName.setText("");
    }
}
