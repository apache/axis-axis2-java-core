/*
 * Created on Feb 9, 2005
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
public class LoadIncreaseTest {
    public static void main(String[] args) {
        int count = 0;
        while(true) {
            count = count + 1000;
            System.out.print("Invoke ="+ count + " |");
            
            Sampler sampler = new Sampler(count);
            try {
                sampler.invokeService();
            } catch (Exception e) {
                System.out.println("Failed at the size " + count);
                e.printStackTrace();
                break;
            }
        }
    }
}
