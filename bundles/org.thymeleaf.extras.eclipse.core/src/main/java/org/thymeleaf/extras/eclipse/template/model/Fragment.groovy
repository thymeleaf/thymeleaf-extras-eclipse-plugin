/*
 * Copyright 2013, The Thymelef Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.template.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model of a template fragment.
 * 
 * @author Emanuel Rabina
 */
public class Fragment {

	private static final Pattern FRAGMENT_SPEC_PATTERN = Pattern.compile("(.*?)(\\(.*\\))");

	private final String name;
	private final String[] arguments;

	/**
	 * Constructor, build a fragment from the given fragment spec string.
	 * 
	 * @param fragmentspec
	 * @throws IllegalArgumentException If the fragment spec is invalid.
	 */
	public Fragment(String fragmentspec) {

		Matcher matcher = FRAGMENT_SPEC_PATTERN.matcher(fragmentspec);
		if (matcher.matches()) {
			name = matcher.group(1);
			if (matcher.groupCount() == 2) {
				arguments = matcher.group(2).split(",");
				for (int i = 0; i < arguments.length; i++) {
					arguments[i] = arguments[i].trim();
				}
			}
			else {
				arguments = null;
			}
		}
		else {
			throw new IllegalArgumentException("Fragment spec doesn't conform to fragment signature pattern");
		}
	}

	/**
	 * Return the name of the fragment.
	 * 
	 * @return Fragment name.
	 */
	public String getName() {

		return name;
	}
}
