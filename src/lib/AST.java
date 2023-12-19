package lib;

import java.util.HashSet;
import java.util.List;

public sealed abstract class AST permits AST.Char, AST.Union, AST.Concat, AST.Star, AST.Group{

    static final class Char extends AST {
        // 一文字の正規表現
        public char c;

        Char(char c) {
            super();
            this.c = c;
        }
    }
    static final class Union<T1 extends AST, T2 extends AST> extends AST {
        // 正規表現t1とt2の和
        public T1 t1;
        public T2 t2;

        Union(T1 t1, T2 t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }
    }
    static final class Concat<T1 extends AST, T2 extends AST> extends AST {
        // 正規表現t1とt2の連結
        public T1 t1;
        public T2 t2;

        Concat(T1 t1, T2 t2) {
            super();
            this.t1 = t1;
            this.t2 = t2;
        }
    }
    static final class Star<T extends AST> extends AST {
        // 正規表現tの繰り返し
        public T t;

        Star(T t) {
            super();
            this.t = t;
        }
    }

    static final class Group<T extends AST> extends AST {
        // 正規表現tをグループ化
        public T t;

        Group(T t) {
            super();
            this.t = t;
        }
    }

    public String toString() {
        // 正規表現の文字列化
        return switch (this) {
            case Char c -> String.valueOf(c.c);
            case Union<?, ?> t -> STR."\{t.t1.toString()}|\{t.t2.toString()}";
            case Concat<?, ?> t -> t.t1.toString() + t.t2.toString();
            case Star<?> t -> STR."\{t.t.toString()}*";
            case Group<?> t -> STR."(\{t.t.toString()})";
        };
    }

    public boolean equals(Object obj) {
        // 正規表現の等価性判定
        if(!(obj instanceof AST ast)){
            return false;
        }
        return switch (this) {
            case Char c -> ast instanceof Char && c.c == ((Char) ast).c;
            case Union<?, ?> t -> ast instanceof Union && t.t1.equals(((Union<?, ?>) ast).t1) && t.t2.equals(((Union<?, ?>) ast).t2);
            case Concat<?, ?> t -> ast instanceof Concat && t.t1.equals(((Concat<?, ?>) ast).t1) && t.t2.equals(((Concat<?, ?>) ast).t2);
            case Star<?> t -> ast instanceof Star && t.t.equals(((Star<?>) ast).t);
            case Group<?> t -> (ast instanceof Group && t.t.equals(((Group<?>) ast).t)) || t.t.equals(ast);
        };
    }

    private static AST _concat(AST t1, AST t2) {
        // 連結の省略
        if (t1 == null) {
            return t2;
        }
        if (t2 == null) {
            return t1;
        }
        return new Concat<>(t1, t2);
    }

    private static AST _parse(String re, AST ast){
        // 正規表現の構文解析
        // re: これから構文解析する正規表現
        // ast: 現在までの構文解析結果

        if (re.isEmpty()) {
            return ast;
        }
        char c = re.charAt(0);
        return switch (c) {
            case '(' -> {
                // 括弧の対応を取る
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

                // 括弧内の正規表現を再帰的に構文解析
                AST t = _parse(re.substring(1, id), null);

                // 括弧内の正規表現をグループ化
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

                // 連結されている場合は、後ろの正規表現を繰り返す
                case Concat<?, ?> t -> _parse(re.substring(1),new Concat<>(t.t1, new Star<>(t.t2)));

                // それ以外の場合は、正規表現を繰り返す
                default -> _parse(re.substring(1), new Star<>(ast));
            };
            default -> _parse(re.substring(1), _concat(ast, new Char(c)));
        };
    }

    public static AST parse(String re) {
        // 正規表現の構文解析
        return _parse(re, null);
    }
}