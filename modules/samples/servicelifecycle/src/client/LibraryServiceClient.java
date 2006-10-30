package client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import sample.servicelifecycle.bean.Book;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

public class LibraryServiceClient {
    public static void main(String[] args) throws Exception {
        String epr = "http://127.0.0.1:8000/axis2/services/Library";
        RPCServiceClient rpcClient = new RPCServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference(epr));
        rpcClient.setOptions(opts);
        LibraryServiceClient client = new LibraryServiceClient();

        //TO list all the books
        client.listAllBook(rpcClient);
        //To register for the system
        System.out.println(client.register("axis2", "abc", rpcClient));
        //To login to the system
        System.out.println(client.login("axis2", "abc", rpcClient));
        //To lend a book
        client.lendBook("1-56592-229-8", "axis2", rpcClient);
        // to view the books in the lend section
        client.listLendBook(rpcClient);
        //list all the available books
        client.listAvailableBook(rpcClient);
        // returinging the book
        client.returnBook("1-56592-229-8", rpcClient);
        // view the lend list again
        client.listLendBook(rpcClient);
    }

    public void returnBook(String isbn, RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:returnBook");
        ArrayList args = new ArrayList();
        args.add(isbn);
        rpcClient.invokeRobust(new QName("http://servicelifecycle.sample/xsd",
                "returnBook"), args.toArray());
    }

    public void lendBook(String isbn, String userName,
                         RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:lendBook");
        ArrayList args = new ArrayList();
        args.add(isbn);
        args.add(userName);
        Object obj [] = rpcClient.invokeBlocking(new QName("http://servicelifecycle.sample/xsd",
                "lendBook"), args.toArray(), new Class[]{Book.class});
        Book book = (Book) obj[0];
        System.out.println("Title : " + book.getTitle());
        System.out.println("Isbn : " + book.getIsbn());
        System.out.println("Author : " + book.getAuthor());

    }

    public boolean register(String userName,
                            String passWord,
                            RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:register");
        ArrayList args = new ArrayList();
        args.add(userName);
        args.add(passWord);
        Object obj [] = rpcClient.invokeBlocking(new QName("http://servicelifecycle.sample/xsd",
                "register"), args.toArray(), new Class[]{Boolean.class});
        return ((Boolean) obj[0]).booleanValue();
    }

    public boolean login(String userName,
                         String passWord,
                         RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:login");
        ArrayList args = new ArrayList();
        args.add(userName);
        args.add(passWord);
        Object obj [] = rpcClient.invokeBlocking(new QName("http://servicelifecycle.sample/xsd",
                "login"), args.toArray(), new Class[]{Boolean.class});
        return ((Boolean) obj[0]).booleanValue();
    }

    public void listAvailableBook(RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:listAvailableBook");
        OMElement elemnt = rpcClient.invokeBlocking(new QName("http://servicelifecycle.sample/xsd",
                "listAvailableBook"), new Object[]{null});
        System.out.println(elemnt);
    }

    public void listAllBook(RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:listAllBook");
        OMElement elemnt = rpcClient.invokeBlocking(new QName("http://servicelifecycle.sample/xsd",
                "listAllBook"), new Object[]{null});
        System.out.println(elemnt);
    }

    public void listLendBook(RPCServiceClient rpcClient) throws Exception {
        rpcClient.getOptions().setAction("urn:listLendBook");
        OMElement elemnt = rpcClient.invokeBlocking(new QName("http://servicelifecycle.sample/xsd",
                "listLendBook"), new Object[]{null});
        System.out.println(elemnt);
    }

}
