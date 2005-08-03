package userguide.clients;

import org.apache.axis2.clientapi.Call;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: saminda
 * Date: Aug 1, 2005
 * Time: 1:40:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientSideModuleEngagement {
    public static void main(String[] args) throws Exception{
        String home = System.getProperty("user.home");
        // create this folder at your home. This folder could be anything
        //then create the "modules" folder

        File repository = new File(home+File.separator+"client-repository");
        if (!repository.exists()) {
            throw new FileNotFoundException("Repository Doesnot Exist");
        }
        //copy the LoggingModule.mar to "modules" folder.
        //then modify the axis2.xml that is generating there according to
        //phases that being included in the "module.xml"
        Call call = new Call(repository.getAbsolutePath());
        call.engageModule(new QName("LoggingModule"));
    }
}
