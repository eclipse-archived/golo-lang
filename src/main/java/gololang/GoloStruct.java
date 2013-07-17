/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package gololang;

import java.util.Iterator;

public abstract class GoloStruct implements Iterable<Tuple> {

  protected String[] members;

  public GoloStruct() {
    super();
  }

  public Tuple members() {
    return Tuple.fromArray(members);
  }

  public abstract Tuple values();

  public abstract Object get(String member);

  @Override
  public Iterator<Tuple> iterator() {
    return new Iterator<Tuple>() {

      final Iterator<?> memberIterator = members().iterator();
      final Iterator<?> valuesIterator = values().iterator();

      @Override
      public boolean hasNext() {
        return memberIterator.hasNext();
      }

      @Override
      public Tuple next() {
        return new Tuple(memberIterator.next(), valuesIterator.next());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
