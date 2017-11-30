import javafx.scene.layout.HBox;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class WebFrame extends JFrame {
    private ArrayList<String> urls;
    private JProgressBar pbar;
    private JLabel stateOfWorker;
    private  int MY_MINIMUM_BAR = 0 ;
    private int numberOfThread;
    private JLabel numberOfThreadRunningLabel;
    private JLabel numberOfThreadRunning;

    public WebFrame(){
        numberOfThread = 0;

        urls = new ArrayList<>();
        stateOfWorker = new JLabel("    not running");
        pbar = new JProgressBar();
        pbar.setMinimum(MY_MINIMUM_BAR);
        numberOfThreadRunning = new JLabel(Integer.toString(numberOfThread));
        numberOfThreadRunningLabel = new JLabel ("Number of Thread running: ");
    }

    public JLabel getNumberOfThreadRunning() {
        return numberOfThreadRunning;
    }

    public JLabel getNumberOfThreadRunningLabel() {
        return numberOfThreadRunningLabel;
    }

    public synchronized void setNumberOfThread(int numberOfThread) {
        this.numberOfThread = numberOfThread;
    }

    public int getNumberOfThread() {
        return numberOfThread;
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

        //create spinner to select number of thread to run concurrently
        JLabel headerLabel = new JLabel();
        headerLabel.setText("Number of Threads");
        SpinnerModel spinnerModel = new SpinnerNumberModel(5, //initial value
                0, //min
                100, //max
                1);//step
        JSpinner spinner = new JSpinner(spinnerModel);
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // statusLabel.setText("Value : " + ((JSpinner)e.getSource()).getValue());
            }
        });


        //TODO: Grab a txt file with urls; load it; fetch urls; for each url create a new row in the table and 2 buttons: Fetch, Stop;
        // TODO: For each url create a webworker
        // TODO: Implement state design pattern

        frame.loadFile(args[0]);

        Iterator<String> itr = frame.getUrls().iterator();
        while (itr.hasNext()){
           model.addRow(new String[] {itr.next(), "put status here"});

        }

        Thread launcherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("inside launcher begin " + frame.getNumberOfThread());
                frame.setNumberOfThread(frame.getNumberOfThread() + 1);
                frame.getNumberOfThreadRunning().setText(Integer.toString(frame.getNumberOfThread()));
                System.out.println("inside launcher increase " + frame.getNumberOfThread());
                Iterator<String> itrNew = frame.getUrls().iterator();
        while (itrNew.hasNext()){
            new WebWorker(itrNew.next(),frame).start();
        }

                frame.setNumberOfThread(frame.getNumberOfThread() - 1);
                System.out.println("inside launcher decrease " + frame.getNumberOfThread());

                frame.getNumberOfThreadRunning().setText(Integer.toString(frame.getNumberOfThread()));

            }
        });

        launcherThread.start();


//        Iterator<String> itrNew = frame.getUrls().iterator();
//        while (itrNew.hasNext()){
//            new WebWorker(itrNew.next()).start();
//        }
//
//        WebWorker worker1 = new WebWorker(url1);
//        worker1.run();





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

        //example loop to update progress bar
        for (int i = 0; i <= frame.getUrls().size(); i++) {
            final int percent = i;
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.updateBar(percent);
                    }
                });
                java.lang.Thread.sleep(1000);
            } catch (InterruptedException e) {
                ;
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
}