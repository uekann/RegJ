package lib.tests;

import lib.AST;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTTest {

    static HashMap<String, Constructor<?>> getConstructors() throws ClassNotFoundException, NoSuchMethodException {
        // ASTのインナークラス(Char, Union, Concat, Star, Group)のコンストラクタを取得
        HashMap<String, Constructor<?>> constructors = new HashMap<>(
                Map.of(
                        "Char", Class.forName("lib.AST$Char").getDeclaredConstructor(char.class),
                        "Union", Class.forName("lib.AST$Union").getDeclaredConstructor(AST.class, AST.class),
                        "Concat", Class.forName("lib.AST$Concat").getDeclaredConstructor(AST.class, AST.class),
                        "Star", Class.forName("lib.AST$Star").getDeclaredConstructor(AST.class),
                        "Group", Class.forName("lib.AST$Group").getDeclaredConstructor(AST.class)
                )
        );

        // コンストラクタをアクセス可能にする
        for(Constructor<?> constructor : constructors.values()){
            constructor.setAccessible(true);
        }

        return constructors;
    }

    HashMap<String, AST> constructSampleAST1() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {

        // ASTのインナークラス(Char, Union, Concat, Star, Group)のアクセス可能なコンストラクタを取得
        HashMap<String, Constructor<?>> constructors = getConstructors();

        // ASTのサンプルの作成
        AST charA = (AST) constructors.get("Char").newInstance('a');
        AST charB = (AST) constructors.get("Char").newInstance('b');
        AST charC = (AST) constructors.get("Char").newInstance('c');
        AST charD = (AST) constructors.get("Char").newInstance('d');
        AST unionBC = (AST) constructors.get("Union").newInstance(charB, charC);
        AST groupUnionBC = (AST) constructors.get("Group").newInstance(unionBC);
        AST starBC = (AST) constructors.get("Star").newInstance(groupUnionBC);
        AST concatAstarBC = (AST) constructors.get("Concat").newInstance(charA, starBC);
        AST concatConcatAstarBCcharD = (AST) constructors.get("Concat").newInstance(concatAstarBC, charD);

        return new HashMap<>(
                Map.of(
                        "a", charA,
                        "b", charB,
                        "c", charC,
                        "d", charD,
                        "b|c", unionBC,
                        "(b|c)", groupUnionBC,
                        "(b|c)*", starBC,
                        "a(b|c)*", concatAstarBC,
                        "a(b|c)*d", concatConcatAstarBCcharD
                )
        );
    }

    HashMap<String, AST> constructSampleAST2 () throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException{
        HashMap<String, Constructor<?>> constructors = getConstructors();

        AST charA = (AST) constructors.get("Char").newInstance('a');
        AST charB = (AST) constructors.get("Char").newInstance('b');
        AST charC = (AST) constructors.get("Char").newInstance('c');
        AST charD = (AST) constructors.get("Char").newInstance('d');
        AST concatAB = (AST) constructors.get("Concat").newInstance(charA, charB);
        AST unionABC = (AST) constructors.get("Union").newInstance(concatAB, charC);
        AST groupUnionABC = (AST) constructors.get("Group").newInstance(unionABC);
        AST starD = (AST) constructors.get("Star").newInstance(charD);
        AST concatABCD = (AST) constructors.get("Concat").newInstance(groupUnionABC, starD);

        return new HashMap<>(
                Map.of(
                        "a", charA,
                        "b", charB,
                        "c", charC,
                        "d", charD,
                        "ab", concatAB,
                        "ab|c", unionABC,
                        "(ab|c)", groupUnionABC,
                        "d*", starD,
                        "(ab|c)d*", concatABCD
                )
        );
    }
    @Test
    void testToString() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {

        // ASTのサンプルを取得
        HashMap<String, AST> sampleAST1 = constructSampleAST1();
        HashMap<String, AST> sampleAST2 = constructSampleAST2();

        // ASTの文字列化のテスト
        for(Map.Entry<String, AST> entry : sampleAST1.entrySet()){
            assertThat(entry.getValue().toString()).isEqualTo(entry.getKey());
        }
        for(Map.Entry<String, AST> entry : sampleAST2.entrySet()){
            assertThat(entry.getValue().toString()).isEqualTo(entry.getKey());
        }
    }

    @Test
    void testEquals() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        // ASTの複数の同一(構造は同一だがオブジェクトは別)なサンプルを取得
        AST sampleAST1 = constructSampleAST1().get("a(b|c)*d");
        AST sampleAST1Copy = constructSampleAST1().get("a(b|c)*d");
        AST sampleAST2 = constructSampleAST2().get("(ab|c)d*");
        AST sampleAST2Copy = constructSampleAST2().get("(ab|c)d*");

        // ASTの等価性のテスト
        assertThat(sampleAST1).isEqualTo(sampleAST1Copy);
        assertThat(sampleAST2).isEqualTo(sampleAST2Copy);
    }

    @Test
    void testParse() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        // ASTのサンプルと構文解析によって得られるASTを取得
        AST sampleAST1 = AST.parse("a(b|c)*d");
        AST sampleAST1Copy = constructSampleAST1().get("a(b|c)*d");
        AST sampleAST2 = AST.parse("(ab|c)d*");
        AST sampleAST2Copy = constructSampleAST2().get("(ab|c)d*");

        // ASTの等価性のテスト
        assertThat(sampleAST1).isEqualTo(sampleAST1Copy);
        assertThat(sampleAST2).isEqualTo(sampleAST2Copy);
    }
}
