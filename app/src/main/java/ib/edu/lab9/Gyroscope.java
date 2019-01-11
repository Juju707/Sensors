package ib.edu.lab9;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;


public class Gyroscope extends Activity implements SensorEventListener {

    private SensorManager manager;
    private boolean isRunning=false;
    private EditText editTextAx;
    private EditText editTextAy;
    private EditText editTextAz;
    private Sensor gyroscope;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private XYMultipleSeriesRenderer multiple;
    private LinearLayout chartLayout;

    private boolean wasRunning=false;

    private XYSeries X;
    private XYSeries Y;
    private XYSeries Z;
    private StringBuilder data=new StringBuilder();

    private int count=0;
    private ArrayList<Double> values=new ArrayList<>();
    private Intent plot;

    private double[] fftX;
    private double[] fftY ;
    private double[] fftZ;

    private ArrayList<Double> TransX=new ArrayList<>();
    private ArrayList<Double> TransY=new ArrayList<>();
    private ArrayList<Double> TransZ=new ArrayList<>();
    private ArrayList<Float>  time;


    @Override
    //Method for initialization upon creation
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gyroscope);

        if(savedInstanceState!=null){
            count=savedInstanceState.getInt("count");
            X= (XYSeries) savedInstanceState.getSerializable("X");
            Y= (XYSeries) savedInstanceState.getSerializable("Y");
            Z= (XYSeries) savedInstanceState.getSerializable("Z");
            values= (ArrayList<Double>) savedInstanceState.getSerializable("values");
            wasRunning=savedInstanceState.getBoolean("wasRunning");
            isRunning=savedInstanceState.getBoolean("isRunning");
        }

        //initializing data series
        X=new XYSeries("X");
        Y=new XYSeries("Y");
        Z=new XYSeries("Z");
        //Initializing plot intent,text views and accelerometer sensor
        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        gyroscope=manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        manager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        editTextAx=(EditText)findViewById(R.id.xAxis);
        editTextAy=(EditText)findViewById(R.id.yAxis);
        editTextAz=(EditText)findViewById(R.id.zAxis);
        powerManager=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myapp:test");
        plot = new Intent(getBaseContext(), FFT.class);
        time=new ArrayList<>();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt("count",count);
        savedInstanceState.putSerializable("X",X);
        savedInstanceState.putSerializable("Y",Y);
        savedInstanceState.putSerializable("Z",Z);
        savedInstanceState.putSerializable("values",values);
        savedInstanceState.putBoolean("wasRunning",wasRunning);
        savedInstanceState.putBoolean("isRunning",isRunning);
    }
    @Override
    protected void onStop() {
        super.onStop();
        wasRunning=isRunning;
        isRunning=false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(wasRunning)isRunning=true;
    }

    //Method for registering the changes
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isRunning){
            if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
                //Setting values in fields to proper values
                float aX=event.values[0];
                float aY=event.values[1];
                float aZ=event.values[2];
                float timeStamp = event.timestamp;

                //Counter for number of measurement for plots
                count++;

                String x=String.format("%.2f", aX);
                String y=String.format("%.2f", aY);
                String z=String.format("%.2f", aZ);

                editTextAx.setText(x);
                editTextAy.setText(y);
                editTextAz.setText(z);

                //Adding data to series
                X.add(count, aX);
                Y.add(count, aY);
                Z.add(count, aZ);
                values.add((double) aZ);
                time.add(timeStamp);
                //Adding values for fourier's transformate
                TransX.add((double)aX);
                TransY.add((double)aY);
                TransZ.add((double)aZ);
                //Adding a string of data in case of reading from file
                String toData=count+";"+aX+";"+aY+";"+aZ+";"+timeStamp+"%";
                data.append(toData);
            }
        }
    }
    //In this app we don't change accuracy
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //Start button for staring measuring angular rotation and stopping it
    public void startMeasure(View view) {
        isRunning = !isRunning;
        if (isRunning) {
            X.clear();
            Y.clear();
            Z.clear();
            count=0;
            wakeLock.acquire();
        }else {
            wakeLock.release();

        }
    }

    public void drawPlot(View view) {
        isRunning=false;
        chartLayout =(LinearLayout) findViewById(R.id.plotLayout);


        //Creating rendererers for rendering data series and choosing custom properties
        XYSeriesRenderer xSeriesRenderer = new XYSeriesRenderer();
        xSeriesRenderer.setColor(Color.MAGENTA);
        xSeriesRenderer.setPointStrokeWidth(5);
        xSeriesRenderer.setLineWidth(4);

        XYSeriesRenderer ySeriesRenderer= new XYSeriesRenderer();
        ySeriesRenderer.setColor(Color.GREEN);
        ySeriesRenderer.setPointStrokeWidth(5);
        ySeriesRenderer.setLineWidth(4);

        XYSeriesRenderer zSeriesRenderer = new XYSeriesRenderer();
        zSeriesRenderer.setColor(Color.CYAN);
        zSeriesRenderer.setPointStrokeWidth(5);
        zSeriesRenderer.setLineWidth(4);

        //Adding the rendererers to a list and setting YAxis boundaries
        multiple=new XYMultipleSeriesRenderer();
        multiple.addSeriesRenderer(xSeriesRenderer);
        multiple.addSeriesRenderer(ySeriesRenderer);
        multiple.addSeriesRenderer(zSeriesRenderer);
        multiple.setYAxisMax(15);
        multiple.setYAxisMin(-15);

        //Show grid
        multiple.setShowGrid(true);
        //Frequency of sampling
        double sum=0;
        float  freq;

        for (int i = 1; i < time.size()-1; i++) {
            sum+=(1/(Math.abs((time.get(i))-(time.get(i-1)))/1000000000));
            Log.d("Gyroscope",Double.toString(sum));
        }freq=(float)(sum/(double) (time.size()-1));
        Log.d("Gyroscope",("Frequency: "+Float.toString(freq)));

        //Remove all previous data from plot
        chartLayout.removeAllViews();
        XYMultipleSeriesDataset multipleSeriesDataset=new XYMultipleSeriesDataset();
        multipleSeriesDataset.addSeries(X);
        multipleSeriesDataset.addSeries(Y);
        multipleSeriesDataset.addSeries(Z);
        //Show new plot
        GraphicalView chartView = ChartFactory.getLineChartView(this,multipleSeriesDataset,multiple);
        chartLayout.addView(chartView);



    }
    //Fast Fourier's transformate
    public void transform(View view) {
        isRunning=false;
        int length=(int)(Math.log(TransZ.size())/Math.log(2.0));
        if((2^length)<TransZ.size()) length++;

        //Fast Fourier Tranformate needs matrix which dimensions are 2's pow
        int dimension=(int)Math.pow(2,length);
        fftX = new double[dimension];
        fftY = new double[dimension];
        fftZ = new double[dimension];

        for (int i = 0; i < fftX.length; i++) {
            //If matrix is bigger than table then 0'es are assigned
            if(TransZ.size()-1<i ){
                fftX[i] = 0;
                fftY[i] = 0;
                fftZ[i] = 0;
            }else{
                fftX[i] = TransX.get(i);
                fftY[i] = TransY.get(i);
                fftZ[i] = TransZ.get(i);
            }        }

        double [] xTrans=Transformer.computeFFT(fftX);
        double [] yTrans=Transformer.computeFFT(fftY);
        double [] zTrans=Transformer.computeFFT(fftZ);

        //obliczone tablice przekazuje do aktywnosci
        plot.putExtra("FX", xTrans);
        plot.putExtra("FY", yTrans);
        plot.putExtra("FZ", zTrans);
        //rozpoczynam nowa aktywność
        startActivity(plot);
    }
}
