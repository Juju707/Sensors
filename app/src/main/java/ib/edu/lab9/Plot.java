package ib.edu.lab9;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

//An activity for drawing plots
public class Plot extends Activity {

    //Attributes
    private int steps;

    private XYSeries X;
    private XYSeries Y;
    private XYSeries Z;


    private double[] currentData;

    private XYSeries readX;
    private XYSeries readY;
    private XYSeries readZ;

    private LinearLayout chartLayout;
    private EditText fileName;
    private TextView numberOfStepsText;

    private XYMultipleSeriesRenderer multiple;
    private ArrayList<Double> allValues;
    private ArrayList<Double>  time;


    //Method that initializes after creating this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //Setting the right layout
        setContentView(R.layout.plot);
        //The counter for counting the number of measurements
        steps = 0;
        //An array list containing all lines of data from file
        allValues = new ArrayList<>();
        time=new ArrayList<>();

        //Getting data from Accelerometer
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            X = (XYSeries) extras.getSerializable("Xdata");
            Y = (XYSeries) extras.getSerializable("Ydata");
            Z = (XYSeries) extras.getSerializable("Zdata");
            currentData = extras.getDoubleArray("Number of steps");
        }

        //Creating rendererers for rendering data series and choosing custom properties
        XYSeriesRenderer xSeriesRenderer = new XYSeriesRenderer();
        xSeriesRenderer.setColor(Color.MAGENTA);
        xSeriesRenderer.setPointStrokeWidth(5);
        xSeriesRenderer.setLineWidth(4);

        XYSeriesRenderer ySeriesRenderer = new XYSeriesRenderer();
        ySeriesRenderer.setColor(Color.GREEN);
        ySeriesRenderer.setPointStrokeWidth(5);
        ySeriesRenderer.setLineWidth(4);

        XYSeriesRenderer zSeriesRenderer = new XYSeriesRenderer();
        zSeriesRenderer.setColor(Color.CYAN);
        zSeriesRenderer.setPointStrokeWidth(5);
        zSeriesRenderer.setLineWidth(4);

        //Adding the rendererers to a list and setting YAxis boundaries
        multiple = new XYMultipleSeriesRenderer();
        multiple.addSeriesRenderer(xSeriesRenderer);
        multiple.addSeriesRenderer(ySeriesRenderer);
        multiple.addSeriesRenderer(zSeriesRenderer);
        multiple.setYAxisMax(15);
        multiple.setYAxisMin(-15);
        //Show grid
        multiple.setShowGrid(true);

        //Initialize showcasing elements
        chartLayout = (LinearLayout) findViewById(R.id.chartLayout);
        fileName = (EditText) findViewById(R.id.editTxtFileName);
        numberOfStepsText = (TextView) findViewById(R.id.stepsTxt);

    }

    public int countSteps(double[] values) {
        //To count the number of steps I take 7 points (one main,3 before it and 3 after it)
        //And I compare whether my main point is surely the peak
        //If it is it will be bigger than the rest
        //And I go through all values to count all the steps
        double sum=0;
        float  freq;
        for (int i = 3; i < values.length - 3; i++) {
            if (values[i] > values[i - 3] && values[i] > values[i - 2] && values[i] > values[i - 1]
                    && values[i] > values[i + 1] && values[i] > values[i + 2] && values[i] > values[i + 3])
                steps++;
        }
        //Frequency of sampling
        for (int i = 1; i < time.size()-1; i++) {
            sum+=(1/(Math.abs((time.get(i))-(time.get(i-1)))/1000000000));}
        freq=(float)(sum/(double) (time.size()-1));
        Log.d("Plot",("Frequency: "+Float.toString(freq)));
        return steps;
    }

    //To draw from currently stored data
    public void drawCurrent(View view) {
        //Deleting all traces of previous plots
        steps = 0;
        chartLayout.removeAllViews();
        //Adding new series to data set
        XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
        multipleSeriesDataset.addSeries(X);
        multipleSeriesDataset.addSeries(Y);
        multipleSeriesDataset.addSeries(Z);

        //Swo the number of steps
        numberOfStepsText.setText(String.valueOf(countSteps(currentData)) + " steps");

        //Show plot
        GraphicalView chartView = ChartFactory.getLineChartView(this, multipleSeriesDataset, multiple);
        chartLayout.addView(chartView);


    }

    //To draw from data in file
    public void drawFromFile(View view) {
        //Deleting all traces of previous plots
        steps = 0;
        chartLayout.removeAllViews();
        //An exception to ensure a file name fill be given
        if (fileName.getText().toString().equals("") || fileName.getText().toString() == null) {
            Toast.makeText(this, "Write file name", Toast.LENGTH_LONG).show();
        } else {
            //Setting file name and reading data from it
            String name = fileName.getText().toString() + ".txt";
            readData(name);
            //Values from Array must be saved in form of a table
            double[] savedData = new double[allValues.size()];
            for (int i = 0; i < savedData.length; i++) {
                savedData[i] = allValues.get(i);
            }
            //Showing number of steps
            numberOfStepsText.setText(String.valueOf(countSteps(savedData)) + " steps");
            //Adding data to plot
            XYMultipleSeriesDataset multipleDatasets = new XYMultipleSeriesDataset();
            multipleDatasets.addSeries(readX);
            multipleDatasets.addSeries(readY);
            multipleDatasets.addSeries(readZ);
            //Showing plot
            GraphicalView chartView = ChartFactory.getLineChartView(this, multipleDatasets, multiple);
            chartLayout.addView(chartView);
        }
    }

    //To read data from file
    public void readData(String name) {

        int counter = 0;
        double aX = 0;
        double aY = 0;
        double aZ = 0;
        double timeStamp=0;
        //Initialize series
        readX = new XYSeries("Read X data");
        readY = new XYSeries("Read Y data");
        readZ = new XYSeries("Read Z data");
        allValues.removeAll(allValues);
        try {
            //To read form file we use classes below
            FileInputStream fis = openFileInput(name);
            InputStreamReader reader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String file = null;
            //If the file isn't empty, there will be consequences
            if ((file = bufferedReader.readLine()) != null) {

                //Measures are split by % regex
                String[] text = file.split("%");
                String[] line;

                for (int i = 0; i < text.length; i++) {
                    //Each series had values split with ";"
                    line = text[i].split(";");

                    counter = Integer.valueOf(line[0]);

                    aX = Double.valueOf(line[1]);
                    aY = Double.valueOf(line[2]);
                    aZ = Double.valueOf(line[3]);
                    timeStamp=Double.valueOf(line[4]);

                    readX.add(counter, aX);
                    readY.add(counter, aY);
                    readZ.add(counter, aZ);
                    //All Values is a table for counting steps
                    allValues.add(aZ);
                    time.add(timeStamp);
                }
            }

            bufferedReader.close();
            reader.close();
            fis.close();

        } catch (java.io.IOException e) {
            //Toast will be shown if the file does not exist or data cannot be read
            Toast.makeText(this, "Cannot read ", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}
