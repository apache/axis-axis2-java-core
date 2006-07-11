This package will allow for independence of annotation collection techniques. There will be an 
interface, AnnotationCollector, that defines a method which will return an array of WSMService 
objects. These objects will be created by the collector after it has located all of the 
annotations in the relevant class files.