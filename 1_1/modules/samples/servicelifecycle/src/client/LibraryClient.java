package client;

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

public class LibraryClient {
    public static void main(String[] args) throws Exception {
        LibraryStub stub = new LibraryStub();
        LibraryClient client = new LibraryClient();
        client.listAllBook(stub);
        client.register("abc", "abc", stub);
        client.login("abc", "abc", stub);
        client.lendBook("1-56592-229-8", "abc", stub);
        client.listLendBook(stub);
        client.listAvailableBook(stub);
        client.returnBook("1-56592-229-8", stub);
        client.listLendBook(stub);
    }

    public void returnBook(String isbn, LibraryStub stub) throws Exception {
        LibraryStub.ReturnBook res = new LibraryStub.ReturnBook();
        LibraryStub.Book book = new LibraryStub.Book();
        book.setIsbn(isbn);
        book.setAuthor(null);
        book.setTitle(null);
        res.setBook(book);
        stub.returnBook(res);
    }

    public void lendBook(String isbn, String userName, LibraryStub stub) throws Exception {
        LibraryStub.LendBook lendBook = new LibraryStub.LendBook();
        lendBook.setIsbn(isbn);
        lendBook.setUserName(userName);
        LibraryStub.LendBookResponse res =
                stub.lendBook(lendBook);
        LibraryStub.Book book = res.get_return();
        System.out.println("Author" + book.getAuthor());
        System.out.println("Isbn" + book.getIsbn());
        System.out.println("Title" + book.getTitle());
        System.out.println("==================================");
    }

    public boolean register(String userName,
                            String passWord,
                            LibraryStub stub) throws Exception {
        LibraryStub.Register register = new LibraryStub.Register();
        register.setPassWord(passWord);
        register.setUserName(userName);
        LibraryStub.RegisterResponse res = stub.register(register);
        return res.get_return();
    }

    public boolean login(String userName,
                         String passWord,
                         LibraryStub stub) throws Exception {
        LibraryStub.Login login = new LibraryStub.Login();
        login.setPassWord(passWord);
        login.setUserName(userName);
        LibraryStub.LoginResponse res = stub.login(login);
        return res.get_return();
    }

    public void listAvailableBook(LibraryStub stub) throws Exception {
        LibraryStub.ListAvailableBookResponse res = stub.listAvailableBook();
        LibraryStub.Book [] books = res.get_return();
        for (int i = 0; i < books.length; i++) {
            LibraryStub.Book book = books[i];
            System.out.println("Author" + book.getAuthor());
            System.out.println("Isbn" + book.getIsbn());
            System.out.println("Title" + book.getTitle());
            System.out.println("==================================");
        }
    }

    public void listAllBook(LibraryStub stub) throws Exception {
        LibraryStub.ListAllBookResponse res = stub.listAllBook();
        LibraryStub.Book [] books = res.get_return();
        for (int i = 0; i < books.length; i++) {
            LibraryStub.Book book = books[i];
            System.out.println("Author" + book.getAuthor());
            System.out.println("Isbn" + book.getIsbn());
            System.out.println("Title" + book.getTitle());
            System.out.println("==================================");
        }
    }

    public void listLendBook(LibraryStub stub) throws Exception {
        LibraryStub.ListLendBookResponse res = stub.listLendBook();
        LibraryStub.Book [] books = res.get_return();
        for (int i = 0; i < books.length; i++) {
            LibraryStub.Book book = books[i];
            System.out.println("Author" + book.getAuthor());
            System.out.println("Isbn" + book.getIsbn());
            System.out.println("Title" + book.getTitle());
            System.out.println("==================================");
        }
    }

}
