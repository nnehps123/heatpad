package heatpad;

//package hotplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Element implements Runnable{
    private List<Element> neighbours;
    private double temperature;
    private double heatConstant;
    private boolean stopRequested;
    private static final int SLEEP_MS = 5;
    private String elementNumber;

    public Element(double temperature,double heatConstant){
        this(temperature,heatConstant,"0");
    }
    
    public Element(double temperature,double heatConstant,String elementNumber){
        this.elementNumber = elementNumber;
        neighbours = new ArrayList<Element>();
        stopRequested = false;
        this.temperature = temperature;
        this.heatConstant = heatConstant;
    }
    
    public synchronized double getTemperature(){
        return temperature;
    }
    
    public void run(){
        while(!stopRequested){
            double totalTemp = 0;
            for(Element neighbour:neighbours){
                totalTemp += neighbour.getTemperature();
            }
            //System.out.println("TOTAL TEMP FOR ELEMENT "+elementNumber+"  is "+getTemperature());
            double avg = totalTemp/((double)neighbours.size());
            //System.out.println("AVERAGE TEMP FOR ELEMENTS NEIGHBOURS FOR ELEMENT "+elementNumber+" is "+avg);
            updateTemperature(avg);
            try{
                Thread.sleep(SLEEP_MS);
            }
            catch(InterruptedException ie){}
        }
    }
    public synchronized void applyTempToElement(double appliedTemp)
    {   double t = (appliedTemp - temperature)*heatConstant;
        temperature = temperature + t;
    }
    public void addNeighbour(Element element)
    {   neighbours.add(element);
        //System.out.println("ELEMENT "+elementNumber+" HAS THIS MANY NEIGHBOURS NOW "+neighbours.size());
    }
    private synchronized void updateTemperature(double avg)
    {   if(neighbours.size() > 0)
        {
            double t = (avg - temperature)*(heatConstant/5.0);
            temperature = temperature + t;
        }
        //System.out.println("Element Number "+elementNumber+" is at temperature "+temperature);
    }
    public void requestStop()
    {   stopRequested = true;
    }
    public void start()
    {   stopRequested = false;
        Thread thread = new Thread(this);
        thread.start();
    }
    public synchronized void resetHeatConstant(double heatConstant)
    {   this.heatConstant = heatConstant;
    }
    
    public static void main(String[] args){
        Element[] elements = new Element[5];
        elements[0] = new Element(300.0,0.15,"0");
        elements[1] = new Element(0.0,0.15,"1");
        elements[2] = new Element(0.0,0.15,"2");
        elements[3] = new Element(0.0,0.15,"3");
        elements[4] = new Element(0.0,0.15,"4");
        //add neighbours
        for(int i=1;i<elements.length;i++){
            elements[0].addNeighbour(elements[i]);
            elements[i].addNeighbour(elements[0]);
        }
        for(int i=0;i<elements.length;i++){
            elements[i].start();
        }
        Scanner scanner = new Scanner(System.in);
        String input = "";
        do{
            System.out.println("Press Any key to Apply Temp to Element 0 or q to quit ");
            input = scanner.next();
            if(!input.equals("q")){
                System.out.println("APPLYING");
                elements[0].applyTempToElement(1000.0);
            }
        }
        while(!input.equals("q"));
        for(int i=0;i<elements.length;i++){
            elements[i].requestStop();
        }
    }
}
