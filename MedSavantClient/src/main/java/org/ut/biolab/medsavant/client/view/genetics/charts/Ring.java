package org.ut.biolab.medsavant.client.view.genetics.charts;

import java.awt.Color;
import java.awt.Shape;
import java.util.Vector;


public class Ring {
    private Vector<String> Labels;
    private Vector<Double> Values;
    private Vector<Color> Colors;
    private double x;
    private double y;
    private double radius;
    private double ringwidth;
    private double start;
    private double end;
    private Shape[] segments;

    public Ring(){
        Labels = new Vector<String>();
        Values = new Vector<Double>();
        Colors = new Vector<Color>();
        this.start = 0;
        this.end = 360;
        segments = null;
    }

    public void setStart(double start){
        this.start = start;
    }

    public void setEnd(double end){
        this.end = end;
    }

    public double getValue(int i){
    if(this.segments==null){
        this.createSegments();
    }
    return Values.get(i);
    }

    public int count(){
        if(this.segments==null){
            this.createSegments();
        }
        return Values.size();
    }

    public String getLabel(int index){
        return Labels.get(index);
    }

    public Color getColor(int index){
        return Colors.get(index);
    }

    public void setCenter(double x, double y){
        this.x = x;
        this.y = y;
    }

    public void setRadius(double radius){
        this.radius = radius;
    }

    public void setRingWidth(double width){
        this.ringwidth = width;
    }

    public void addItem(String label, double val, Color color){
        Labels.add(label);
        Values.add(val);
        Colors.add(color);
    }

    public Shape getSegment(int index){
        if(this.segments==null){
            this.createSegments();
        }

        return segments[index];
    }

    public void createSegments(){
        Shape[] shapes = new Shape[Values.size()];
        double sum = 0;
        double span = this.end - this.start;
        for(int i=0; i<Values.size(); i++){
            sum += Values.get(i);
        }

        double strt = this.start;
        for(int i=0; i<Values.size(); i++){
            double extent = (Values.get(i) / sum) *span;
            ArcSegment arc = new ArcSegment();
            shapes[i] = arc.Create(this.x,this.y,this.radius,this.ringwidth,strt,extent);
            strt += extent;
        }

        this.segments = shapes;

    }
}

