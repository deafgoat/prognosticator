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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

// Log4j
import org.apache.log4j.Logger;

// JSON
import org.json.JSONException;

// SuperCSV
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

// Weka
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * This class helps us create ARFF files from CSV dumps using custom
 * configuration parameters.
 */
public final class ARFFWriter {

	/**
	 * Allows the user to add posite features for learning
	 */
	private void addUserDefinedAttributes() {
		_logger.info("Adding user defined features");
		// initialize list of user defined attributes
		_userDefinedAttributes = new ArrayList<UserDefinedAttribute>();
		// add each of the user defined attributes. e.g.
		// _userDefinedAttributes.add(new TempChange());

		// do not edit the lines below this
		Attributes attribute = null;
		for (UserDefinedAttribute userDefined : _userDefinedAttributes) {
			attribute = userDefined.getUserDefinedAttribute();
			setType(attribute, false);
			_logger.info("Added " + attribute.getAttributeName());
		}
	}

	/**
	 * Extract the instances from the .CSV file
	 * @param cls
	 * @throws IOException
	 * @throws ParseException
	 */
	private void extractInstances() throws IOException, ParseException {
		int index, count = 0;
		String curValue = null;
		String classValue = null;
		List<String> dump;
		Attributes attribute;
		setReader(_inCSV);
		String[] header = _reader.getCSVHeader(true);
		_values = new double[_data.numAttributes()];
		if (_logger.isDebugEnabled()) {
			_logger.debug("Now extracting instances");
		}

		while ((dump = _reader.read(_processing)) != null) {
			for (int i = 0; i < dump.size(); i++) {
				curValue = dump.get(i).trim();
				index = _config._attributeMap.get(_config._dumpFile).get(header[i].trim().toLowerCase());
				attribute = _config._attributes.get(_config._dumpFile).get(index);
				// only consider attributes marked as 'include'
				if (attribute.isInclude()) {
					// insert attribute into double instance array
					insertValue(attribute, curValue);
					if (attribute.getAttributeName().equals(_config._classValue)) {
						classValue = curValue;
					}
				}
			}
			// insert user defined feature for this instance
			setUserDefinedAttributes(dump);
			count += 1;
			if (count % 10000 == 0) {
				if (_logger.isDebugEnabled()) {
					_logger.debug("Processed " + count + " records.");
				}
			}
			// add weights according to instance class value
			if (classValue.equals(_config._positiveClassValue)) {
				_data.add(new DenseInstance(_config._positiveClassWeight, _values));
			} else {
				_data.add(new DenseInstance(_config._negativeClassWeight, _values));
			}
			_values = new double[_data.numAttributes()];
			if (_logger.isDebugEnabled()) {
				_logger.debug("Processed " + count + " records.");
			}
		}
		_logger.info("Done. Processed " + count + " records!");
	}

	/**
	 * Generates mapping from attribute name to an integer value. This is
	 * important in creating the .ARFF file in an acceptable format. Note: This
	 * must be called before any user-generate attributes are added.
	 */
	private void generateAttributeMap() {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Creating attribute map");
		}
		_attributeMap = new HashMap<String, Integer>();
		int numAttributes = _attributes.size();

