package ib.edu.lab9;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by Juju on 08.01.2019.
 */

public class FFT extends Activity {
    //serie danych zbieranych aktualnie
    private XYSeries X = new XYSeries("x");
    private XYSeries Y = new XYSeries("y");
    private XYSeries Z = new XYSeries("z");
    //elementy potrzebne do wykresu
    private XYMultipleSeriesRenderer multiple;
    private LinearLayout fftLayout;
    //tablice z danymi otrzynamymi z poprzedniej aktywnosci
    private double[] x;
    private double[] y;
    private double[] z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fft);

        //pobranie danych z poprzedniej aktywnosci
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            x = extras.getDoubleArray("FX");
            y = extras.getDoubleArray("FY");
            z = extras.getDoubleArray("FZ");

        }

        for (int i = 0; i < x.length; i++) {
            X.add(i, x[i]);
            Y.add(i, y[i]);
            Z.add(i, z[i]);
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
        multiple.setYAxisMin(0);

        //Show grid
        multiple.setShowGrid(true);

        fftLayout = (LinearLayout) findViewById(R.id.fftLayout);
        //Remove all previous data from plot
        fftLayout.removeAllViews();
        XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
        multipleSeriesDataset.addSeries(X);
        multipleSeriesDataset.addSeries(Y);
        multipleSeriesDataset.addSeries(Z);
        //Show new plot
        GraphicalView chartView = ChartFactory.getLineChartView(this, multipleSeriesDataset, multiple);
        fftLayout.addView(chartView);
    }
}