
package interop.doclit;

public class WSDLInteropTestDocLitPortType {

    public java.lang.String echoString(java.lang.String a) throws java.rmi.RemoteException {
        return a;
    }
    public String[] echoStringArray(String[] a) throws java.rmi.RemoteException {
        return a;
    }
    public SOAPStruct echoStruct(SOAPStruct a) throws java.rmi.RemoteException {
        return a;
    }
    public void echoVoid() throws java.rmi.RemoteException {
    }

}
