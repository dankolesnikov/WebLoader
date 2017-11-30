import javafx.scene.layout.HBox;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class WebFrame extends JFrame {
    private ArrayList<String> urls;
    private JProgressBar pbar;
    private JLabel stateOfWorker;
    private  int MY_MINIMUM_BAR;
    private int runnningThreadCount;
    private JLabel numberOfThreadRunningLabel;
    private JLabel numberOfThreadRunning;
    private int userDefinedNumberOfThread;
    private boolean isInterrupt;
    private WebWorker a;

    public WebFrame(){
        a = new WebWorker();
        isInterrupt = false;
        MY_MINIMUM_BAR = 0;
        userDefinedNumberOfThread = 0;
        runnningThreadCount = 0;
        urls = new ArrayList<>();
        stateOfWorker = new JLabel("    not running");
        pbar = new JProgressBar();
        pbar.setMinimum(MY_MINIMUM_BAR);
        numberOfThreadRunning = new JLabel(Integer.toString(runnningThreadCount));
        numberOfThreadRunningLabel = new JLabel ("Number of Thread running: ");
    }

    public WebWorker getA() {
        return a;
    }

    public void setA(WebWorker a) {
        this.a = a;
    }

    public boolean isInterrupt() {
        return isInterrupt;
    }

    public void setInterrupt(boolean interrupt) {
        isInterrupt = interrupt;
    }

    public int getUserDefinedNumberOfThread() {
        return userDefinedNumberOfThread;
    }

    public void setUserDefinedNumberOfThread(int userDefinedNumberOfThread) {
        this.userDefinedNumberOfThread = userDefinedNumberOfThread;
    }

    public JLabel getNumberOfThreadRunning() {
        return numberOfThreadRunning;
    }

    public JLabel getNumberOfThreadRunningLabel() {
        return numberOfThreadRunningLabel;
    }

    public synchronized void setRunningThreadCount(int numberOfThread) {
        this.runnningThreadCount = numberOfThread;
    }

    public int getRunningThreadCount() {
        return runnningThreadCount;
    }

    public JLabel getStateOfWorker() {
        return stateOfWorker;
    }

    public JProgressBar getPbar() {
        return pbar;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public static void main(String[] args) throws IOException {

        WebFrame frame = new WebFrame();
        DefaultTableModel model = new DefaultTableModel(new String[] { "url", "status"}, 0);
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(600,300));
        //create buttons and put them in box
        Box vBox = Box.createVerticalBox();
        Box hBox = Box.createHorizontalBox();
        Box hBoxProgressBar = Box.createHorizontalBox();
        JButton singleThreadFetch = new JButton("Single-Thread Fetch");
        JButton concurrentFetch = new JButton("Concurrent Fetch");
        JButton stop = new JButton(" Stop");
        concurrentFetch.setEnabled(false);

        //create spinner to select number of thread to run concurrently
        JLabel headerLabel = new JLabel();
        headerLabel.setText("Number of Threads");
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, //initial value
                0, //min
                100, //max
                1);//step
        JSpinner spinner = new JSpinner(spinnerModel);

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Integer numberOfThreadSelected = (Integer)((JSpinner)e.getSource()).getValue();
                 frame.setUserDefinedNumberOfThread(numberOfThreadSelected);
                 System.out.println("number of thread selected is ---- "+frame.getUserDefinedNumberOfThread());
                 //spinner.setEnabled(false);
                 concurrentFetch.setEnabled(true);
            }
        });


        // listener for single thread fetch
        singleThreadFetch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread launcherThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                frame.setUserDefinedNumberOfThread(1);
                WebWorker a = frame.getA();
                Iterator<String> itrNew = frame.getUrls().iterator();

                while (itrNew.hasNext() && !a.isAlive()){
                    a = new WebWorker(itrNew.next(),frame,new Semaphore(frame.getUserDefinedNumberOfThread()));
                   frame.setA(a);

                    if(frame.isInterrupt){
                        break;
                    }

                    a.start();

                    //frame.getA().interrupt();
                   // System.out.println("a is alive: " + a.isAlive());

                    try {
                        a.join();
                       // System.out.println("a is alive: " + a.isAlive());
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                    }
                });
                launcherThread.start();
                singleThreadFetch.setEnabled(false);
                stop.setEnabled(true);
            }
        });

        // listener for concurrent thread fetch
        concurrentFetch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread launcherThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("inside launcher begin " + frame.getRunningThreadCount());
                        frame.setRunningThreadCount(frame.getRunningThreadCount() + 1);
                        frame.getNumberOfThreadRunning().setText(Integer.toString(frame.getRunningThreadCount()));
                        System.out.println("inside launcher increase " + frame.getRunningThreadCount());

                        Iterator<String> itrNew = frame.getUrls().iterator();
                        while (itrNew.hasNext()){
                            new WebWorker(itrNew.next(),frame,new Semaphore(frame.getUserDefinedNumberOfThread())).start();
                        }
                        //PROBLEM NOTE: it launch 4 threads cuz of 4 rows but they
                        // eventually line up for 2 semaphore
                        
                        frame.setRunningThreadCount(frame.getRunningThreadCount() - 1);
                        System.out.println("inside launcher decrease " + frame.getRunningThreadCount());
                        frame.getNumberOfThreadRunning().setText(Integer.toString(frame.getRunningThreadCount()));
                    }
                });
                launcherThread.start();
                concurrentFetch.setEnabled(false);
                spinner.setEnabled(true);
            }
        });

        //stop button top interrupt thread
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setInterrupt(true);
                frame.getA().interrupt();
                singleThreadFetch.setEnabled(true);
                stop.setEnabled(false);
            }
        });

        //load text file and add more rows
        frame.loadFile(args[0]);
        Iterator<String> itr = frame.getUrls().iterator();
        while (itr.hasNext()){
           model.addRow(new String[] {itr.next(), "put status here"});
        }

        //create progress bar base on number of urls are being working on
        frame.getPbar().setMaximum(frame.getUrls().size());
        frame.getStateOfWorker().setText("    Running Now!!!");

        //start adding all buttons, table, progressbar
        hBox.add(singleThreadFetch);
        hBox.add(concurrentFetch);
        hBox.add(headerLabel);
        hBox.add(spinner);
        hBox.add(stop);
        hBoxProgressBar.add(frame.getNumberOfThreadRunningLabel());
        hBoxProgressBar.add(frame.getNumberOfThreadRunning());
        hBoxProgressBar.add(frame.getStateOfWorker());
        hBoxProgressBar.add(frame.getPbar());
        vBox.add(hBox);
        vBox.add(hBoxProgressBar);
        vBox.add(scrollpane);
        frame.add(vBox);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

//        example loop to update progress bar
//        for (int i = 0; i <= frame.getUrls().size(); i++) {
//            final int percent = i;
//            try {
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        frame.updateBar(percent);
//                    }
//                });
//                java.lang.Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                ;
//            }
//        }
//
    }

    //load text file of urls
    public void loadFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {

            String line = br.readLine();
            while (line != null) {
                urls.add(line);
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    }

    //method to update progress bar
    public void updateBar(int newValue) {
        pbar.setValue(newValue);
    }
}