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

// Java
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Apache IO/Logger
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

// JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility to read from JSON configuration file or directly from a JSONObject.
 * Parses the JSON for the app's config
 */
public final class ConfigReader {

	/**
	 * Reads the configuration file
	 * @throws IOException If configuration file can not be found
	 * @throws JSONException If configuration file can not be parsed
	 */
	public void readConfig() throws IOException, JSONException {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Reading application configuration");
		}
		// Read classification configurations
		readGlobalConfig();
		// Set input/output file parameters
		_dumpARFF = _configJSON.getString("dumpARFF");
		// Set data configurations
		JSONObject _data = _configJSON.getJSONObject("data");
		_classValue = _data.getString("classValue");
		_dateFormat = _data.getString("dateFormat");
		_positiveClassValue = _data.getString("positiveClassValue");
		_negativeClassValue = _data.getString("negativeClassValue");
		_positiveClassWeight = Integer.parseInt(_data.getString("positiveClassWeight"));
		_negativeClassWeight = Integer.parseInt(_data.getString("negativeClassWeight"));
		Attributes attribute = null;
		JSONObject rec = null;
		// Read in top-level table dump configuration
		JSONObject data = _configJSON.getJSONObject("dump");
		_dumpFile = data.getString("file");
		_attributes = new HashMap<String, ArrayList<Attributes>>();
		_attributes.put(_dumpFile, new ArrayList<Attributes>());
		JSONArray array = data.getJSONArray("attributes");
		for (int i = 0; i < array.length(); i++) {
			rec = array.getJSONObject(i);
			attribute = Attributes.createAttribute(rec.getString("rawAttributeName"), rec.getString("attributeName"), rec.getString("attributeType"), rec.getBoolean("include")).getObject();
			_attributes.get(_dumpFile).add(attribute);
			if (_logger.isDebugEnabled()) {
				_logger.debug("Read attribute " + attribute.getRawAttributeName());
			}
		}
		if (_logger.isDebugEnabled()) {
			_logger.debug("Read " + _attributes.get(_dumpFile).size() + " attribute(s) for " + _dumpFile);
		}
	}

	/**
	 * Reads classifier-pertinent information
	 * @throws IOException If configuration file can not be found
	 * @throws JSONException If configuration file can not be parsed
	 */
	private void readGlobalConfig() throws IOException, JSONException {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Reading classifier configuration");
		}
		JSONObject classifier = _configJSON.getJSONObject("classifier");
		JSONObject prediction = classifier.getJSONObject("prediction");
		JSONObject errorAnalysis = _configJSON.getJSONObject("errorAnalysis");
		JSONObject crossValidation = classifier.getJSONObject("crossValidation");
		JSONObject classify = classifier.getJSONObject("classify");
		JSONObject model = classifier.getJSONObject("model");
		_relation = _configJSON.getString("name");
		_testARFF = classify.getString("testARFF");
		_modelFile = model.getString("outputModel");
		_classifier = model.getString("classifier");
		_dumpARFF = _configJSON.getString("dumpARFF");
		_folds = crossValidation.getString("numFolds");
		_trainingARFF = classify.getString("trainingARFF");
		_writeToFile = classifier.getBoolean("writeToFile");
		_writeToMongoDB = classifier.getBoolean("writeToMongoDB");
		_truePositives = errorAnalysis.getString("truePositives");
		_trueNegatives = errorAnalysis.getString("trueNegatives");
		_falsePositives = errorAnalysis.getString("falsePositives");
		_falseNegatives = errorAnalysis.getString("falseNegatives");
		_maxCount = prediction.getInt("maxCount");
		
		if (_writeToMongoDB) {
			JSONObject mongoDB = _configJSON.getJSONObject("mongoDB");
			_port = mongoDB.getInt("port");
			_db = mongoDB.getString("database");
			_host = mongoDB.getString("hostname");
			_modelCollection = mongoDB.getString("modelCollection");
			_configCollection = mongoDB.getString("configCollection");
			_predictionCollection = mongoDB.getString("predictionCollection");
		}
		
		_predictionFile = prediction.getString("file");
		if (_maxCount == 0) {
			_maxCount = Integer.MAX_VALUE;
		}
		_minProb = Double.parseDouble(prediction.getString("minProb"));
		_onlyPosNominal = prediction.getBoolean("onlyPosNominal");
		JSONObject _data = _configJSON.getJSONObject("data");
		_classValue = _data.getString("classValue");
	}

	/**
	 * Sets the attribute hash to allow for efficient retrieval
	 */
	public void setAttributeMap() {
		_attributeMap = new HashMap<String, HashMap<String, Integer>>();
		for (String attributeDump : _attributes.keySet()) {
			_attributeMap.put(attributeDump, new HashMap<String, Integer>());
			for (int i = 0; i < _attributes.get(attributeDump).size(); i++) {
				_attributeMap.get(attributeDump).put(_attributes.get(attributeDump).get(i).getAttributeName().trim().toLowerCase(), i);
			}
		}
	}

	/**
	 * contains a mapping of all dumps to their name/location
	 */
	public Map<String, HashMap<String, Integer>> _attributeMap;

	/**
	 * the set of all attributes specified in configuration
	 */
	public Map<String, ArrayList<Attributes>> _attributes;

	/**
	 * the name of the classification algorithm to use
	 */
	public String _classifier;

	/**
	 * the attribute to predict
	 */
	public String _classValue;

	/**
	 * the application configuration collection
	 */
	public String _configCollection;

	/**
	 * the name of the configuration file
	 */
	public String _configFile;

	/**
	 * an object to hold the read in JSON configuration
	 */
	public JSONObject _configJSON;

	/**
	 * the input data date format
	 */
	public String _dateFormat;

	/**
	 * the application database
	 */
	public String _db;

	/**
	 * where to save the converted CSV
	 */
	public String _dumpARFF;

	/**
	 * the name of the file containing the CSV dump
	 */
	public String _dumpFile;

	/**
	 * where to store predictions for false negatives
	 */
	public String _falseNegatives;

	/**
	 * where to store predictions for false positives
	 */
	public String _falsePositives;

	/**
	 * number of folds for cross-validation
	 */
	public String _folds;

	/**
	 * the database hostname
	 */
	public String _host;

	/**
	 * a handle to the logging object
	 */
	private Logger _logger;

	/**
	 * maximum number of predictions to output
	 */
	public int _maxCount;

	/**
	 * minimum confidence threshold for predictions
	 */
	public double _minProb;

	/**
	 * the application experiment models/results collection
	 */
	public String _modelCollection;

	/**
	 * where to save the model file
	 */
	public String _modelFile;

	/**
	 * the value of the negative class
	 */
	public String _negativeClassValue;

	/**
	 * the weight associated with the negative class(es)
	 */
	public int _negativeClassWeight;

	/**
	 * flag indicating whether to write only positive nominal class
	 */
	public boolean _onlyPosNominal;
	
	/**
	 * the application database port
	 */
	public int _port;

	/**
	 * the value of the positive class
	 */
	public String _positiveClassValue;

	/**
	 * the weight associated with the positive class
	 */
	public int _positiveClassWeight;

	/**
	 * the application prediction collection
	 */
	public String _predictionCollection;

	/**
	 * where to write predictions to
	 */
	public String _predictionFile;

	/**
	 * name of the relationship
	 */
	public String _relation;

	/**
	 * where to save the test portion of dump ARFF
	 */
	public String _testARFF;

	/**
	 * where to save the training portion of dump ARFF
	 */
	public String _trainingARFF;

	/**
	 * where to store predictions for true negatives
	 */
	public String _trueNegatives;

	/**
	 * where to store predictions for true positives
	 */
	public String _truePositives;

	/**
	 * indicates if application should write model/results to file
	 */
	public boolean _writeToFile;

	/**
	 * indicates if application should write model/results to mongodb
	 */
	public boolean _writeToMongoDB;
	
	static final ClassLoader loader = ConfigReader.class.getClassLoader();

	/**
	 * Constructor for MongoDB configuration JSON
	 * @param json The JSON configuration object
	 */
	public ConfigReader(JSONObject json) {
		_logger = Logger.getLogger(AppLogger.class.getName());
		_configJSON = json;
	}

	/**
	 * Constructor for configuration file
	 * @param configFile The configuration file
	 * @throws IOException If configuration file can not be found
	 * @throws JSONException If configuration file can not be parsed
	 */
	public ConfigReader(String configFile) throws IOException, JSONException {
		_logger = AppLogger.getLogger();
		_configFile = configFile;
		//	_configJSON = new JSONObject(loader.getResource(_configFile).toString());
		_configJSON = new JSONObject(IOUtils.toString(new FileReader(_configFile)));
	}

}
