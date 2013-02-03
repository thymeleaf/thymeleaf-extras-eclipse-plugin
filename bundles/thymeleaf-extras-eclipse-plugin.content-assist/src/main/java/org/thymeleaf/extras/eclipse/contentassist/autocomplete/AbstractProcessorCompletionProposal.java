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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete;

import org.thymeleaf.extras.eclipse.dialect.xml.Processor;

/**
 * Common code for the attribute and element completion proposals.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractProcessorCompletionProposal extends AbstractCompletionProposal {

	protected final String fullprocessorname;

	/**
	 * Subclass constructor, set processor information.
	 * 
	 * @param processor		  Processor being proposed.
	 * @param charsentered	  How much of the entire proposal has already been
	 * 						  entered by the user.
	 * @param cursorposition
	 */
	protected AbstractProcessorCompletionProposal(Processor processor, int charsentered,
		int cursorposition) {

		super(processor, (processor.getDialect().getPrefix() + ":" + processor.getName()).substring(charsentered),
				cursorposition);

		fullprocessorname = processor.getDialect().getPrefix() + ":" + processor.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayString() {

		return fullprocessorname;
	}
}
