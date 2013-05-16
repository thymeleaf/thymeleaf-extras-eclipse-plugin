/*
 * Copyright 2013, The Thymeleaf Project (http://www.thymeleaf.org/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thymeleaf.extras.eclipse.dialect;

import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Representation of a project that contains one or more files which in turn
 * contain dialect information.
 * 
 * @author Emanuel Rabina
 */
public class DialectProject {

	final TreeMap<Dialect,DialectFile> dialectfiles = new TreeMap<Dialect,DialectFile>(new Comparator<Dialect>() {
		@Override
		public int compare(Dialect dialect1, Dialect dialect2) {
			return dialect1.getPrefix().compareTo(dialect2.getPrefix());
		}
	});

	/**
	 * Return all of the attribute processors in this project.
	 * 
	 * @return List of this project's attribute processors.
	 */
	public List<AttributeProcessor> getAttributeProcessors() {

		ArrayList<AttributeProcessor> processors = new ArrayList<AttributeProcessor>();
		for (DialectFile dialectfile: dialectfiles.values()) {
			processors.addAll(dialectfile.getAttributeProcessors());
		}
		return processors;
	}

	/**
	 * Return all of the element processors in this project.
	 * 
	 * @return List of this project's element processors.
	 */
	public List<ElementProcessor> getElementProcessors() {

		ArrayList<ElementProcessor> processors = new ArrayList<ElementProcessor>();
		for (DialectFile dialectfile: dialectfiles.values()) {
			processors.addAll(dialectfile.getElementProcessors());
		}
		return processors;
	}
}
