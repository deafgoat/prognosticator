# Prognosticator
## Overview
A Java application that enables users to build machine learning models capable of making predictions on new data. The application uses WEKA algorithms for prediction tasks and includes several data pre-processing tools. Various configuration options allow the user to tailor the models, experiments, and predictions accordingly.


## Instructions
The following are the various ant commands used in development/testing.

* ant compile - compile all the Java code
* ant test - run the integration tests
* ant unit - run the unit tests
* ant javadocs - generate the javadocs
* ant jar - generate the jar file

## Examples

### Apple Stock Price Predictor
This is a very simple example of training a model and then running against some test data. To run type:

`ant example -Dclass=stock.AppleStockPricePredictor`

### Tomorrow Weather Predictor
This predicts tomorrow's weather. To run type:

`ant example -Dclass=weather.WeatherTomorrowPredictor`

