The xmlhttp tests exercise the JAX-WS XML/HTTP binding.

Structure:

org.apache.axis2.jaxws.xmlhttp.
                               Dispatch*Tests.java   (Dispatch clients: payload String, Source
                                                      and JAXB; message String, Source and
                                                      DataSource)
                               provider.
                                        payload.
                                                string.     XPayloadStringProvider
                                                source.     XPayloadSourceProvider
                                        message.
                                                string.     XMessageStringProvider
                                                source.     XMessageSourceProvider
                                                datasource. XMessageDataSourceProvider
