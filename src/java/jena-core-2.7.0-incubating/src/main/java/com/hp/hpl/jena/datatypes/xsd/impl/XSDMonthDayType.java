/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.xsd.AbstractDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

/**
 * Type processor for gMonthDay, most of the machinery is in the
 * base XSDAbstractDateTimeType class.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:58 $
 */
public class XSDMonthDayType extends XSDAbstractDateTimeType {

    /**
     * Constructor
     */
    public XSDMonthDayType(String typename) {
        super(typename);
    }

    //size without time zone: --MM-DD
    private final static int MONTHDAY_SIZE = 7;

    /**
     * Parse a validated date. This is invoked from
     * XSDDatatype.convertValidatedDataValue rather then from a local
     * parse method to make the implementation of XSDGenericType easier.
     */
    @Override
    public Object parseValidated(String str) {
        int len = str.length();
        int[] date = new int[TOTAL_SIZE];
        int[] timeZone = new int[2];

        //initialize
        date[CY]=YEAR;

        date[M]=parseInt(str, 2, 4);
        int start=5;

        date[D]=parseInt(str, start, start+2);

        if ( MONTHDAY_SIZE<len ) {
            int sign = findUTCSign(str, MONTHDAY_SIZE, len);
            getTimeZone(str, date, sign, len, timeZone);
        }

        if ( date[utc]!=0 && date[utc]!='Z' ) {
            AbstractDateTime.normalize(date, timeZone);
        }

        return new XSDDateTime(date, MONTH_MASK | DAY_MASK);
    }
    
}
