import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Semaphore;


/**
 * WebWorker is a subclass of Thread that downloads the content for one url. The "Fetch" buttons ultimately fork off a few WebWorkers.
 */


public class WebWorker extends Thread {
    String urlString;
    WebFrame webFrameRef;
    Semaphore semaphore;

    public WebWorker(String url, WebFrame frame, Semaphore semaphore) {
        urlString = url;
        webFrameRef = frame;
       this.semaphore =  semaphore;
    }

    public void run() {
        InputStream input = null;
        try {

            semaphore.acquire();

            //setting GUI components from webFrame
            webFrameRef.setRunningThreadCount(webFrameRef.getRunningThreadCount() + 1);
            webFrameRef.getNumberOfThreadRunning().setText(Integer.toString( webFrameRef.getRunningThreadCount()));
            webFrameRef.getStop().setEnabled(true);
            webFrameRef.getSingleThreadFetch().setEnabled(false);
            webFrameRef.getConcurrentFetch().setEnabled(false);
            webFrameRef.getStateOfWorker().setText("    Running !!!");


            System.out.println("Fetching...." + urlString);
            StringBuilder contents;
            try {
                webFrameRef.updateStatus("Downloading...",urlString);
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                connection.connect();
                connection.setConnectTimeout(500);
                input = connection.getInputStream();
                BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
                char[] array = new char[1000];
                int len;
                contents = new StringBuilder(1000);
                while ((len = reader.read(array, 0, array.length)) > 0) {
                    contents.append(array, 0, len);
                    Thread.sleep(100);
                }

                // call to save html of the url as text file
                download(url.getHost().toString()+".txt",contents.toString());


                //setting GUI components from webFrame
                webFrameRef.setBarPercent(webFrameRef.getBarPercent() + 1);
                webFrameRef.updateBar(webFrameRef.getBarPercent());
                webFrameRef.updateStatus("Downloaded",urlString);
                webFrameRef.setRunningThreadCount(webFrameRef.getRunningThreadCount() - 1);
                webFrameRef.getNumberOfThreadRunning().setText(Integer.toString( webFrameRef.getRunningThreadCount()));

                // if all urls downloaded, set fetch buttons visible and disable stop button
                if(webFrameRef.getBarPercent() == webFrameRef.getUrls().size()){
                    webFrameRef.getStateOfWorker().setText("    Ready !!!");
                    webFrameRef.setBarPercent(0);
                    webFrameRef.updateBar(webFrameRef.getBarPercent());
                    webFrameRef.getStop().setEnabled(false);
                    webFrameRef.getSingleThreadFetch().setEnabled(true);
                    webFrameRef.getConcurrentFetch().setEnabled(true);
                    System.out.println("All urls have been downloaded!!!");
                    System.out.println("NOTE: it may take up to 2 minutes\n for downloaded html text files to show up");
                }

                semaphore.release();

            } catch (InterruptedException e) {
                System.out.println("**** IMPORTANT MESSAGE **** \n"+this.getName() + " is interrupted ");
            }
        }
        // Otherwise control jumps to a catch...
        catch(MalformedURLException ignored) {
            System.out.println("Exception: " + ignored.toString());
        }
        catch(InterruptedException exception) {
            System.out.println(this.getName() + " is interrupted ");
        }
        catch(IOException ignored) {
            System.out.println("Exception: " + ignored.toString());
        }
        finally {
            try{
                if (input != null) input.close();
            }
            catch(IOException ignored) {}
        }
    }

    // download() method attempt to download an html file
    private void download(String fileName,String html) throws FileNotFoundException {
        File file = new File(fileName);
        PrintWriter out = new PrintWriter(file);
        out.println(html);
        out.close();
    }
}