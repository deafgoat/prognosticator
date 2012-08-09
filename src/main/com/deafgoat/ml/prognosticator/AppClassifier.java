/**
 * Copyright 2012, Wisdom Omuya.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deafgoat.ml.prognosticator;

//Java
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

//Log4j
import org.apache.log4j.Logger;

//Weka
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Builds and cross validates models, classifies test instances.
 */
public final class AppClassifier {
	/**
	 * Perform cross-validation on data set/builds model
	 * @throws Exception
	 */
	public void crossValidate() throws Exception {
		// stratify nominal target class
		if (_trainInstances.classAttribute().isNominal()) {
			_trainInstances.stratify(_folds);
		}
		_eval = new Evaluation(_trainInstances);
		for (int n = 0; n < _folds; n++) {
			
			if (_logger.isDebugEnabled()) {
				_logger.debug("Cross validation fold: " + (n + 1));
			}
			_train = _trainInstances.trainCV(_folds, n);
			_test = _trainInstances.testCV(_folds, n);
			_clsCopy = AbstractClassifier.makeCopy(_cls);
			try {
				_clsCopy.buildClassifier(_train);
			} catch (Exception e) {
				_logger.debug(_config._classifier + " can not handle " + getAttributeType(_test.classAttribute()) + " class attributes");
			}	
			
			try {
				_eval.evaluateModel(_clsCopy, _test);
			} catch (Exception e) {
				_logger.debug("Can not evaluate model");
			}
		}
		
		if (_config._writeToMongoDB) {
			_logger.info("Writing model to mongoDB");
			// save the trained model
			saveModel();
			// save CV performance of trained model
			writeToMongoDB(_eval);
		}

		if (_config._writeToFile) {
			_logger.info("Writing model to file");
			SerializationHelper.write(_config._modelFile, _clsCopy);
		}
	}

	/**
	 * Gets details on classified instances according to supplied attribute
	 * @param attribute The focal attribute for error analysis
	 * @throws Exception If model can not be evaluated
	 */
	public void errorAnalysis(String attribute) throws Exception {
		readModel();
		_logger.info("Performing error analysis");
		Evaluation eval = new Evaluation(_testInstances);
		eval.evaluateModel(_cls, _testInstances);
		_predictionList = new HashMap<String, List<Prediction>>();
		String predicted, actual = null;
		double[] distribution = null;
		_predictionList.put(_config._truePositives, new ArrayList<Prediction>());
		_predictionList.put(_config._trueNegatives, new ArrayList<Prediction>());
		_predictionList.put(_config._falsePositives, new ArrayList<Prediction>());
		_predictionList.put(_config._falseNegatives, new ArrayList<Prediction>());
		for (int i = 0; i < _testInstances.numInstances(); i++) {
			distribution = _cls.distributionForInstance(_testInstances.instance(i));
			actual = _testInstances.classAttribute().value((int) _testInstances.instance(i).classValue());
			predicted = _testInstances.classAttribute().value((int) _cls.classifyInstance(_testInstances.instance(i)));
			// 0 is negative, 1 is positive
			if (!predicted.equals(actual)) {
				if (actual.equals(_config._negativeClassValue)) {
					_predictionList.get(_config._falsePositives).add(new Prediction(i + 1, predicted, distribution, _fullData.instance(i)));
				} else if (actual.equals(_config._positiveClassValue)) {
					_predictionList.get(_config._falseNegatives).add(new Prediction(i + 1, predicted, distribution, _fullData.instance(i)));
				}
			} else if (predicted.equals(actual)) {
				if (actual.equals(_config._negativeClassValue)) {
					_predictionList.get(_config._trueNegatives).add(new Prediction(i + 1, predicted, distribution, _fullData.instance(i)));
				} else if (actual.equals(_config._positiveClassValue)) {
					_predictionList.get(_config._truePositives).add(new Prediction(i + 1, predicted, distribution, _fullData.instance(i)));
				}
			}
		}
		BufferedWriter writer = null;
		String name, prediction = null;
		for (Entry<String, List<Prediction>> entry : _predictionList.entrySet()) {
			name = entry.getKey();
			Collections.sort(_predictionList.get(name), Collections.reverseOrder());
			writer = new BufferedWriter(new FileWriter(name));
			List<Prediction> predictions = _predictionList.get(name);
			for (int count = 0; count < predictions.size(); count++) {
				if (count < _config._maxCount) {
					prediction = predictions.get(count).attributeDistribution(attribute);
					if (Double.parseDouble(prediction.split(_delimeter)[1]) >= _config._minProb) {
						writer.write(prediction + "\n");
					}
				} else {
					break;
				}
			}
			writer.close();
		}
	}

