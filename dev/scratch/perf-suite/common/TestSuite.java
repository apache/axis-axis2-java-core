import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Created on Feb 11, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSuite {
    private Sampler sampler;
    private PrintWriter writer;
    private String message;
    
    public TestSuite(Sampler sampler,PrintWriter writer,String message){
        this.sampler = sampler;
        this.writer = writer;
        this.message = message;
        
    }
    
    public void runSuite() throws IOException{
       writer.write("Starting the Suite at "+ new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(new Date())+"\n");
        String includes = System.getProperty("include");
        if(includes == null){
            endToEndTest();
            loadIncreaseTest();
            loadTest();
        }else{
            if("end2end".equals(includes)){
                endToEndTest();
            }else if("load".equals(includes)){
                loadTest();
            }else{
                loadIncreaseTest();
            }
        }
        writer.write("Starting the Suite at "+ new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(new Date())+"\n");
    }
    
    private void endToEndTest() throws IOException{
        try {
            EndToEndTest end2endtest =
                new EndToEndTest(writer, sampler.createCopy(), message + ",test=\"End to End Test\"");
            end2endtest.invokeTest();
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("End to End test Failed\n");
            e.printStackTrace(writer);
            writer.flush();
        }
    
    }
    
    private void loadTest() throws IOException{
        int threads = 500;
        writer.write("Starting Load test with "+threads+"\n");
        try {
           
            int numberOfRequests = 10;
            Collecter c = new Collecter(threads * numberOfRequests,"test=\"Load increase test\"",writer);
            for (int i = 0; i < threads; i++) {
                System.out.println("Thread "+i+ " started");
                Thread thread = new Thread(new LoadTest(i,writer,sampler.createCopy(),message + ",test=\"Load Test\"",c,numberOfRequests));
                thread.start();
            }
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("Load test Failed\n");
            e.printStackTrace(writer);
        }
        writer.write("End Load test\n");
        writer.flush();
    }
 
    private void loadIncreaseTest() throws IOException{
        writer.write("Starting loadIncreaseTest\n");
        try {
            LoadIncreaseTest end2endtest =
                new LoadIncreaseTest(writer, sampler.createCopy(), message + "test=\"Load increase test\"");
            end2endtest.invokeTest();
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("loadIncreaseTest Failed\n");
            e.printStackTrace(writer);
            writer.flush();
        }
        writer.write("End loadIncreaseTest\n");
        writer.flush();
    }
}
