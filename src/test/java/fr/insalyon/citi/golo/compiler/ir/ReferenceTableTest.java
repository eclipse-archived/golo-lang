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

package fr.insalyon.citi.golo.compiler.ir;

import org.testng.annotations.Test;

import static fr.insalyon.citi.golo.compiler.ir.LocalReference.Kind.CONSTANT;
import static fr.insalyon.citi.golo.compiler.ir.LocalReference.Kind.VARIABLE;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class ReferenceTableTest {

  @Test
  public void verify() {
    ReferenceTable root = new ReferenceTable()
        .add(new LocalReference(CONSTANT, "foo"))
        .add(new LocalReference(VARIABLE, "bar"));

    assertThat(root.hasReferenceFor("foo"), is(true));
    assertThat(root.hasReferenceFor("bar"), is(true));
    assertThat(root.hasReferenceFor("baz"), is(false));

    assertThat(root.get("foo"), is(new LocalReference(CONSTANT, "foo")));
    assertThat(root.get("baz"), nullValue());

    assertThat(root.symbols().size(), is(2));
    assertThat(root.symbols(), hasItems("foo", "bar"));

    assertThat(root.references().size(), is(2));
    assertThat(root.references(), hasItems(
        new LocalReference(CONSTANT, "foo"),
        new LocalReference(VARIABLE, "bar")));

    ReferenceTable child = root
        .fork()
        .add(new LocalReference(CONSTANT, "baz"));

    assertThat(child.hasReferenceFor("foo"), is(true));
    assertThat(child.hasReferenceFor("bar"), is(true));
    assertThat(child.hasReferenceFor("baz"), is(true));

    assertThat(child.symbols().size(), is(3));
    assertThat(child.symbols(), hasItem("baz"));

    root.add(new LocalReference(VARIABLE, "mrbean"));
    assertThat(child.references().size(), is(4));
    assertThat(child.symbols().size(), is(4));
    assertThat(child.hasReferenceFor("mrbean"), is(true));

    assertThat(child.ownedReferences().size(), is(1));
    assertThat(child.ownedSymbols().size(), is(1));

    child.remove("baz");
    assertThat(child.ownedReferences().size(), is(0));

    child.add(new LocalReference(VARIABLE, "plop"));
    ReferenceTable flatCopy = child.flatDeepCopy(false);
    assertThat(flatCopy.references().size(), is(child.references().size()));
    child.get("plop").setIndex(666);
    assertThat(flatCopy.get("plop").getIndex(), not(is(child.get("plop").getIndex())));
  }
}
