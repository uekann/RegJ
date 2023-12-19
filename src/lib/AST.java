package lib;

public sealed abstract class AST permits AST.Char, AST.Union, AST.Concat, AST.Star, AST.Group{

    AST() {}

    public String toString() {
        return switch (this) {
            case Char c -> String.valueOf(c.c);
            case Union<?, ?> t -> STR."\{t.t1.toString()}|\{t.t2.toString()}";
            case Concat<?, ?> t -> t.t1.toString() + t.t2.toString();
            case Star<?> t -> STR."\{t.t.toString()}*";
            case Group<?> t -> STR."(\{t.t.toString()})";
        };
    }

    private static AST _concat(AST t1, AST t2) {
        if (t1 == null) {
            return t2;
        }
        if (t2 == null) {
            return t1;
        }
        return new Concat<>(t1, t2);
    }

    private static AST _parse(String re, AST ast){
        if (re.isEmpty()) {
            return ast;
        }
        char c = re.charAt(0);
        return switch (c) {
            case '(' -> {
                int id = 0;
                int count = 1;
                while (count > 0) {
                    id++;
                    if (id >= re.length()) {
                        throw new RuntimeException("Unmatched parentheses");
                    }
                    if (re.charAt(id) == '(') {
                        count++;
                    } else if (re.charAt(id) == ')') {
                        count--;
                    }
                }
                AST t = _parse(re.substring(1, id), null);
                yield _parse(re.substring(id+1), _concat(ast, new Group<>(t)));
            }
            case ')' -> ast;
            case '|' -> {
                AST t = _parse(re.substring(1), null);
                yield new Union<>(ast, t);
            }
            case '*' -> switch (ast) {
                case null -> throw new RuntimeException("Nothing to repeat");
                case Star<?> _ -> throw new RuntimeException("Nothing to repeat");
                case Concat<?, ?> t -> _parse(re.substring(1),new Concat<>(t.t1, new Star<>(t.t2)));
                default -> _parse(re.substring(1), new Star<>(ast));
            };
            default -> _parse(re.substring(1), _concat(ast, new Char(c)));
        };
    }

    public static AST parse(String re) {
        return _parse(re, null);
    }

    public static final class Char extends AST {
        public char c;

        public Char(char c) {
            super();
            this.c = c;
        }
    }
    public static final class Union<T1 extends AST, T2 extends AST> extends AST {
        public T1 t1;
        public T2 t2;

        public Union(T1 t1, T2 t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }
    }
    public static final class Concat<T1 extends AST, T2 extends AST> extends AST {
        public T1 t1;
        public T2 t2;

        public Concat(T1 t1, T2 t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }
    }
    public static final class Star<T extends AST> extends AST {
        public T t;

        public Star(T t) {
            super();
            this.t = t;
        }
    }

    public static final class Group<T extends AST> extends AST {
        public T t;

        public Group(T t) {
            super();
            this.t = t;
        }
    }
}