package sample.pojo.rpcclient;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import sample.pojo.data.Weather;


public class WeatherRPCClient {

    public static void main(String[] args1) throws AxisFault {

        RPCServiceClient serviceClient = new RPCServiceClient();

        Options options = serviceClient.getOptions();

        EndpointReference targetEPR = new EndpointReference("http://localhost:8080/axis2/services/WeatherService");

        options.setTo(targetEPR);

        // Setting the weather
        QName opSetWeather = new QName("http://service.pojo.sample/xsd", "setWeather");

        Weather w = new Weather();

        w.setTemperature((float)39.3);
        w.setForecast("Cloudy with showers");
        w.setRain(true);
        w.setHowMuchRain((float)4.5);

        Object[] opSetWeatherArgs = new Object[] { w };

        serviceClient.invokeRobust(opSetWeather, opSetWeatherArgs);


        // Getting the weather
        QName opGetWeather = new QName("http://service.pojo.sample/xsd", "getWeather");

        Object[] opGetWeatherArgs = new Object[] { };
        Class[] returnTypes = new Class[] { Weather.class };
        
        
        Object[] response = serviceClient.invokeBlocking(opGetWeather,
                opGetWeatherArgs, returnTypes);
        
        Weather result = (Weather) response[0];
        
        if (result == null) {
            System.out.println("Weather didn't initialize!");
            return;
        }
        
        // Displaying the result
        System.out.println("Temperature               : " +
                           result.getTemperature());
        System.out.println("Forecast                  : " +
                           result.getForecast());
        System.out.println("Rain                      : " +
                           result.getRain());
        System.out.println("How much rain (in inches) : " +
                           result.getHowMuchRain());
        
    }
}
