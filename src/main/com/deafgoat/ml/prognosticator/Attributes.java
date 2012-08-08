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

/**
 * This class allows us model each learning attribute.
 */
public final class Attributes {

	/**
	 * Create an attribute with given parameters
	 * @param attributeName The preferred name for this attribute
	 * @param attributeType The WEKA type of this attribute
	 * @param rawAttributeName The raw attribute name
	 * @param include Indicates if this attribute is to be included in the ARFF file
	 * @return The created attribute
	 */
	public static Attributes createAttribute(String rawAttributeName, String attributeName, String attributeType, Boolean include) {
		return new Attributes(rawAttributeName, attributeName == null || attributeName.equals("") ? rawAttributeName : attributeName, attributeType, include);
	}

	/**
	 * @return the attribute name
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	/**
	 * @return the attribute type
	 */
	public String getAttributeType() {
		return _attributeType;
	}

	/**
	 * Get the object
	 */
	public Attributes getObject() {
		return this;
	}

	/**
	 * @return the raw attribute name
	 */
	public String getRawAttributeName() {
		return _rawAttributeName;
	}

	/**
	 * @return if this attribute is marked to be included in the ARFF file
	 */
	public boolean isInclude() {
		return _include;
	}

	/**
	 * @param attributeName The attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		_attributeName = attributeName;
	}

	/**
	 * @param attributeType The attributeType to set
	 */
	public void setAttributeType(String attributeType) {
		_attributeType = attributeType;
	}

	/**
	 * @param include Indicates if this attribute is to be included in the ARFF file
	 */
	public void setInclude(boolean include) {
		_include = include;
	}

	/**
	 * @param rawAttributeName The rawAttributeName to set
	 */
	public void setRawAttributeName(String rawAttributeName) {
		_rawAttributeName = rawAttributeName;
	}

	/**
	 * name of the attribute
	 */
	private String	_attributeName;
	
	/** 
	 * WEKA type of the attribute
	 */
	private String	_attributeType;
	
	/**
	 * indicates whether to include this attribute
	 */
	private boolean	_include;
	
	/**
	 * raw attribute name from dump
	 */
	private String	_rawAttributeName;

	/**
	 * Constructor for Attributes class
	 */
	public Attributes() {
		_attributeName = null;
		_rawAttributeName = null;
		_attributeType = null;
		_include = false;
	}

	/**
	 * @param attributeName The preferred name for this attribute
	 * @param attributeType The WEKA type of this attribute
	 * @param rawAttributeName The raw attribute name
	 * @param include Indicates if this attribute is to be included in the ARFF file
	 */
	public Attributes(String rawAttributeName, String attributeName, String attributeType, boolean include) {
		_attributeName = attributeName;
		_attributeType = attributeType;
		_include = include;
		_rawAttributeName = rawAttributeName;
	}

}
