import lib.AST;
import lib.NFA;

public class Main {
    public static void main(String[] args) {
        NFA nfa = NFA.fromAST(AST.parse("a(b|c)*d"));
        System.out.println(nfa.match("abccccccd"));
    }
}