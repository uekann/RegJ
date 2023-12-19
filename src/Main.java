import lib.AST;

public class Main {
    public static void main(String[] args) {
        AST ast = AST.parse("(a|b)*abb");
        System.out.println(ast);
    }
}