/* 
 * Copyright 2019, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.extensions

import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Extensions to the {@code ExecutorService} interface.
 * 
 * @author Emanuel Rabina
 */
class ExecutorServiceExtensions {

	/**
	 * Execute the given closure, performing a shutdown after it has exited.
	 * 
	 * @param <T>
	 * @param self
	 * @param closure
	 *   Called within the context of a try/finally block with the executor
	 *   service itself for performing any parallel tasks.
	 * @return
	 *   The return value from the closure.
	 */
	static <T> T executeAndShutdown(ExecutorService self, Closure<T> closure) {

		try {
			return closure(self)
		}
		finally {
			self.shutdown()
			if (!self.awaitTermination(5, TimeUnit.SECONDS)) {
				self.shutdownNow()
			}
		}
	}
}