	/**
	 * Evaluates model performance on test instances
	 * @throws Exception If model can not be evaluated.
	 */
	public void evaluate() throws Exception {
		readModel();
		_logger.info("Classifying with " + _config._classifier);
		Evaluation eval = new Evaluation(_testInstances);
		eval.evaluateModel(_cls, _testInstances);
		_logger.info("\n" + eval.toSummaryString());
		try {
			_logger.info("\n" + eval.toClassDetailsString());
		} catch (Exception e) {
			_logger.info("Can not create class details" + _config._classifier);
		}
		try {
			_logger.info("\n" + _eval.toMatrixString());
		} catch (Exception e) {
			_logger.info("Can not create confusion matrix for " + _config._classifier + " using " + _config._classValue);
		}
	}
	
	/**
	 * Returns the Weka type of the given attribute
	 */
	public String getAttributeType(Attribute attribute) {
		if (attribute.isDate()) {
			return "date";
		} else if (attribute.isNominal()) {
			return "nominal";
		} else if (attribute.isNumeric()) {
			return "numeric";
		} else {
			return "string";
		}
	}
		
	/**
	 * Initialize instances classifier.
	 * @throws Exception If the classifier can not be initialized.
	 */
	public void initializeClassifier() throws Exception {
		String base = "weka.classifiers.";
		String[] groups = new String[] { "bayes.", "functions.", "lazy.", "meta.", "misc.", "rules.", "trees." };
		for (int i = 0; i < groups.length; i++) {
			try {
				_cls = AbstractClassifier.forName(base + groups[i] + _config._classifier, null);
				break;
			} catch (Exception e) {
				if (i == groups.length - 1) {
					_logger.error("Could not create classifier - msg: " + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Does prediction in production
	 * @throws Exception If model can not be evaluated
	 */
	public void predict() throws Exception {
		_logger.info("Predicting test instances");
		readModel();
		String predicted = null;
		Prediction prediction = null;
		double[] distribution = null;
		HashMap<String, String> result = null;
		MongoResult mongoResult = null;
		ArrayList<Prediction> predictionList = new ArrayList<Prediction>();
		ArrayList<HashMap<String, String>> predictions = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i < _testInstances.numInstances(); i++) {	
			try {
				distribution = _cls.distributionForInstance(_testInstances.instance(i));
				predicted = _testInstances.classAttribute().value((int) _cls.classifyInstance(_testInstances.instance(i)));
				prediction = new Prediction(i + 1, predicted, distribution, _fullData.instance(i));
		
				if (_testInstances.classAttribute().isNominal() && _config._onlyPosNominal) {
					// write only 'positive' predictions to file
					if (predicted.equals(_config._positiveClassValue)) {
						predictionList.add(prediction);
					}
				} else {
					predictionList.add(prediction);
				}	
			} catch (Exception e) {
				_logger.debug(_config._classifier + " does not provide instance prediction distribution");
			}
			
			// writing ALL predictions to database
			if (_config._writeToMongoDB) {
				result = new HashMap<String, String>();
				if (_testInstances.classAttribute().isNumeric()) {
					result.put("confidence", "");
				} else {
					result.put("confidence", prediction.getConfidence().toString());
				}
				result.put(_config._classValue, prediction.getPrediction());
				predictions.add(result);
			}
		}
		
		if (_config._writeToFile) {
			_logger.info("Writing predictions to file");
			// sort prediction list
			try {
				Collections.sort(predictionList);
			} catch (Exception e) {
				_logger.debug("Can not use prediction compareTo");
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(_config._predictionFile));
			String value = null;
			double confidence = 0.0;
			int count = 0;
			int index = 0;
			for (Prediction entry : predictionList) {
				confidence = entry.getConfidence();
				value = entry.getPrediction();
				index = entry.getIndex();
				if (count < _config._maxCount) {
					if (confidence >= _config._minProb) {
						if (_testInstances.classAttribute().isNumeric()) {
							writer.write(index + _delimeter + value + "\n");
						} else {
							writer.write(index + _delimeter + confidence + _delimeter + value + "\n");
						}
						count += 1;
					}
				} else {
					break;
				}
			}
			writer.close();
		}
		
		if (_config._writeToMongoDB) {
			_logger.info("Writing predictions to mongoDB");
			mongoResult = new MongoResult(_config._host, _config._port, _config._db, _config._predictionCollection);
			mongoResult.writeResult(_config._relation, predictions);
		}
	}

	/**
	 * Output cross-validation results
	 * @throws Exception If the confusion matrix can not be shown
	 */
	public void printSummary() throws Exception {
		_logger.info(_eval.toSummaryString("\n" + _folds + "-fold Cross-validation\n", false));
		try {
			_logger.info("\n" + _eval.toMatrixString());
		} catch (Exception e) {
			_logger.info("Can not create confusion matrix for " + _config._classifier + " using " + _config._classValue);
		}
	}

	/**
	 * Reads the trained model
	 * @throws Exception If the model can not be read.
	 */
	public void readModel() throws Exception {
        if (_logger.isDebugEnabled()) {
		    _logger.debug("Deserializing model");
        }

		if (_config._writeToMongoDB) {
			MongoResult mongoResult = new MongoResult(_config._host, _config._port, _config._db, _config._modelCollection);
			_cls = mongoResult.readModel(_config._relation);
			mongoResult.close();
		}
		
		if (_config._writeToFile) {
			_cls = (Classifier) SerializationHelper.read(_config._modelFile);
		}
	}

	/**
	 * Saves the trained model
	 * @throws Exception If the model can not be saved
	 */
	public void saveModel() throws Exception {
        if (_logger.isDebugEnabled()) {
		    _logger.debug("Serializing model");
        }

		if (_config._writeToMongoDB) {
			MongoResult mongoResult = new MongoResult(_config._host, _config._port, _config._db, _config._modelCollection);
			mongoResult.writeModel(_config._relation, _clsCopy);
			mongoResult.close();
		}

		if (_config._writeToFile) {
			SerializationHelper.write(_config._modelFile, _clsCopy);
		}
	}

	/**
	 * Write results to mongoDB
	 * @param eval The evaluation object holding data.
	 * @throws Exception 
	 */
	public void writeToMongoDB(Evaluation eval) throws Exception {
		MongoResult mongoResult = new MongoResult(_config._host, _config._port, _config._db, _config._modelCollection);
		mongoResult.writeExperiment(_config._relation, "summary", eval.toSummaryString());
		try {
			mongoResult.writeExperiment(_config._relation, "class detail", eval.toClassDetailsString());
		} catch (Exception e) {
			_logger.error("Can not create class details" + _config._classifier);
		}
		try {
			mongoResult.writeExperiment(_config._relation, "confusion matrix", eval.toMatrixString());
		} catch (Exception e) {
			_logger.error("Can not create confusion matrix for " + _config._classifier);
		}
		mongoResult.close();
	}

	/**
     * handle to classifier object
     */
	private Classifier _cls;

	/**
     * handle to a copy of the classifier object
     */
	private Classifier _clsCopy;

	/**
     * configuration handle
     */
	private ConfigReader _config;

	/**
	 * the delimeter to use in the prediction file
	 */
	private static final String	_delimeter	= "\t";
	
	/**
     * handle AbstractClassifier the evaluation object
     */
	private Evaluation _eval;

	/**
     * number of folds to use in cross-validation
     */
	private int _folds;

	/**
     * the full set of unfiltered data
     */
	private Instances _fullData;

	/**
     * handle to the logger
     */
	private Logger _logger;

	/**
     * contains all predictions made on test data
     */
	private HashMap<String, List<Prediction>> _predictionList;

	/**
     * holds test data (used in CV)
     */
	private Instances _test;

	/**
     * holds initialized test data
     */
	private Instances _testInstances;

	/**
     * holds training data (used in CV)
     */
	private Instances _train;

	/**
     * holds initialized training data
     */
	private Instances _trainInstances;
	

	/**
	 * Constructor for classifying a given set of unfiltered test instances
	 * @param fullData The full data set.
	 * @param config The config reader handle.
	 * @throws Exception
	 */
	public AppClassifier(Instances fullData, ConfigReader config) throws Exception {
		_config = config;
		_testInstances = fullData;
		_fullData = fullData;
		_logger = AppLogger.getLogger();
	}

	/**
	 * Constructor for classifying a given set of test instances which have been
	 * filtered
	 * @param filteredData The filtered data set.
	 * @param fullData The full data set.
	 * @param config The config reader handle.
	 * @throws Exception
	 */
	public AppClassifier(Instances filteredData, Instances fullData, ConfigReader config) throws Exception {
		_config = config;
		_testInstances = filteredData;
		_fullData = fullData;
		_logger = AppLogger.getLogger();
	}

	/**
	 * Constructor to build model based on CV
	 * @param trainData The training data.
	 * @param fold The number of folds for corss validation.
	 * @param config The config reader handle.
	 * @throws Exception
	 */
	public AppClassifier(Instances trainData, int fold, ConfigReader config) throws Exception {
		_folds = fold;
		_config = config;
		_trainInstances = new Instances(trainData);
		_logger = AppLogger.getLogger();
	}
}

