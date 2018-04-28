/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package gololang.ir;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class ReferenceTableTest {

  @Test
  public void verify() {
    ReferenceTable root = new ReferenceTable()
        .add(LocalReference.of("foo"))
        .add(LocalReference.of("bar").variable());

    assertThat(root.hasReferenceFor("foo"), is(true));
    assertThat(root.hasReferenceFor("bar"), is(true));
    assertThat(root.hasReferenceFor("baz"), is(false));

    assertThat(root.get("foo"), is(LocalReference.of("foo")));
    assertThat(root.get("baz"), nullValue());

    assertThat(root.symbols().size(), is(2));
    assertThat(root.symbols(), hasItems("foo", "bar"));

    assertThat(root.references().size(), is(2));
    assertThat(root.references(), hasItems(
        LocalReference.of("foo"),
        LocalReference.of("bar").variable()));

    ReferenceTable child = root
        .fork()
        .add(LocalReference.of("baz"));

    assertThat(child.hasReferenceFor("foo"), is(true));
    assertThat(child.hasReferenceFor("bar"), is(true));
    assertThat(child.hasReferenceFor("baz"), is(true));

    assertThat(child.symbols().size(), is(3));
    assertThat(child.symbols(), hasItem("baz"));

    root.add(LocalReference.of("mrbean").variable());
    assertThat(child.references().size(), is(4));
    assertThat(child.symbols().size(), is(4));
    assertThat(child.hasReferenceFor("mrbean"), is(true));

    assertThat(child.ownedReferences().size(), is(1));
    assertThat(child.ownedSymbols().size(), is(1));

    child.remove("baz");
    assertThat(child.ownedReferences().size(), is(0));

    child.add(LocalReference.of("plop").variable());
    ReferenceTable flatCopy = child.flatDeepCopy(false);
    assertThat(flatCopy.references().size(), is(child.references().size()));
    child.get("plop").setIndex(666);
    assertThat(flatCopy.get("plop").getIndex(), not(is(child.get("plop").getIndex())));
  }
}
