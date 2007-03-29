package sample.pojo.service;

import sample.pojo.data.Weather;

public class WeatherService{
    Weather weather;
    
    public void setWeather(Weather weather){
        this.weather = weather;
    }

    public Weather getWeather(){
        return this.weather;
    }
}