package lib;

sealed abstract class AST permits AST.Char, AST.Union, AST.Concat, AST.Star, AST.Group{

    AST() {}

    public String toString() {
        return switch (this) {
            case Char c -> String.valueOf(c.c);
            case Union t -> t.t1.toString() + "|" + t.t2.toString();
            case Concat t -> t.t1.toString() + t.t2.toString();
            case Star t -> t.t.toString() + "*";
            case Group t -> "(" + t.t.toString() + ")";
        };
    }

    private static AST _parse(String re, AST ast){
        if (re.length() == 0) {
            return ast;
        }
        char c = re.charAt(0);
        return switch (c) {
            case '(' -> {
                int id = 1;
                int count = 1;
                while (count > 0) {
                    if (re.charAt(id) == '(') {
                        count++;
                    } else if (re.charAt(id) == ')') {
                        count--;
                    }
                    id++;
                    if (id >= re.length()) {
                        throw new RuntimeException("Unmatched parentheses");
                    }
                }
                AST t = _parse(re.substring(1, id), null);
                yield _parse(re.substring(id + 1), new Group(t));
            }
            case ')' -> ast;
            case '|' -> {
                AST t = _parse(re.substring(1), null);
                yield _parse(re.substring(1), new Union(ast, t));
            }
            case '*' -> _parse(re.substring(1), new Star(ast));
            default -> _parse(re.substring(1), new Concat(ast, new Char(c)));
        };
    }

    private static final class Char extends AST {
        public char c;

        public Char(char c) {
            super();
            this.c = c;
        }
    }
    private static final class Union<T1 extends AST, T2 extends AST> extends AST {
        public T1 t1;
        public T2 t2;

        public Union(T1 t1, T2 t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }
    }
    private static final class Concat<T1 extends AST, T2 extends AST> extends AST {
        public T1 t1;
        public T2 t2;

        public Concat(T1 t1, T2 t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }
    }
    private static final class Star<T extends AST> extends AST {
        public T t;

        public Star(T t) {
            super();
            this.t = t;
        }
    }

    private static final class Group<T extends AST> extends AST {
        public T t;

        public Group(T t) {
            super();
            this.t = t;
        }
    }
}