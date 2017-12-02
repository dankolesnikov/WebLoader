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
    private DefaultTableModel model;
    private WebWorker[] referenceOfWorkers;
    private JLabel stateOfWorker;
    private JLabel numberOfThreadRunningLabel;
    private JLabel numberOfThreadRunning;
    private JButton singleThreadFetch;
    private JButton concurrentFetch;
    private JButton stop;
    private int userDefinedNumberOfThread;
    private int barPercent;
    private int MY_MINIMUM_BAR;
    private int runnningThreadCount;
    private boolean isInterrupt;

    public WebFrame(){
        model = new DefaultTableModel(new String[] { "url", "status"}, 0);
        singleThreadFetch = new JButton("Single-Thread Fetch");
        concurrentFetch = new JButton("Concurrent Fetch");
        stop = new JButton(" Stop");
        referenceOfWorkers = new WebWorker[0];
        barPercent = 0;
        isInterrupt = false;
        MY_MINIMUM_BAR = 0;
        userDefinedNumberOfThread = 0;
        runnningThreadCount = 0;
        urls = new ArrayList<>();
        stateOfWorker = new JLabel("    Ready!!!");
        pbar = new JProgressBar();
        pbar.setMinimum(MY_MINIMUM_BAR);
        numberOfThreadRunning = new JLabel(Integer.toString(runnningThreadCount));
        numberOfThreadRunningLabel = new JLabel ("Number of Thread running: ");
    }

    public static void main(String[] args) throws IOException {
        WebFrame frame = new WebFrame();

        JTable table = new JTable(frame.getModel());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(600,300));

        //create buttons and put them in box
        Box vBox = Box.createVerticalBox();
        Box hBox = Box.createHorizontalBox();
        Box hBoxProgressBar = Box.createHorizontalBox();
        frame.getConcurrentFetch().setEnabled(false);

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
                frame.getConcurrentFetch().setEnabled(true);
            }
        });


        // listener for single thread fetch
        frame.getSingleThreadFetch().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread launcherThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                frame.setUserDefinedNumberOfThread(1);
                WebWorker [] a = frame.getReferenceOfWorkers();
                Iterator<String> itrNew = frame.getUrls().iterator();
                Semaphore tempSem = new Semaphore(frame.getUserDefinedNumberOfThread());
                int count = 0;
                while (itrNew.hasNext()){
                    a[count] = new WebWorker(itrNew.next(),frame,tempSem);
                    if(frame.isInterrupt){
                        break;
                    }
                        a[count].start();
                    try {
                        a[count].join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                    }
                });
                launcherThread.start();
                frame.getSingleThreadFetch().setEnabled(false);
                frame.getStop().setEnabled(true);
            }
        });

        // listener for concurrent thread fetch
       frame.getConcurrentFetch().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread launcherThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        frame.setRunningThreadCount(frame.getRunningThreadCount() + 1);
                        frame.getNumberOfThreadRunning().setText(Integer.toString(frame.getRunningThreadCount()));

                        WebWorker[] a = frame.getReferenceOfWorkers();
                        int count = 0;
                        Iterator<String> itrNew = frame.getUrls().iterator();
                        Semaphore tempSem = new Semaphore(frame.getUserDefinedNumberOfThread());
                        while (itrNew.hasNext()){
                          a[count] = new WebWorker(itrNew.next(),frame,tempSem);
                          a[count].start(); //start as many workers as needed
                          count++;
                        }

                        frame.setRunningThreadCount(frame.getRunningThreadCount() - 1);
                        frame.getNumberOfThreadRunning().setText(Integer.toString(frame.getRunningThreadCount()));
                    }
                });
                launcherThread.start();
                frame.getConcurrentFetch().setEnabled(false);
                spinner.setEnabled(true);
            }
        });

        // listener stop button top interrupt thread
        frame.getStop().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setInterrupt(true);
                WebWorker[] temp = frame.getReferenceOfWorkers();

                for (int i = 0; i < temp.length;i++) {
                    if(temp[i] != null) {
                        temp[i].interrupt();
                    }
                }
                frame.getSingleThreadFetch().setEnabled(true);
                frame.getStop().setEnabled(false);
            }
        });

        //load text file and add more rows
        frame.loadFile(args[0]);
        Iterator<String> itr = frame.getUrls().iterator();
        while (itr.hasNext()){
            frame.getModel().addRow(new String[] {itr.next(), "Not Downloaded"});
        }

        //create reference of workers array equal to the size of table
        WebWorker[] temp = new WebWorker[frame.getUrls().size()];
        frame.setReferenceOfWorkers(temp);

        //create progress bar base on number of urls are being working on
        frame.getPbar().setMaximum(frame.getUrls().size());

        //start adding all buttons, table, progressbar
        hBox.add( frame.getSingleThreadFetch());
        hBox.add(frame.getConcurrentFetch());
        hBox.add(headerLabel);
        hBox.add(spinner);
        hBox.add(frame.getStop());
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

    }

    // method to loop through and search the table to update status of each url
    public void updateStatus(String status, String url){
        for(int i = 0; i < urls.size(); i++){
            if(model.getValueAt(i,0).equals(url)){
                model.setValueAt(status,i,1);
            }
        }

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

    //******************************************************
    //*** Getters and Setters methods for all properties ***
    //******************************************************

    public DefaultTableModel getModel() {
        return model;
    }

    public JButton getConcurrentFetch() {
        return concurrentFetch;
    }

    public JButton getSingleThreadFetch() {
        return singleThreadFetch;
    }

    public JButton getStop() {
        return stop;
    }

    public WebWorker[] getReferenceOfWorkers() {
        return referenceOfWorkers;
    }

    public void setReferenceOfWorkers(WebWorker[] a) {
        this.referenceOfWorkers = a;
    }

    public int getBarPercent() {
        return barPercent;
    }

    public void setBarPercent(int barPercent) {
        this.barPercent = barPercent;
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
}