package heatpad;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

public class HotplateGUI extends JPanel implements ActionListener
{
    private static final double HEAT_K = 0.5;
    private static final int BLOCK_SIZE = 20;
    private HotplatePanel hotplate;
    public double heatToApply;
    public double maxTemp = 1000.0;
    public double minTemp = 0.0;
    public JPanel controlPanel;
    public JSlider tempSlider,hcSlider;
    public JButton exitButton,updateHCButton,updateTempButton;


    public HotplateGUI()
    {   super(new BorderLayout());
        controlPanel = new JPanel(new GridLayout(3,1));
        heatToApply = maxTemp;
        JPanel sPanel1 = new JPanel();
        sPanel1.setBorder(BorderFactory.createTitledBorder(" Temperature slider 0 - 1000 "));
        JPanel sPanel2 = new JPanel();
        sPanel2.setBorder(BorderFactory.createTitledBorder(" HeatConstant slider 0.01 - 1.0 "));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(" Button Updates "));

        tempSlider = new JSlider((int)minTemp,(int)maxTemp,(int)heatToApply);
        hcSlider = new JSlider(1,(int)100,(int)(HEAT_K*100.0));

        tempSlider.setPaintTicks(true);
        hcSlider.setPaintTicks(true);
        tempSlider.setMinorTickSpacing(20);
        hcSlider.setMinorTickSpacing(2);
        tempSlider.setMajorTickSpacing(100);
        hcSlider.setMajorTickSpacing(10);
        sPanel1.add(tempSlider);
        sPanel2.add(hcSlider);

        exitButton = new JButton(" Exit ");
        updateHCButton = new JButton(" Constant ");
        updateTempButton = new JButton(" Temperature ");

        buttonPanel.add(exitButton);
        buttonPanel.add(updateHCButton);
        buttonPanel.add(updateTempButton);
        exitButton.addActionListener(this);
        updateHCButton.addActionListener(this);
        updateTempButton.addActionListener(this);

        controlPanel.add(sPanel1);
        controlPanel.add(sPanel2);
        controlPanel.add(buttonPanel);
        hotplate = new HotplatePanel(20,20);

