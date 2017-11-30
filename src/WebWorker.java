import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Semaphore;


/**
 * WebWorker is a subclass of Thread that downloads the content for one url. The "Fetch" buttons ultimately fork off a few WebWorkers.
 */


public class WebWorker extends Thread {
    /*
      This is the core web/download i/o code...*/

    String urlString;
    WebFrame webFrameRef;
    static Semaphore semaphore;

    public WebWorker(){
        urlString = "";
        webFrameRef = null;
        semaphore = new Semaphore(0);
    }

    public WebWorker(String url, WebFrame frame, Semaphore semaphore) {
        urlString = url;
        webFrameRef = frame;
       this.semaphore =  semaphore;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void setWebFrameRef(WebFrame webFrameRef) {
        this.webFrameRef = webFrameRef;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUrlString() {
        return urlString;
    }

    public WebFrame getWebFrameRef() {
        return webFrameRef;
    }

    public void run() {
        InputStream input = null;

        try {

            System.out.println("==================================");
            System.out.println("the thread is "+this.getName());
            System.out.println("number of semaphore before acquire() " + semaphore.availablePermits());
            webFrameRef.setRunningThreadCount(webFrameRef.getRunningThreadCount() + 1);
            webFrameRef.getNumberOfThreadRunning().setText(Integer.toString( webFrameRef.getRunningThreadCount()));
            System.out.println("Webworker running thread count increase " + webFrameRef.getRunningThreadCount());

            semaphore.acquire();

            System.out.println("Fetching...." + urlString);
            StringBuilder contents = null;
            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();

                // Set connect() to throw an IOException
                // if connection does not succeed in this many msecs.
                connection.setConnectTimeout(5000);

                connection.connect();
                input = connection.getInputStream();

                BufferedReader reader  = new BufferedReader(new InputStreamReader(input));

                char[] array = new char[1000];
                int len;
                contents = new StringBuilder(1000);
                while ((len = reader.read(array, 0, array.length)) > 0) {
                    // System.out.println("Fetching...." + urlString + len);
                    contents.append(array, 0, len);
                    Thread.sleep(100);
                }

               // System.out.print(contents.toString());

                System.out.println("number of semaphore after acquire() " + semaphore.availablePermits());

                semaphore.release();

                System.out.println("number of semaphore after release() " + semaphore.availablePermits());

                webFrameRef.setRunningThreadCount(webFrameRef.getRunningThreadCount() - 1);
                webFrameRef.getNumberOfThreadRunning().setText(Integer.toString( webFrameRef.getRunningThreadCount()));
                System.out.println("Webworker running thread count decrease " + webFrameRef.getRunningThreadCount());

            } catch (InterruptedException e) {
                System.out.println("**** IMPORTANT MESSAGE **** \n"+this.getName() + " is interrupted ");

               // e.printStackTrace();
        }


        }
        // Otherwise control jumps to a catch...
        catch(MalformedURLException ignored) {
            System.out.println(this.getName() + " is interrupted ");

            System.out.println("Exception: " + ignored.toString());
        }
        catch(InterruptedException exception) {
            // YOUR CODE HERE
            // deal with interruption
            System.out.println(this.getName() + " is interrupted ");
            //System.out.println("Exception: " + exception.toString());
        }
        catch(IOException ignored) {
            System.out.println(this.getName() + " is interrupted ");

            System.out.println("Exception: " + ignored.toString());
        }
        // "finally" clause, to close the input stream
        // in any case
        finally {
            try{
                if (input != null) input.close();
            }
            catch(IOException ignored) {}
        }
    }

    // download() method attempt to download an html file
    private void download(){

    }


}