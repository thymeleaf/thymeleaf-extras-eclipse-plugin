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

package org.thymeleaf.extras.eclipse.dialect.cache

import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod

/**
 * Representation of a file containing dialect information.
 * 
 * @author Emanuel Rabina
 */
class DialectFile {

	private final ArrayList<DialectItem> dialectItems

	@Lazy(soft = true)
	ArrayList<AttributeProcessor> attributeProcessors = { -> getDialectItemsByType(AttributeProcessor) }()

	@Lazy(soft = true)
	ArrayList<ElementProcessor> elementProcessors = { -> getDialectItemsByType(ElementProcessor) }()

	@Lazy(soft = true)
	ArrayList<ExpressionObjectMethod> expressionObjectMethods = { -> getDialectItemsByType(ExpressionObjectMethod) }()

	/**
	 * Constructor, associate this class with a dialect's processed items.
	 * 
	 * @param dialectItems
	 */
	DialectFile(List<DialectItem> dialectItems) {

		this.dialectItems = new ArrayList<>(dialectItems)
	}

	/**
	 * Get all of the given type of dialect item in this dialect.
	 * 
	 * @param type
	 *   Item type.
	 * @param <T>
	 *   Item type.
	 * @return List of all dialect items of the given type.
	 */
	private <T> ArrayList<T> getDialectItemsByType(Class<T> type) {

		return dialectItems.findAll { dialectItem -> type.isAssignableFrom(dialectItem.class) }
	}
}
