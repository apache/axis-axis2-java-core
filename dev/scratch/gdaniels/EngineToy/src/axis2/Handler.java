package axis2;

/**
 * The Handler interface.
 */
public interface Handler {
    /**
     *
     * @param context
     * @return
     * @throws Exception
     */
    boolean invoke(MessageContext context) throws Exception;

    void setOption(String name, Object value);
    Object getOption(String name);

    String getName();

    void setName(String name);
}
