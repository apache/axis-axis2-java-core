
public interface Sampler {
    public void init(int arraysize)throws Exception;
    public void invoke()throws Exception;
    public void end()throws Exception;
    public Sampler createCopy();
}
