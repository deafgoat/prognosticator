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
import java.util.List;
import java.util.Map;

/**
 * Abstract class that allow users define their own composite features.
 */
public abstract class UserDefinedAttribute {

	/**
	 * Returns the name of the attribute
	 * @return the name of the attribute
	 */
	public abstract String getAttributeName();

	/**
	 * Returns the attribute value. The user should ensure to take care of
	 * missing values in data as well. The user can access variables by using
	 * the attribute's "attributeName" or "rawAttributeName" if the former is
	 * not defined.
	 * @param instance The current instance
	 * @param attributeMap Contains a mapping of attribute name to its location in the current instance
	 * @param config The application configuration object
	 * @return The value of the attribute on a given instance
	 */
	public abstract String getAttributeValue(List<String> instance, Map<String, Integer> attributeMap, ConfigReader config);

	/**
	 * Returns the user defined attribute
	 * @return the created attribute
	 */
	public abstract Attributes getUserDefinedAttribute();
}
