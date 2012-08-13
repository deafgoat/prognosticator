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
This is a very simple example of training a model and then running against some test data. 

To run with ant type:

`ant example -Dclass=stock.AppleStockPricePredictor`

To run with java type:

`./example stock.AppleStockPricePredictor`

### Tomorrow's Weather Predictor
This predicts tomorrow's weather. 

To run with ant type:

`ant example -Dclass=weather.WeatherTomorrowPredictor`

To run with java type:

`./example weather.WeatherTomorrowPredictor`

## Configuration 
This following documentation describes the various configuration options that are available in the application.

*	`name*`	:	The name of the current configuration. For each different configuration should have a unique `name`.
*	`dumpARFF*`	:	The location of the entire dump (both training and test) if it exists. 
*	`data*`	:	Holds data-specific information:
	*	`dataFormat*` : The format to expect data attributes.
	*	`classValue*` : The class attribute to be predicted.
	*	`positiveClassValue*` : For nominal class attributes, the value of the 'positive' case.
	*	`negativeClassValue*` : For nominal class attributes, the value of the 'negative' case.
	*	`positiveClassWeight*` : For nominal class attributes, the weight of the 'positive' case.
	*	`negativeClassWeight*` : For nominal class attributes, the weight of the 'negative' case.
*	`errorAnalysis*` : Hold test outcome results for binary classification tasks.
 	*	`truePositives*`	:	Path to file to store true positives.
	*	`trueNegatives*`	:	Path to file to store true negatives.
 	*	`falsePositives*`	:	Path to file to store false positives.
 	*	`falseNegatives*`	:	Path to file to store false negatives.
*	`classifier*`	:	Holds all classifier related information:
	*	 `model*`	:	Holds model specific information:
		*	`classifier*`	:	The WEKA classifier to train 
		*	`outputModel*`	:	File path to store the trained model.
	*	`crossValidation*`	:	Holds cross-validation specific information:
		*	`numFolds*`	:	The number of folds to use in cross validation.
		*	`arguments`	:	Holds cross validation options.
		*	`seed`	:	Random seed to use in cross validation.
	*	`classify*`	:	Holds location of training and test arff file, if they exists. If they don't the application partitions (stratified for nominal class attributes) the `dump` data:
		*	`testARFF*`	: Path to the test ARFF file.
		*	`trainingARFF*`	:	Path to the training ARFF file.
	*	`prediction*`	:	Stores configuration option for test instance classifications:
		*	`maxCount*`	:	The maximum number of predictions to return (0 for all).
		*	`minProb*`	:	The minimum threshold for all model predictions.
		*	`file*`	:	Path to file to store the prediction.
		*	`onlyPosNominal*`	:	For nominal class values, indicates whether to include only `positiveClassValue` predictions.
	*	`writeToMongoDB*`	:	Indicates whether to write cross validation results/model to mongoDB. If set to `true`, ensure a mongod is running.
	*	`writeToFile*`	:	Indicates whether to write results/model to file.
*	`mongoDB`	:	Holds all mongoDB specific information. This must be present if `writeToMongoDB` is set to `true`.:
	*	`hostname`	:	Host name of mongod.
	*	`port`	: Mongod port.
	*	`database`	:	Name of database to write results to.
	*	`configCollection`	:	Name of collection that holds other configurations. Useful for large experiments.
	*	`modelCollection`	:	Name of collection that holds models and cross validation results.
	*	`predictionCollection`	:	Name of collection that holds the predictions made by the model.
*	`dump*`	:	Holds information pertaining to the structure and content of the imput dump data. 
	*	`file*`	:	Holds the location of the input data.
	*	`attributes*`	:	Holds all the attributes contained within the dump.
		*	`rawAttributeName*`	:	Holds the name of each attribute in the dump, *exactly* as it appears in `file`.
		*	`attributeName`	:	Holds the preferred name for the attribute.
		*	`attributeType*`	:	Holds the WEKA attribute type for this attribute ()nominal, numeric, string or date).
		*	`include*`	:	Flag indicating if this attribute should be used in training.
	
Options marked `*` are required.

## License

Copyright 2011, Wisdom Omuya.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.