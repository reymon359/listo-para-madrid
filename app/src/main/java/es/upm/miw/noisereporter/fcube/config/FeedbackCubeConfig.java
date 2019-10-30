/*******************************************************************************
 * Copyright (C) 2014 Open University of The Netherlands
 * Author: Bernardo Tabuenca Archilla
 * Lifelong Learning Hub project 
 * 
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package es.upm.miw.noisereporter.fcube.config;

import es.upm.miw.noisereporter.feeback.FeedbackColor;


public class FeedbackCubeConfig {

	
	/**
	 * > GET / HTTP/1.1                      : Responds w/ "Hello from Arduino Server"
	 * > GET /ring/color/ HTTP/1.1           : Responds w/ a JSON representation of 
	 *                                         the strip color -> {"r":x,"g":x,"b":x}
	 * > PUT /ring/on/ HTTP/1.1              : Turns the LED strip on
	 * > PUT /ring/off/ HTTP/1.1             : Turns the LED strip off

	 */
	
	private String CLASSNAME = this.getClass().getName();
	
	private String ip_address = "";
	

	// Number of polls to be made to calculate the averag
	public static final int NUM_POLLS = 5;
	

	public static final int POLL_INTERVAL = 1000; // 1 sec


	public static final int INIT_THRESHOLD = -25;
	public Double mThresholdMax;
	public Double mThresholdMin;
	

	
	// Current poll iteration
	private int mPollIndex = 0;
	
	// Buffer
	private double[] mNoiseArray  = {INIT_THRESHOLD,INIT_THRESHOLD,INIT_THRESHOLD,INIT_THRESHOLD,INIT_THRESHOLD};

	
	public static final String URL_PREFIX = "http://";



	private static FeedbackCubeConfig singleInstance;


	

	public static FeedbackCubeConfig getSingleInstance() {
		if (singleInstance == null) {
			synchronized (FeedbackCubeConfig.class) {
				if (singleInstance == null) {
					singleInstance = new FeedbackCubeConfig();
				}
			}
		}
		return singleInstance;
	}	
	

	public String getIp(){
		return ip_address;
	}
	
	public void setIp(String sIp) {
		ip_address = sIp;
	}
	






	/**
	 * Returns current value of the index
	 *  
	 * @return the mPollIndex
	 */
	public int getPollIndex() {

		return mPollIndex;
	}



	public void resetPollIndex() {
		this.mPollIndex = 0;
	}


	/**
	 * @return the mNoiseArray
	 */
	public double getAverageNoise() {
		
		double dSum = 0;
		
		for (int i = 0; i < mNoiseArray.length; i++) {
			dSum = mNoiseArray[i] + dSum;
			
		}
		
		return (dSum / NUM_POLLS);
	}



	public int addNoiseItem(double dNoiseItem) {
		this.mNoiseArray[mPollIndex] = dNoiseItem;
		mPollIndex++;
		return mPollIndex;
	}


	/**
	 * @return the mThresholdMax
	 */
	public Double getmThresholdMax() {
		return mThresholdMax;
	}


	/**
	 * @param mThresholdMax the mThresholdMax to set
	 */
	public void setmThresholdMax(Double mThresholdMax) {
		this.mThresholdMax = mThresholdMax;
	}


	/**
	 * @return the mThresholdMin
	 */
	public Double getmThresholdMin() {
		return mThresholdMin;
	}


	/**
	 * @param mThresholdMin the mThresholdMin to set
	 */
	public void setmThresholdMin(Double mThresholdMin) {
		this.mThresholdMin = mThresholdMin;
	}
	
}
