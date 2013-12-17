/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package samples.databinding;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.UnmarshalHandler;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import samples.databinding.data.GetStockQuote;
import samples.databinding.data.GetStockQuoteResponse;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

public final class StockClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: StockClient <url> <symbol>");
            return;
        }
        final String url = args[0];
        final String symbol = args[1];

        System.out.println();
        System.out.println("Getting Stock Quote for " + symbol);

        StockQuoteServiceStub stub =
                new StockQuoteServiceStub(url);
        stub._getServiceClient().getOptions().setAction("getStockQuote");
        GetStockQuote stockQuote = new GetStockQuote();
        stockQuote.setSymbol(symbol);
        OMDocument document = OMAbstractFactory.getOMFactory().createOMDocument();
        Marshaller.marshal(stockQuote, document.getSAXResult().getHandler());
        OMElement response = stub.getStockQuote(
                document.getOMDocumentElement());


        Unmarshaller unmarshaller = new Unmarshaller(GetStockQuoteResponse.class);
        UnmarshalHandler unmarshalHandler = unmarshaller.createHandler();
        GetStockQuoteResponse stockQuoteResponse;
        try {
            ContentHandler contentHandler = Unmarshaller.getContentHandler(unmarshalHandler);
            TransformerFactory.newInstance().newTransformer().transform(response.getSAXSource(false), new SAXResult(contentHandler));
            stockQuoteResponse = (GetStockQuoteResponse) unmarshalHandler.getObject();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Price = " + stockQuoteResponse.getQuote().getLastTrade().getPrice());
    }
}
