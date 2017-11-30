import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * WebWorker is a subclass of Thread that downloads the content for one url. The "Fetch" buttons ultimately fork off a few WebWorkers.
 */


public class WebWorker extends Thread {
    /*
      This is the core web/download i/o code...*/
    String urlString;
    WebFrame webFrameRef;
    public WebWorker(String url, WebFrame frame) {
        urlString = url;
        webFrameRef = frame;
    }

    public void run() {
        webFrameRef.setNumberOfThread(webFrameRef.getNumberOfThread() + 1);
        webFrameRef.getNumberOfThreadRunning().setText(Integer.toString( webFrameRef.getNumberOfThread()));
        System.out.println("inside webworker increase " + webFrameRef.getNumberOfThread());
       // System.out.println("Fetching...." + urlString);
        InputStream input = null;
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
            webFrameRef.setNumberOfThread(webFrameRef.getNumberOfThread() - 1);
            System.out.println("inside webworker decrease " + webFrameRef.getNumberOfThread());

            webFrameRef.getNumberOfThreadRunning().setText(Integer.toString( webFrameRef.getNumberOfThread()));

        }
        // Otherwise control jumps to a catch...
        catch(MalformedURLException ignored) {
            System.out.println("Exception: " + ignored.toString());
        }
        catch(InterruptedException exception) {
            // YOUR CODE HERE
            // deal with interruption
            System.out.println("Exception: " + exception.toString());
        }
        catch(IOException ignored) {
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