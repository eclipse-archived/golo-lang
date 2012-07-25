package gololang.compiler.ast;

import org.junit.Test;

import static gololang.compiler.ast.LocalReference.Kind.CONSTANT;
import static gololang.compiler.ast.LocalReference.Kind.VARIABLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

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
  }
}