		// WEKA map
		for (int i = 0; i < numAttributes; i++) {
			_attributeMap.put(_attributes.get(i).name(), i);
		}
	}

	/**
	 * Initializes our instances object once we've added all our dump and
	 * user-defined features
	 */
	private void initializeInstances() {
		_logger.info("Initializing instances object");
		_data = new Instances(_config._relation, _attributes, 0);
	}

	/**
	 * Inserts the value of an attribute at the appropriate position
	 * @param attribute The attribute object
	 * @param value The attribute value to insert
	 * @throws ParseException
	 */
	private void insertValue(Attributes attribute, String value) throws ParseException {
		String name = attribute.getAttributeName();
		String type = attribute.getAttributeType();
		if (value.equals("") || value.equals("?")) {
			_values[_attributeMap.get(name)] = Utils.missingValue();
		} else if (type.equals("numeric")) {
			_values[_attributeMap.get(name)] = Double.parseDouble(value);
		} else if (type.equals("string")) {
			_values[_attributeMap.get(name)] = _data.attribute(_attributeMap.get(name)).addStringValue(value);
		} else if (type.equals("date")) {
			_values[_attributeMap.get(name)] = _data.attribute(_attributeMap.get(name)).parseDate(value);
		} else if (type.equals("nominal")) {
			_values[_attributeMap.get(name)] = _nominalRange.get(name).indexOf(value);
		} else {
			_logger.warn("Found unanticipated entry set: " + name + " = " + value);
		}
	}

	/**
	 * Prints missing count of all attributes across all instances
	 * @throws IOException
	 */
	public void printMissingAttributeCount() throws IOException {
		List<String> dump;
		setReader(_inCSV);
		String[] header = _reader.getCSVHeader(true);
		Map<String, Integer> attCount = new HashMap<String, Integer>();
		if (_logger.isDebugEnabled()) {
			_logger.debug("Discovering missing attributes");
		}
		// iterate through all records to discover
		// data set nominal attribute ranges.
		while ((dump = _reader.read(_processing)) != null) {
			for (int i = 0; i < dump.size(); i++) {
				if (!dump.get(i).equals("")) {
					if (!attCount.containsKey(header[i])) {
						attCount.put(header[i], 1);
					} else {
						attCount.put(header[i], attCount.get(header[i]) + 1);
					}
				}
			}
		}
		// print counts
		for (Map.Entry<String, Integer> entry : attCount.entrySet()) {
			System.out.format("%-42s = %10d%n", entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Saves an Instances object to .ARFF
	 * @param instances The set of instances to write
	 * @param outFile The file to which to write the instances
	 * @throws IOException If instances object can not be written
	 */
	public void saveInstancesToARFF(Instances instances, String outFile) throws IOException {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Now saving instances");
		}
		InstancesWriter.writeInstances(instances, outFile);
		if (_logger.isDebugEnabled()) {
			_logger.debug("Saved instances to " + outFile);
		}
	}

	/**
	 * Sets attribute types for all non-nominal attributes
	 * @param cls
	 * @throws IOException
	 */
	private void setAttributeType() throws IOException {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Setting attribute types");
		}
		// iterate through all attributes and set their type
		Attributes attribute = null;
		for (int i = 0; i < _config._attributes.get(_config._dumpFile).size(); i++) {
			attribute = _config._attributes.get(_config._dumpFile).get(i);
			if (attribute.isInclude()) {
				setType(attribute, false);
			}
		}
		// set the class attribute type
		setType(_target, true);
	}

	/**
	 * Sets internal map for user-defined features note that features defined by
	 * user may use attributes that are not marked as 'include' in the
	 * configuration file.
	 */
	private void setInternalMap() {
		_internalMap = new HashMap<String, Integer>();
		// Internal map
		for (int i = 0; i < _header.length; i++) {
			_internalMap.put(_header[i].trim(), i);
		}
	}

	/**
	 * Discovers the set of possible configurations for nominal attributes
	 * @throws IOException
	 */
	private void setNominalRange() throws IOException {
		int index = 0;
		List<String> dump;
		Attributes attribute;
		setReader(_inCSV);
		String key, name, value = null;
		_header = _reader.getCSVHeader(true);
		Map<String, Set<String>> nominalAttRange = new HashMap<String, Set<String>>();
		for (UserDefinedAttribute uda : _userDefinedAttributes) {
			nominalAttRange.put(uda.getAttributeName(), new HashSet<String>());
		}
		setInternalMap();
		if (_logger.isDebugEnabled()) {
			_logger.debug("Discovering nominal ranges");
		}
		// iterate through all records to discover
		// data set nominal attribute ranges.
		while ((dump = _reader.read(_processing)) != null) {
			for (int i = 0; i < dump.size(); i++) {
				value = dump.get(i).trim();
				if (!value.equals("")) {
					key = _header[i].trim();
					try {
						index = _config._attributeMap.get(_config._dumpFile).get(key.toLowerCase());
					} catch (Exception e) {
						_logger.error("Could not find specified configuration attribute "
						+ key + "\n" + e.getMessage(), e);
					}
					attribute = _config._attributes.get(_config._dumpFile).get(index);
					// check that attribute is to be included and 'nominal'
					if (attribute.getAttributeType().equals("nominal") && attribute.isInclude()) {
						name = attribute.getAttributeName();
						if (nominalAttRange.get(name) == null) {
							nominalAttRange.put(name, new HashSet<String>());
						}
						nominalAttRange.get(name).add(value);
						for (UserDefinedAttribute uda : _userDefinedAttributes) {
							if (uda.getUserDefinedAttribute().isInclude()) {
								name = uda.getAttributeName();
								value = uda.getAttributeValue(dump, _internalMap, _config);
								if (!value.equals("")) {
									nominalAttRange.get(name).add(value);
								}
							}
						}
					}
				}
			}
		}

		ArrayList<String> curAttribute;
		// convert the sets to ordered lists for later retrieval
		_nominalRange = new HashMap<String, ArrayList<String>>();
		for (Entry<String, Set<String>> entry : nominalAttRange.entrySet()) {
			_nominalRange.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
		}
		ArrayList<String> _value;
		// add the list to our attributes object
		for (Entry<String, ArrayList<String>> entry : _nominalRange.entrySet()) {
			_value = entry.getValue();
			curAttribute = new ArrayList<String>();
			for (String s : _value) {
				curAttribute.add(s);
			}
			// convenience check to ensure class attribute is added last
			if (entry.getKey().equals(_config._classValue)) {
				_targetRange = curAttribute;
			} else {
				_attributes.add(new Attribute(entry.getKey(), curAttribute));
			}
		}
	}

	/**
	 * Reset the .CSV file reader
	 * @param dataFile
	 * @throws IOException
	 */
	private void setReader(String inCSV) throws IOException {
		_reader = new CsvListReader(new FileReader(inCSV), CsvPreference.EXCEL_PREFERENCE);
	}

	/**
	 * Sets attribute based on its WEKA type
	 * 
	 * @param attribute Thes attribute to set
	 * @param targetSet The flag that allows the class value to be set last
	 */
	private void setType(Attributes attribute, boolean targetSet) {
		String name = attribute.getAttributeName();
		String type = attribute.getAttributeType();
		_internalAttributes = new ArrayList<Attributes>();
		_internalAttributes.add(attribute);

		if (name.equals(_config._classValue) && !targetSet) {
			_target = attribute;
		} else if (type.equals("nominal")) {
			if (targetSet) {
				_attributes.add(new Attribute(_target.getAttributeName(), _targetRange));
			} else {
				return;
			}
		} else if (type.equals("numeric")) {
			_attributes.add(new Attribute(name));
		} else if (type.equals("string")) {
			_attributes.add(new Attribute(name, (ArrayList<String>) null));
		} else if (type.equals("date")) {
			_attributes.add(new Attribute(name, _config._dateFormat));
		} else {
			_logger.warn("Found unexpected key: " + attribute.getAttributeName());
		}
	}

	/**
	 * Sets the values of all user defined features on current instance
	 * @throws ParseException
	 */
	private void setUserDefinedAttributes(List<String> instance) throws ParseException {
		Attributes attribute = null;
		String value = null;
		for (UserDefinedAttribute userDefined : _userDefinedAttributes) {
			attribute = userDefined.getUserDefinedAttribute();
			value = userDefined.getAttributeValue(instance, _internalMap, _config);
			insertValue(attribute, value);
		}
	}

	/**
	 * Performs steps involved in creating the .ARFF file in an acceptable format.
	 * @throws Exception
	 */
	public void writeARFF() throws Exception {
		_logger.info("Beginning ARFF creation");
		// add any other interesting attributes
		addUserDefinedAttributes();
		// predetermines the range of nominal attributes
		setNominalRange();
		// sets the attribute types of all other attributes
		setAttributeType();
		// internal method used to track attribute index
		generateAttributeMap();
		// initializes the instances object
		initializeInstances();
		// extracts the instance from the dump
		extractInstances();
		// saves the instances to file
		saveInstancesToARFF(_data, _outARFF);
		_logger.info("Finished ARFF creation");
	}
	
	/** 
	 * contains a mapping of WEKA attribute name to its location 
	 */
	private Map<String, Integer> _attributeMap;
	
	/** 
	 * contains a mapping of all WEKA transformed attributes in configuration
	 */
	private ArrayList<Attribute> _attributes;
	
	/** 
	 * the handle to the configuration reader
	 */
	private ConfigReader _config;
	
	/** 
	 * the set of instances to be written
	 */
	private Instances _data;
	
	/** 
	 * the headers of the CSV dump
	 */
	private String[] _header;
	
	/**
	 * the reference CSV dump
	 */
	private String _inCSV;
	
	/** 
	 * contains a list of all internal attributes described in configuration
	 */
	private ArrayList<Attributes> _internalAttributes;
	
	/**
	 * contains a mapping of internal attribute name to its location
	 */
	private Map<String, Integer> _internalMap;
	
	/**
	 * a handle to the logging object 
	 */
	private Logger _logger;
	
	/**
	 * contains a mapping of nominal attributes to their range
	 */
	private Map<String, ArrayList<String>>	_nominalRange;
	
	/**
	 * the target ARFF file to be written to
	 */
	private String _outARFF;
	
	/**
	 * the cell processing object
	 */
	private CellProcessor[] _processing;
	
	/**
	 * the CSV dump reader
	 */
	private ICsvListReader _reader;
	
	/**
	 * holds the target class of the data set
	 */
	private Attributes	_target;
	
	/**
	 * holds the target range of possibly nominal class attribute
	 */
	private ArrayList<String> _targetRange;
	
	/**
	 * the list of user-defined attributes
	 */
	private List<UserDefinedAttribute>	_userDefinedAttributes;
	
	/** 
	 * the set of values holding the current instance
	 */
	private double[] _values;

	/**
	 * Class constructor
	 * @throws IOException
	 */
	public ARFFWriter() throws IOException {
		_logger = Logger.getLogger(AppLogger.class.getName());
	}

	/**
	 * Class constructor - accepts input .CSV file and saves to .ARFF file.
	 * @param config
	 * @throws IOException
	 * @throws JSONException
	 */
	public ARFFWriter(ConfigReader config) throws JSONException, IOException {
		_logger = AppLogger.getLogger();
		_config = config;
		_config.readConfig();
		_config.setAttributeMap();
		_attributes = new ArrayList<Attribute>();
		// We don't want to enforce any constraints for now
		int columnSize = _config._attributes.get(_config._dumpFile).size();
		_processing = new CellProcessor[columnSize];
		_outARFF = _config._dumpARFF;
		_inCSV = _config._dumpFile;
		setReader(_inCSV);
	}

	/**
	 * Class constructor - accepts input .CSV file and saves to .ARFF file.
	 * @param config
	 * @param inCSV
	 * @param outARFF
	 * @throws IOException
	 * @throws JSONException
	 */
	public ARFFWriter(ConfigReader config, String inCSV, String outARFF) throws IOException, JSONException {
		_logger = AppLogger.getLogger();
		_config = config;
		_config.readConfig();
		_config.setAttributeMap();
		_attributes = new ArrayList<Attribute>();
		// We don't want to enforce any constraints for now
		int columnSize = _config._attributes.get(_config._dumpFile).size();
		_processing = new CellProcessor[columnSize];
		_outARFF = outARFF;
		_inCSV = inCSV;
		setReader(_inCSV);
	}

}
