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
public class StructArraySampler implements Sampler {
    
    private EchoStruct[] objs;
    private EchoStruct[] results;
    private EchoStub stub;
    private URL url;
    
    public StructArraySampler(URL url){
        this.url = url;
    }

    public void init(int arraysize) throws Exception {
        this.objs = new EchoStruct[arraysize];
        this.stub = new EchoStub(url);
        for (int i = 0; i < objs.length; i++) {
            objs[i] = new EchoStruct();
            objs[i].setValue1("Ruy Lopez" + i);
            objs[i].setValue2("Kings Gambit" + i);
            objs[i].setValue3(345);
            objs[i].setValue4("Kings Indian Defence" + i);
            objs[i].setValue5("Musio Gambit" + i);
            objs[i].setValue6("Benko Gambit" + i);
            objs[i].setValue7("Secillian Defance" + i);
            objs[i].setValue8("Queens Gambit" + i);
            objs[i].setValue9("Queens Indian Defense" + i);
            objs[i].setValue10("Alekine's Defense" + i);
            objs[i].setValue11("Perc Defense" + i);
            objs[i].setValue12("Scotch Gambit");
            objs[i].setValue13("English Opening" + i);
        }

    }

    /* (non-Javadoc)
     * @see Sampler#invoke()
     */
    public void invoke() throws Exception {
        results = stub.echoEchoStructArray(objs);

    }

    /* (non-Javadoc)
     * @see Sampler#end()
     */
    public void end() throws Exception {

        for (int i = 0; i < results.length; i++) {
            if (!results[i].equals(objs[i])) {
                throw new Exception("Assertion Failed");
            }
        }

    }
    /* (non-Javadoc)
     * @see Sampler#createCopy()
     */
    public Sampler createCopy() {
        
        return new StructArraySampler(url);
    }

}
