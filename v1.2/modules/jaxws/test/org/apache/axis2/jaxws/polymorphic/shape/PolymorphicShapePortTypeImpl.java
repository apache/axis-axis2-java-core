package org.apache.axis2.jaxws.polymorphic.shape;

import javax.jws.WebService;

import org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapePortType;
import org.apache.axis2.jaxws.TestLogger;
import org.test.shape.Circle;
import org.test.shape.Shape;
import org.test.shape.Square;
import org.test.shape.threed.ThreeDSquare;

@WebService(endpointInterface="org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapePortType", wsdlLocation="test/org/apache/axis2/jaxws/polymorphic/shape/META-INF/shapes.wsdl")
public class PolymorphicShapePortTypeImpl implements PolymorphicShapePortType {

	public Shape draw(Shape request) {
		if(request instanceof Circle){
			Circle circle =(Circle) request;
            TestLogger.logger.debug("Drawing Circle on x =" + request.getXAxis() + " y=" +
                    request.getYAxis() + " With Radius =" + circle.getRadius());
			return request;
		}
		if(request instanceof Square){
			Square square =(Square) request;
            TestLogger.logger.debug("Drawing Square on x =" + request.getXAxis() + " y=" +
                    request.getYAxis() + " With Sides =" + square.getLength());
			return request;
		}
		return null;
	}

	public Shape draw3D(Shape request) {
		if(request instanceof ThreeDSquare){
			ThreeDSquare threeDsquare =(ThreeDSquare) request;
            TestLogger.logger.debug("Drawing 3DSquare on x =" + request.getXAxis() + " y=" +
                    request.getYAxis() + " With Bredth =" + threeDsquare.getBredth());
			return request;
		}
		return null;
	}

}
