import java.net.URL;

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
public class TeastAxis2 {

    public static void main(String[] args) throws Exception {
        StructArraySampler sampler =
            new StructArraySampler(new URL(Constants.AXIS2_URL));
        long start;
        long end;
        sampler.init(1);
        start = System.currentTimeMillis();
        sampler.invoke();
        end = System.currentTimeMillis();
        sampler.end();

        sampler = new StructArraySampler(new URL(Constants.AXIS2_URL));
        sampler.init(10);
        start = System.currentTimeMillis();
        sampler.invoke();
        end = System.currentTimeMillis();
        sampler.end();

    }
}
