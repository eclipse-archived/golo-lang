import sample.parser.*;

public class SimpleMain {

  public static void main(String... args) throws ParseException {
    Simple parser = new Simple(System.in);
    SimpleNode start = parser.Start();
    start.dump("# ");
    start.childrenAccept(new SimpleVisitor() {
      @Override
      public Object visit(SimpleNode node, Object data) {
        return null;
      }

      @Override
      public Object visit(ASTStart node, Object data) {
        return null;
      }

      @Override
      public Object visit(ASTNumber node, Object data) {
        System.out.println(">>> " + node.jjtGetValue());
        return null;
      }
    }, null);
  }
}
