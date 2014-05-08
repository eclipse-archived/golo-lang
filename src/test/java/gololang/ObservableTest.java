/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObservableTest {

  @Test
  public void smoke_test() {
    Observable observable = new Observable(3);

    assertThat(observable.get(), is((Object) 3));
    observable.set(5);
    assertThat(observable.get(), is((Object) 5));

    final Observable other = new Observable(666);
    observable.onChange(new Observable.Observer() {
      @Override
      public void apply(Object newValue) {
        other.set(newValue);
      }
    });
    assertThat(other.get(), is((Object) 666));
    observable.set(1);
    assertThat(other.get(), is((Object) 1));
  }

  @Test
  public void combinators() {
    Observable source = new Observable(1);

    Observable filtering = source.filter(new Observable.Predicate() {
      @Override
      public boolean apply(Object value) {
        return ((int) value) % 2 == 0;
      }
    });

    Observable mapping = source.map(new Observable.Function() {
      @Override
      public Object apply(Object value) {
        return ((int) value) * 10;
      }
    });

    assertThat(filtering.get(), nullValue());
    assertThat(mapping.get(), nullValue());

    source.set(2);
    assertThat(filtering.get(), is((Object) 2));
    assertThat(mapping.get(), is((Object) 20));

    source.set(3);
    assertThat(filtering.get(), is((Object) 2));
    assertThat(mapping.get(), is((Object) 30));

    source.set(4);
    assertThat(filtering.get(), is((Object) 4));
    assertThat(mapping.get(), is((Object) 40));
  }
}
