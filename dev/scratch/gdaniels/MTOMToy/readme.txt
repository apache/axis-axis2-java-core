This is a quickie example just to show a design pattern
with which Object/MTOM functionality might be implemented.

The OMElement only knows how to write itself to an OMWriter,
and if it's got Object content it asks the OMWriter to
serialize it once it's written the start tag (realistically
we'd need to deal with attributes as well, so we'd
probably leave the start tag open).  The BasicWriter
notices if content is a DataHandler, and calls writeBinary()
if so.  MTOMWriter extends BasicWriter in order to add
(trivial) MTOM-like packaging.

