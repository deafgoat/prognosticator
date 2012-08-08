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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

// Log4j
import org.apache.log4j.Logger;

// JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 * Create graphs from prediction files.
 */
public class Charter {

	/**
	 * Creates data set containing categorical attributes along with prediction confidence
	 * @param files List of files containing predictions to chart
	 * @return the series collection to chart
	 */
	private DefaultCategoryDataset createCategoricalDataset(String[] files) {
		_logger.info("Collating data");
		BufferedReader br = null;
		// final XYSeriesCollection dataset = new XYSeriesCollection();
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		XYSeries prediction = null;
		for (String dataFile : files) {
			try {
				String sCurrentLine;
				prediction = new XYSeries(dataFile);
				br = new BufferedReader(new FileReader(dataFile));
				HashMap<String, Double> avgConfidence = new HashMap<String, Double>();
				HashMap<String, Integer> valueCount = new HashMap<String, Integer>();
				while ((sCurrentLine = br.readLine()) != null) {
					String[] data = sCurrentLine.split("\t");
					try {
						if (avgConfidence.containsKey(data[0])) {
							avgConfidence.put(data[0], avgConfidence.get(data[0]) + Double.parseDouble(data[1]));
							valueCount.put(data[0], valueCount.get(data[0]) + 1);
						} else {
							avgConfidence.put(data[0], Double.parseDouble(data[1]));
							valueCount.put(data[0], 1);
						}
					} catch (NumberFormatException e) {
						continue;
					}
				}
				for (Entry<String, Double> entry : avgConfidence.entrySet()) {
					dataset.addValue(entry.getValue() / valueCount.get(entry.getKey()), entry.getKey(), dataFile);
				}
			} catch (IOException e) {
				_logger.error(e.toString());
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					_logger.error(e.toString());
				}
			}
			if (prediction != null) {
				// dataset.addSeries(prediction);
			}
		}
		return dataset;
	}

	/**
	 * Creates data set containing numeric attributes along with prediction confidence
	 * @param files List of files containing predictions to chart
	 * @return the series collection to chart
	 */
	private XYSeriesCollection createNumericDataset(String[] files) {
		_logger.info("Collating data");
		BufferedReader br = null;
		XYSeries prediction = null;
		final XYSeriesCollection dataset = new XYSeriesCollection();
		for (String dataFile : files) {
			try {
				String sCurrentLine;
				br = new BufferedReader(new FileReader(dataFile));
				prediction = new XYSeries(dataFile);
				while ((sCurrentLine = br.readLine()) != null) {
					String[] data = sCurrentLine.split("\t");
					try {
						prediction.add(Double.parseDouble(data[1]), Double.parseDouble(data[2]));
					} catch (NumberFormatException e) {
						continue;
					}
				}
			} catch (IOException e) {
				_logger.error(e.toString());
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					_logger.error(e.toString());
				}
			}
			if (prediction != null) {
				dataset.addSeries(prediction);
			}
		}
		return dataset;
	}

	/**
	 * Shows the given chart
	 * @param name The name to save the chart as
	 * @param chart The chart to draw
	 */
	private void drawChart(String name, JFreeChart chart) {
		_logger.info("Plotting p.d. chart for " + name);
		ChartFrame frame = new ChartFrame(name, chart);
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Charts data set containing categorical attributes against with prediction confidence
	 * @param files List of files containing predictions to chart
	 * @throws IOException If list of files can not be read
	 * @return chart The chart to be drawn
	 */
	public JFreeChart getCategoricalChart(String[] files) throws IOException {
		DefaultCategoryDataset dataset = createCategoricalDataset(files);
		JFreeChart chart = ChartFactory.createBarChart3D(_chartName, // chart title
				"Attribute", // domain axis label
				"Average Confidence", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		return chart;
	}

	/**
	 * Charts data set containing numeric attributes against with prediction confidence
	 * @param files List of files containing predictions to chart
	 * @throws IOException If the list of files can not be read
	 * @return chart The chart to be drawn
	 */
	public JFreeChart getNumericChart(String[] files) throws IOException {
		XYSeriesCollection dataset = createNumericDataset(files);
		JFreeChart chart = ChartFactory.createScatterPlot(_chartName, // chart title
				"Values", // domain axis label
				"Confidence", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		return chart;
	}

	/**
	 * Saves a category chart to file
	 * @param category The category to chart
	 * @param files List of files containing predictions to chart
	 * @throws Exception If chart can not be saved
	 */
	public void saveCategorical(String category, String[] files) throws Exception {
		// read the test ARFF file
		_experimenter.readARFF("test");
		// initialize classifier
		AppClassifier sc = new AppClassifier(_experimenter.filterData(_experimenter._testSet), _experimenter._testSet, _experimenter._config);
		sc.errorAnalysis(category);
		Charter pd = new Charter(category);
		JFreeChart chart = pd.getCategoricalChart(files);
		pd.saveChart(category, chart);
	}

	/**
	 * Generates and saves confidence chart for nominal attributes
	 * @param mode Flag indicating if charts should be drawn on screen as well
	 * @param files List of files containing predictions to chart
	 * @throws Exception If chart can not be generate
	 */
	public void saveCategoricals(boolean mode, String[] files) throws Exception {
		// read the test ARFF file
		_experimenter.readARFF("test");
		// initialize classifier
		AppClassifier sc = new AppClassifier(_experimenter.filterData(_experimenter._testSet), _experimenter._testSet, _experimenter._config);
		ArrayList<String> categoryList = new ArrayList<String>();
		for (Attributes attribute : _experimenter._config._attributes.get(_experimenter._config._dumpFile)) {
			if (attribute.isInclude() && attribute.getAttributeType().equals("nominal")) {
				categoryList.add(attribute.getRawAttributeName());
			}
		}
		String[] categories = categoryList.toArray(new String[categoryList.size()]);
		Charter pd = null;
		JFreeChart chart = null;
		for (String category : categories) {
			sc.errorAnalysis(category);
			pd = new Charter(category);
			chart = pd.getCategoricalChart(files);
			if (mode) {
				pd.drawChart(_chartName, chart);
				Thread.sleep(5000);
			}
			pd.saveChart(_chartName, chart);
		}
	}

	/**
	 * Saves the given chart
	 * @param name The name to save the chart as
	 * @param chart The chart to save
	 * @throws IOException If the chart can not be saved
	 */
	private void saveChart(String name, JFreeChart chart) throws IOException {
		_logger.info("Saving chart for " + name);
		ChartUtilities.saveChartAsPNG(new File(name + ".png"), chart, 2000, 1500);
	}

	/**
	 * Saves a numeric chart to file
	 * @param numeric The numeric to chart
	 * @param files List of files containing predictions to chart
	 * @throws Exception If chart can not be generate
	 */
	public void saveNumeric(String numeric, String[] files) throws Exception {
		// read the test ARFF file
		_experimenter.readARFF("test");
		// initialize classifier
		AppClassifier sc = new AppClassifier(_experimenter.filterData(_experimenter._testSet), _experimenter._testSet, _experimenter._config);
		Charter pd = new Charter(numeric);
		sc.errorAnalysis(numeric);
		JFreeChart chart = pd.getNumericChart(files);
		pd.saveChart(numeric, chart);
	}

	/**
	 * Generates and saves confidence charts for numeric attributes
	 * @param mode Flag indicating if charts should be drawn on screen
	 * @param files List of files containing predictions to chart
	 * @throws Exception
	 */
	public void saveNumerics(boolean mode, String[] files) throws Exception {
		// read the test ARFF file
		_experimenter.readARFF("test");
		// initialize classifier
		AppClassifier sc = new AppClassifier(_experimenter.filterData(_experimenter._testSet), _experimenter._testSet, _experimenter._config);
		ArrayList<String> numericsList = new ArrayList<String>();
		for (Attributes attribute : _experimenter._config._attributes.get(_experimenter._config._dumpFile)) {
			if (attribute.isInclude() && attribute.getAttributeType().equals("numeric")) {
				numericsList.add(attribute.getRawAttributeName());
			}
		}
		String[] numerics = numericsList.toArray(new String[numericsList.size()]);
		Charter pd = null;
		JFreeChart chart = null;
		for (String numeric : numerics) {
			sc.errorAnalysis(numeric);
			pd = new Charter(numeric);
			chart = pd.getNumericChart(files);
			if (mode) {
				pd.drawChart(_chartName, chart);
				Thread.sleep(5000);
			}
			pd.saveChart(_chartName, chart);
		}
	}

	/** 
	 * handle to chart name
	 */
	private String _chartName;
	
	/**
	 * handle to experimenter object
	 */
	private Experimenter _experimenter;
	/**
	 * handle to logger object
	 */
	private Logger _logger;

	/**
	 * Public constructor
	 * @param name Experimenter handle to get charts with
	 * @throws IOException
	 */
	public Charter(Experimenter name) throws IOException {
		_logger = AppLogger.getLogger();
		_experimenter = name;
	}

	/**
	 * Private constructor
	 * @param name The name of the chart
	 * @throws IOException
	 */
	private Charter(String name) throws IOException {
		_logger = AppLogger.getLogger();
		_chartName = name;
	}

}
