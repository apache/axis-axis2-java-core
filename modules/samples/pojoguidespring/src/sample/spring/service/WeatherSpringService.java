package sample.spring.service;

import sample.spring.bean.Weather;

public class WeatherSpringService{
    Weather weather;
    
    public void setWeather(Weather w){
        weather = w;
    }

    public Weather getWeather(){
        return weather;
    }
}