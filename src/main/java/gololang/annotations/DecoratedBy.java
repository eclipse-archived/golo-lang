/*
 * Copyright 2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gololang.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>@DecoratedBy</code> is used to define the reference to the decorator on a decorated function.
 *
 * Mainly used for internal stuff, this annotation can be useful to create decorated function in Java.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DecoratedBy {
  /**
   * This is the reference to the decorator function.
   *
   * @return the reference to the decorator function.
   */
  String value();
}
