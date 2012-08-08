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
import java.io.File;
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

// Weka
import weka.core.Instances;

/**
 * Main working class to run experiments.
 */
public final class Experimenter {

	/**
	 * Builds the application model
	 * @throws Exception If the model can not be created
	 */
	public void buildModel() throws Exception {
		_logger.info("Building model");
		// creates ARFF file if not found
		convertDump();
		// read in train data for cross validation
		readARFF("train");
		// cross validate and build model
		crossValidate();
	}

	/**
	 * Creates training/test data if not found
	 * @param mode Indicatesif ARFF to be read is training (train) or testing (test) data
	 * @throws Exception If data can not be split
	 */
	public void checkExists(String mode) throws Exception {
		File f;
		if (mode.equals("test")) {
			f = new File(_config._testARFF);
			// check if we have to test data file
			if (!f.exists()) {
				readARFF("dump");
				splitData(_dumpSet);
			}
		} else if (mode.equals("train")) {
			f = new File(_config._trainingARFF);
			// check if we have to test data file
			if (!f.exists()) {
				readARFF("dump");
				splitData(_dumpSet);
			}
		} else if (mode.equals("dump")) {
			convertDump();
		}
	}

	/**
	 * Converts dump CSV to ARFF file
	 * @throws Exception If ARFF data can not be created
	 */
	public void convertDump() throws Exception {
		File f = new File(_config._dumpARFF);
		if (!f.exists()) {
			// create dump ARFF if it doesn't exist
			createARFF();
		}
	}

	/**
	 * Create the ARFF file from dump
	 * @throws Exception If ARFF writer can not be initialized
	 */
	public void createARFF() throws Exception {
		_logger.info("Creating dump ARFF");
		ARFFWriter dump = new ARFFWriter(_config);
		dump.writeARFF();
	}

	/**
	 * Carries out cross-validation and prints summary
	 * @throws Exception If cross validation can not be performed
	 */
	public void crossValidate() throws Exception {
		_appClassifier = new AppClassifier(filterData(_trainSet), _folds, _config);
		// initialize the classifier
		_appClassifier.initializeClassifier();
		_logger.info("Cross validating");
		// perform cross validation on data
		_appClassifier.crossValidate();
		// print results
		_appClassifier.printSummary();
	}

	/**
	 * Filters the supplied set of instances using a
	 * set of filters for our test data.
	 * @param instances The set of WEKA instances to be filtered
	 * @return the set of filtered instances
	 * @throws Exception If filter(s) can not be applied to data
	 */
	public Instances filterData(Instances instances) throws Exception {
		_logger.info("Filtering data");
		// Apply whatever filters you want here
		_dataFilter = new InstancesFilter(instances);
		// The filter below removes attributes of type string & date
		// _dataFilter.removeTypeFilter(new String[] { "string", "date" });
		return _dataFilter.getFilteredInstances();
	}

	/**
	 * Does classification task on supplied data set
	 * @throws IOException If ARFF data can not be read
	 */
	public void predict() throws Exception {
		// read the test ARFF file
		readARFF("test");
		// initialize classifier
		_appClassifier = new AppClassifier(filterData(_testSet), _testSet, _config);
		// classify/evaluate model
		// do prediction
		_appClassifier.predict();
	}

	/**
	 * Reads the application configuration from the JSON file
	 * @throws IOException If application configuration can not be read
	 * @throws JSONException If application configuration can not be parsed
	 */
	public void readAppConfig() throws IOException, JSONException {
		_logger.info("Reading app config");
		_config.readConfig();
		_folds = Integer.parseInt(_config._folds);
	}

	/**
	 * Reads the supplied ARFF data file
	 * @param mode Indicates if ARFF to be read is training (train) or testing (test) data
	 * @throws Exception If data can not be read
	 */
	public void readARFF(String mode) throws Exception {
		checkExists(mode);
		_logger.info("Reading " + mode + " data");
		if (mode.equals("test")) {
			// read data from .ARFF file
			_dataReader = new InstancesReader(_config._testARFF);
			_testSet = _dataReader.readFromARFF();
			// set classification attribute
			_testSet.setClass(_testSet.attribute(_config._classValue));
		} else if (mode.equals("train")) {
			// read data from .ARFF file
			_dataReader = new InstancesReader(_config._trainingARFF);
			_trainSet = _dataReader.readFromARFF();
			// set classification attribute
			_trainSet.setClass(_trainSet.attribute(_config._classValue));
		} else if (mode.equals("dump")) {
			// read data from .ARFF file
			_dataReader = new InstancesReader(_config._dumpARFF);
			_dumpSet = _dataReader.readFromARFF();
			// set classification attribute
			_dumpSet.setClass(_dumpSet.attribute(_config._classValue));
		}
	}

	/**
	 * Splits the given ARFF Instances objects into training and test data based
	 * on a supplied split ratio.
	 * @param dataSet The WEKA instances object to split
	 * @throws Exception If instances can not be split
	 */
	public void splitData(Instances dataSet) throws Exception {
		_logger.info("Stratifying data");
		_dataFilter = new InstancesFilter(dataSet);
		_dataFilter.removeStratifiedFoldsFilter(4, 5, false);
		_trainSet = _dataFilter.getFilteredInstances();
		_dataFilter = new InstancesFilter(dataSet);
		_dataFilter.removeStratifiedFoldsFilter(4, 5, true);
		_testSet = _dataFilter.getFilteredInstances();
		writeData();
	}

	/**
	 * Writes partitioned test and training data sets to file
	 * @throws Exception If instances can not be written to file
	 */
	public void writeData() throws Exception {
		ARFFWriter aw = new ARFFWriter();
		aw.saveInstancesToARFF(_trainSet, _config._trainingARFF);
		aw.saveInstancesToARFF(_testSet, _config._testARFF);
	}

	/**
	 * a handle to the configuration reader
	 */
	public ConfigReader _config;

	/**
	 * a handle to a data filter object
	 */
	public InstancesFilter _dataFilter;

	/**
	 * a handle to a data reader object
	 */
	public InstancesReader _dataReader;

	/**
	 * the full dump set
	 */
	public Instances _dumpSet;

	/**
	 * a handle to the configuration holds
	 */
	public int _folds;

	/**
	 * a handle to the logger
	 */
	public Logger _logger;

	/**
	 * a handle to a classifier object
	 */
	public AppClassifier _appClassifier;

	/**
	 * the test set
	 */
	public Instances _testSet;

	/**
	 * the training set
	 */
	public Instances _trainSet;

	/**
	 * @param configuration The location of the JSON configuration file to use
	 * @throws IOException If JSON configuration file can not be read
	 * @throws JSONException If JSON configuration file can not be parsed
	 */
	public Experimenter(String configuration) throws IOException, JSONException {
		_logger = AppLogger.getLogger();
		_config = new ConfigReader(configuration);
		readAppConfig();
	}

	/**
	 * @param configuration The JSON configuration file
	 * @throws IOException If JSON configuration file can not be found
	 * @throws JSONException If JSON configuration file can not be parsed
	 */
	public Experimenter(JSONObject configuration) throws IOException, JSONException {
		_logger = AppLogger.getLogger();
		_config = new ConfigReader(configuration);
		readAppConfig();
	}

}
