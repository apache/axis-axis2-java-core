This module is the runtime portion of a basic XML<->Java databinding system
for Axis2.  Its features include:

* Metadata-based serialization and deserialization
* Automatic multi-ref handling for SOAP encoding (SOAP 1.1 / 1.2)
* RPC style message receiver

SERIALIZING

The SerializationContext (org.apache.axis2.databinding.SerializationContext) is
the main entry point class for serializing Java to XML.  A SerializationContext
is associated with a StAX XMLStreamWriter at construction time:

  SerializationContext sc = new SerializationContext(writer);

The most important externally-accessible method is "serializeElement(qname,
value, serializer)".  This method will use the passed Serializer to write
the passed value to the XML writer, using the passed QName as the element name.
A Serializer is a class implementing org.apache.axis2.databinding.Serializer,
whose responsibility is to write well-known kinds of data to a
SerializationContext/XMLStreamWriter.

In Axis 1.X, the SerializationContext would choose the Serializer to use
itself based on an active set of type mappings.  This worked great for many
cases, but as it turns out there are also context-dependent situations where
the type mapping isn't good enough - this is particularly common with certain
kinds of array/collection processing.  In Axis 2, therefore, each serialization
call allows a reference to the Serializer.

DESERIALIZATION:

As the SerializationContext is the main focus for serializing Java to XML, the
DeserializationContext is the focus for deserializing XML to Java.

  context.deserialize(xmlStreamReader, deserializer)

The first thing to note is that this method has no return value - so where
does the deserialized data go?  The deserialization system is based on the
idea of "targets" - these are "push based" objects whose job in life it is
to recieve deserialized values from Deserializers.  So once you set up the
appropriate target in a Deserializer, all you need to do is hand it to the
DeserializationContext.deserialize() method and your value will be put in
the appropriate place.

Still TODO:

* Nested collections/arrays
* SOAP arrayType processing
* Produce/handle xsi:types
* Holder implementation for inouts
* Associate TypeDesc with Java classes via static data
* The WSDL / Schema portion (translate schemas into generated classes
   with TypeDescs, and vice versa.  Also handle RPC style stubs/skels.)
* Tests!
* Finish this file :) (move a bunch of it to real documentation)