        add(hotplate,BorderLayout.CENTER);
        add(controlPanel,BorderLayout.SOUTH);
    }


    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        if(source == updateTempButton)
        {
            heatToApply = tempSlider.getValue();
        }
        else if(source == updateHCButton)
        {
            double newK = ((double)hcSlider.getValue())/100.0;
            System.out.println("NEW HEAT CONSTANT IS "+newK);
            hotplate.resetHeatConstant(newK);
        }
        else if(source == exitButton)
        {   stop();
            System.exit(0);
        }
    }
    private class HotplatePanel extends JPanel implements MouseListener,MouseMotionListener,ActionListener
    {
        private Timer timer;
        private final int TIME_MS = 50;
        private Element[][] elements;
        private Element selectedElement;
        private int rows;
        private int cols;
        private boolean elementPressed;

        public HotplatePanel(int rows,int cols){   
            super();
            setPreferredSize(new Dimension((rows*BLOCK_SIZE),(cols*BLOCK_SIZE)));
            this.cols = cols;
            this.rows = rows;
            selectedElement = null;
            setLayout(new GridLayout(rows,cols));
            elementPressed = false;

            setupElementsArray();
            addMouseMotionListener(this);
            addMouseListener(this);
            timer = new Timer(TIME_MS,this);
            timer.start();
        }
        public void resetHeatConstant(double newK)
        {
            for(int i=0;i<rows;i++)
            {   for(int j=0;j<cols;j++)
                {   elements[i][j].resetHeatConstant(newK);
                }
            }
        }
        private void setupElementsArray()
        {
            elements = new Element[rows][cols];
            for(int i=0;i<rows;i++)
            {   for(int j=0;j<cols;j++)
                {   //if(i%2==0 && j%2==0) elements[i][j] = new Element(0.0,HEAT_K,"R"+i+",C"+j);
                   //else elements[i][j] = new Element(1000.0,HEAT_K,"R"+i+",C"+j);
                    elements[i][j] = new Element(0.0,HEAT_K,"R"+i+",C"+j);
                }
            }
            //now add neighbours
            for(int i=0;i<rows;i++)
            {   for(int j=0;j<cols;j++)
                {
                    if((i-1) >= 0) elements[i][j].addNeighbour(elements[i-1][j]);
                    if((i+1) < rows) elements[i][j].addNeighbour(elements[i+1][j]);
                    if((j-1) >= 0) elements[i][j].addNeighbour(elements[i][j-1]);
                    if((j+1) < cols) elements[i][j].addNeighbour(elements[i][j+1]);
                }
            }
            for(int i=0;i<rows;i++)
            {   for(int j=0;j<cols;j++)
                {   elements[i][j].start();
                }
            }
            //now start the damn things
        }
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            for(int i=0;i<rows;i++)
            {
                for(int j=0;j<cols;j++)
                {
                    double t = elements[i][j].getTemperature();
                    //double red = (double) (t/(maxTemp-minTemp))*255.0;
                    double red = t;
                    if(red > 255.0) red = 255.0;

                    double blue = 255.0 - red;
                    //System.out.println("RED ="+red+" BLUE = "+blue);
                   g.setColor(new Color((int)red,(int)0,(int)blue));
                    g.fillRect(i*BLOCK_SIZE, j*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.RED);
                    g.drawRect(i*BLOCK_SIZE, j*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }

        }

        public void mousePressed(MouseEvent event)
        {   //set selected Element from the position
            int j = event.getY()/BLOCK_SIZE;
            int i = event.getX()/BLOCK_SIZE;
            System.out.println("ROWS = "+i+" AND COL = "+j);
            if(i<rows && j<cols && j>=0 && i>=0) selectedElement = elements[i][j];
            elementPressed = true;
        }
        public void mouseReleased(MouseEvent event)
        {   selectedElement = null;
        }
        public void actionPerformed(ActionEvent event)
        {
            Object source = event.getSource();
            if(source == timer)
            {   //apply heat to the element at the position
                if(selectedElement != null)
                {   //System.out.println("APPLYING TEMPERATURE");
                    selectedElement.applyTempToElement(heatToApply);
                }
                repaint();
            }
        }
        public void stop()
        {   for(int i=0;i<rows;i++)
            {   for(int j=0;j<cols;j++)
                {   elements[i][j].requestStop();
                }
            }
            timer.stop();
        }
        public void mouseClicked(MouseEvent arg0){}
        public void mouseEntered(MouseEvent arg0){}
        public void mouseExited(MouseEvent arg0) {}
        public void mouseDragged(MouseEvent event)
        {
            int j = event.getY()/BLOCK_SIZE;
            int i = event.getX()/BLOCK_SIZE;
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>ROWS = "+i+" AND COL = "+j);
            if(i<rows && j<cols && j>=0 && i>=0)selectedElement = elements[i][j];
        }
        public void mouseMoved(MouseEvent event) {}
    }
    public void stop()
    {
        hotplate.stop();
    }
    public static void main(String[] args)
    {   
      System.out.println("===========HOT PLATE GUI, ASSIGNMENT 1============");
      JFrame frame = new JFrame(" HOT PLATE GUI, ASSIGNMENT 1 ");
      final HotplateGUI hotplateGUI = new HotplateGUI();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(hotplateGUI);
      frame.pack();
      frame.addWindowListener(new WindowAdapter()
      {  public void windowClosing(WindowEvent we)
         {  
            hotplateGUI.stop();
            System.exit(0);
         }
      });
      // Position the frame in the middle of the screen.
      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dm = tk.getScreenSize();
      int screenHeight = dm.height;
      int screenWidth = dm.width;
      //frame.setSize(400,400);
      frame.setLocation(new java.awt.Point((screenWidth/2) -
            (frame.getWidth()/2), (screenHeight/2) -(frame.getHeight()/2)));
      frame.setVisible(true);

    }
}
