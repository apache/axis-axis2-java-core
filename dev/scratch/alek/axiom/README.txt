This is reviewed version of existing OM APIs modified 
to make it more lightweight in particular to allow String 
to be a direct child of OMElement and to have as many 
as possible immutable objects.

The biggest difference is that OMNode is removed and there is 
no one superclass for all XML Information Items.
This allows to store String directly as child of OMElement.

Iterable was used instead of Iterator to ease future transition 
to JDK5 and to support foreach.

When DOM API wrapper is done over OM API it will have to use 
Node super interface but this should be deferred defer to situations 
only when DOM API is *requested* by some handlers and may be possible
to contain it only for some XML sub-tree. There may be still some 
more changes depending on how much of DOM API we are required to implement.

The issue that is not clear to me is how OM API can be used to 
access event stream for SOAP:Body content 
(and avoid building OM elements for its content) 
- how isComplete() method should be used in this context?
