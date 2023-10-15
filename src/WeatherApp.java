import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApp {

    private static final String API_KEY = "eaG8Gm9kjgMNc07Ae8YD24yx04par1D1";
    private static final String SEARCH_URL = "http://dataservice.accuweather.com/locations/v1/cities/search?apikey=" + API_KEY + "&q=";
    private static final String CURRENT_WEATHER_URL = "http://dataservice.accuweather.com/currentconditions/v1/";
    private static final String FORECAST_5DAY_URL = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/";
    private static final String FORECAST_12HOUR_URL = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/";
    private static final String FORECAST_24HOUR_URL = "http://dataservice.accuweather.com/forecasts/v1/hourly/24hour/";
    private static final String FORECAST_15DAY_URL = "http://dataservice.accuweather.com/forecasts/v1/daily/15day/";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JTextField cityField = new JTextField("Enter city name");
        JButton searchButton = new JButton("Search");
        JTextArea resultArea = new JTextArea();
        JComboBox<String> optionsComboBox = new JComboBox<>(new String[]{"Current Weather", "5-Day Forecast", "12-Hour Forecast", "15-Day Forecast", "24-Hour Forecast"});

        frame.add(cityField, BorderLayout.NORTH);
        frame.add(searchButton, BorderLayout.CENTER);
        frame.add(resultArea, BorderLayout.SOUTH);
        frame.add(optionsComboBox, BorderLayout.WEST);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cityName = cityField.getText();
                String cityKey = searchCity(cityName);
                if (cityKey == null) {
                    resultArea.setText("City not found or error occurred.");
                    return;
                }

                String selectedOption = (String) optionsComboBox.getSelectedItem();
                String result;

                switch (selectedOption) {
                    case "5-Day Forecast":
                        result = fetch5DayForecast(cityKey);
                        break;
                    case "12-Hour Forecast":
                        result = fetch12HourForecast(cityKey);
                        break;
                    case "15-Day Forecast":
                        result = fetch15DayForecast(cityKey);
                        break;
                    case "24-Hour Forecast":
                        result = fetch24HourForecast(cityKey);
                        break;
                    default:
                        result = fetchWeather(cityKey);
                        break;
                }

                resultArea.setText(result);
            }
        });

        frame.setVisible(true);
    }

    private static String searchCity(String cityName) {
        try {
            URL url = new URL(SEARCH_URL + cityName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            JSONArray jsonArray = new JSONArray(response);
            if (jsonArray.length() == 0) return null;

            JSONObject cityObj = jsonArray.getJSONObject(0);
            return cityObj.getString("Key");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String fetchWeather(String cityKey) {
        try {
            URL url = new URL(CURRENT_WEATHER_URL + cityKey + "?apikey=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            JSONArray jsonArray = new JSONArray(response);
            if (jsonArray.length() == 0) return "No weather info available.";

            JSONObject weatherObj = jsonArray.getJSONObject(0);
            String weatherText = weatherObj.getString("WeatherText");
            double temperature = weatherObj.getJSONObject("Temperature").getJSONObject("Metric").getDouble("Value");

            return String.format("Weather: %s\nTemperature: %.2fF", weatherText, temperature);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching current weather";
        }
    }

    private static String fetch5DayForecast(String cityKey) {
        try {
            URL url = new URL(FORECAST_5DAY_URL + cityKey + "?apikey=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            JSONObject jsonObject = new JSONObject(response);
            JSONArray dailyForecasts = jsonObject.getJSONArray("DailyForecasts");
            StringBuilder forecastBuilder = new StringBuilder();

            for (int i = 0; i < dailyForecasts.length(); i++) {
                JSONObject forecast = dailyForecasts.getJSONObject(i);
                String date = forecast.getString("Date");
                JSONObject temperature = forecast.getJSONObject("Temperature");
                double min = temperature.getJSONObject("Minimum").getDouble("Value");
                double max = temperature.getJSONObject("Maximum").getDouble("Value");

                forecastBuilder.append(String.format("Date: %s, Min: %.2fF, Max: %.2fF\n", date, min, max));
            }

            return forecastBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching 5-day forecast";
        }
    }

    private static String fetch12HourForecast(String cityKey) {
        try {
            URL url = new URL(FORECAST_12HOUR_URL + cityKey + "?apikey=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            JSONArray jsonArray = new JSONArray(response);
            StringBuilder forecastBuilder = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject forecast = jsonArray.getJSONObject(i);
                String time = forecast.getString("DateTime");
                String iconPhrase = forecast.getString("IconPhrase");
                forecastBuilder.append(String.format("Time: %s, Weather: %s\n", time, iconPhrase));
            }

            return forecastBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching 12-hour forecast";
        }
    }

    private static String fetch15DayForecast(String cityKey) {
        try {
            URL url = new URL(FORECAST_15DAY_URL + cityKey + "?apikey=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            JSONObject jsonObject = new JSONObject(response);
            JSONArray dailyForecasts = jsonObject.getJSONArray("DailyForecasts");
            StringBuilder forecastBuilder = new StringBuilder();

            for (int i = 0; i < dailyForecasts.length(); i++) {
                JSONObject forecast = dailyForecasts.getJSONObject(i);
                String date = forecast.getString("Date");
                JSONObject temperature = forecast.getJSONObject("Temperature");
                double min = temperature.getJSONObject("Minimum").getDouble("Value");
                double max = temperature.getJSONObject("Maximum").getDouble("Value");

                forecastBuilder.append(String.format("Date: %s, Min: %.2fF, Max: %.2fF\n", date, min, max));
            }

            return forecastBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching 15-day forecast";
        }
    }

    private static String fetch24HourForecast(String cityKey) {
        try {
            URL url = new URL(FORECAST_24HOUR_URL + cityKey + "?apikey=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            JSONArray jsonArray = new JSONArray(response);
            StringBuilder forecastBuilder = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject forecast = jsonArray.getJSONObject(i);
                String time = forecast.getString("DateTime");
                String iconPhrase = forecast.getString("IconPhrase");
                forecastBuilder.append(String.format("Time: %s, Weather: %s\n", time, iconPhrase));
            }

            return forecastBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching 24-hour forecast";
        }
    }

}
