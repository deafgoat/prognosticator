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
import java.io.IOException;
import java.util.HashMap;

import weka.core.Instance;
// Weka

/**
 * Models prediction results.
 */
public final class Prediction implements Comparable<Prediction> {

	@Override
	public int compareTo(Prediction pred) {
		if (pred._instance.classAttribute().isNumeric()) {
			return 0;
		}
	    return (_confidence > pred._confidence ? -1 : (_confidence == pred._confidence ? 0 : 1));
	}

	/**
	 * Generates a mapping for each of the instance's attributes (name) to their
	 * index location
	 * @return map of instance attribute name to location
	 */
	private HashMap<String, Integer> getAttributeMap() {
		HashMap<String, Integer> attributeIndex = new HashMap<String, Integer>();
		for (int i = 0; i < _instance.numAttributes(); i++) {
			attributeIndex.put(_instance.dataset().attribute(i).name(), i);
		}
		return attributeIndex;
	}

	/**
	 * Gets the confidence of the predicted class
	 * @return predicted class confidence
	 */
	public Double getConfidence() {
		double maximum = _distribution[0];
	    for (int i = 1; i < _distribution.length; i++) {
	        if (_distribution[i] > maximum) {
	            maximum = _distribution[i];
	        }
	    }
	    return maximum;
	}

	/**
	 * Gets the index of the predicted instance
	 * @return the predicted instance index
	 */
	public int getIndex() {
		return _instanceIndex;
	}
	
	/**
	 * Gets the confidence of the all predicted classes
	 * @return class confidence distribution
	 */
	public String getPrediction() {
		if (_instance.classAttribute().isNumeric()) {
			return Double.toString(_confidence);
		}
		return _prediction;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(_instanceIndex + _delimeter);
		sb.append(_confidence + _delimeter);
		sb.append(_prediction + _delimeter);
		return sb.toString();
	}
	
	/**
	 * Gets the value of attribute in the instance
	 * @return tab-delimited attribute distribution
	 */
	public String attributeDistribution(String attribute) {
		StringBuilder sb = new StringBuilder();
		int location = 0;
		location = _attributeIndex.get(attribute);
		sb.append(_instance.toString(location) + _delimeter);
		sb.append(_confidence + _delimeter);
		return sb.toString();
	}

	/**
	 * the delimeter for prediction file
	 */
	private static final String	_delimeter	= "\t";
	
	/** 
	 * mapping of attribute name to location
	 */
	private HashMap<String, Integer> _attributeIndex;
	
	/**
	 * for NaiveBayes, holds the probability distribution
	 */
	private double[] _distribution;
	
	/**
	 * the instance to be predicted 
	 */
	private Instance _instance;

	/** 
	 * the predicted class of the instance
	 */
	private String _prediction;

	/** 
	 * the index of the predicted instance
	 */
	private int _instanceIndex;
	
	/** 
	 * the prediction confidence
	 */
	private double _confidence;

	/**
	 * Constructor for instance prediction
	 * @param instanceIndex The instance index in dump
	 * @param prediction The classifier prediction
	 * @param distribution The prediction confidence distribution
	 * @param instance The test instance
	 * @throws IOException
	 */
	public Prediction(int instanceIndex, String prediction, double[] distribution, Instance instance) throws IOException {
		_prediction = prediction;
		_distribution = distribution;
		_instance = instance;
		_instanceIndex = instanceIndex;
		_attributeIndex = getAttributeMap();
		_confidence = getConfidence();
	}
}
