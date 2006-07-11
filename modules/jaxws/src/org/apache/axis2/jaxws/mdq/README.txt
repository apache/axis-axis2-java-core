This package will contain functionality to validate, process, and merge information provided 
by annotation metadata and wsdl. The idea will be that the MDQ works off of collections of 
WSMService objects as defined by the WSM project. Users will need to supply their own
functionality that converts varying data into WSMService objects. For instance, users may 
need a WSDL to WSMSerivce converter. Ideally, the MDQ will have knowledge of a single data 
structure which will be a WSMService.