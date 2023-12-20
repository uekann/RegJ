package lib.tests;

import lib.AST;
import lib.NFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NFATest {

    public static HashMap<Integer, Object> getNodes(NFA nfa) throws NoSuchFieldException, IllegalAccessException {
        Field nodesField = nfa.getClass().getDeclaredField("nodes");
        nodesField.setAccessible(true);
        return (HashMap<Integer, Object>) nodesField.get(nfa);
    }

    public static Object getStart(NFA nfa) throws NoSuchFieldException, IllegalAccessException {
        Field startField = nfa.getClass().getDeclaredField("start");
        startField.setAccessible(true);
        return startField.get(nfa);
    }

    public static Object getEnd(NFA nfa) throws NoSuchFieldException, IllegalAccessException {
        Field endField = nfa.getClass().getDeclaredField("end");
        endField.setAccessible(true);
        return endField.get(nfa);
    }

    public static HashMap<Character, HashSet<Object>> getTransitions(Object node) throws NoSuchFieldException, IllegalAccessException {
        Field transitionsField = node.getClass().getDeclaredField("transitions");
        transitionsField.setAccessible(true);
        assert transitionsField.get(node) instanceof HashMap;
        return (HashMap<Character, HashSet<Object>>) transitionsField.get(node);
    }

    @Test
    void testFromAST() throws
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException,
            NoSuchFieldException
    {
        HashMap<String, Constructor<?>> constructors = ASTTest.getConstructors();

        AST charA = (AST) constructors.get("Char").newInstance('a');
        AST charB = (AST) constructors.get("Char").newInstance('b');
        AST unionAB = (AST) constructors.get("Union").newInstance(charA, charB);
        AST concatAB = (AST) constructors.get("Concat").newInstance(charA, charB);
        AST starA = (AST) constructors.get("Star").newInstance(charA);
        AST groupA = (AST) constructors.get("Group").newInstance(charA);

        // Char
        NFA nfa = NFA.fromAST(charA);
        assertThat(getNodes(nfa).size()).isEqualTo(2);
        assertThat(getTransitions(getStart(nfa)).get('a')).contains(getEnd(nfa));

        // Union
        nfa = NFA.fromAST(unionAB);
        assertThat(getNodes(nfa).size()).isEqualTo(6);
        assertThat(getTransitions(getStart(nfa)).get(null).size()).isEqualTo(2);
        Object end = getEnd(nfa);
        Object node1 = getTransitions(getStart(nfa)).get(null).toArray()[0];
        Object node2 = getTransitions(getStart(nfa)).get(null).toArray()[1];
        HashMap<Character, HashSet<Object>> transitions1 = getTransitions(node1);
        HashMap<Character, HashSet<Object>> transitions2 = getTransitions(node2);
        if(transitions1.containsKey('a')){
            assertThat(transitions2).containsKey('b');
            assertThat(getTransitions(transitions1.get('a').toArray()[0]).get(null)).contains(end);
            assertThat(getTransitions(transitions2.get('b').toArray()[0]).get(null)).contains(end);
        } else {
            assertThat(transitions1).containsKey('b');
            assertThat(getTransitions(transitions1.get('b').toArray()[0]).get(null)).contains(end);
            assertThat(getTransitions(transitions2.get('a').toArray()[0]).get(null)).contains(end);
        }

        // Concat
        nfa = NFA.fromAST(concatAB);
        end = getEnd(nfa);
        assertThat(getNodes(nfa).size()).isEqualTo(4);
        assertThat(getTransitions(getStart(nfa))).containsKey('a');
        Object node = getTransitions(getStart(nfa)).get('a').toArray()[0];
        assertThat(getTransitions(node)).containsKey(null);
        node = getTransitions(node).get(null).toArray()[0];
        assertThat(getTransitions(node)).containsKey('b');
        assertThat(getTransitions(node).get('b')).contains(end);

        // Star
        nfa = NFA.fromAST(starA);
        end = getEnd(nfa);
        assertThat(getNodes(nfa).size()).isEqualTo(4);
        assertThat(getTransitions(getStart(nfa)).get(null).size()).isEqualTo(2);
        node1 = getTransitions(getStart(nfa)).get(null).toArray()[0];
        node2 = getTransitions(getStart(nfa)).get(null).toArray()[1];
        if(node1.equals(end)){
            assertThat(getTransitions(node2)).containsKey('a');
            Object node3 = getTransitions(node2).get('a').toArray()[0];
            assertThat(getTransitions(node3)).containsKey(null);
            assertThat(getTransitions(node3).get(null)).contains(node2);
            assertThat(getTransitions(node3).get(null)).contains(end);
        } else {
            assertThat(node2).isEqualTo(end);
            Object node3 = getTransitions(node1).get('a').toArray()[0];
            assertThat(getTransitions(node3)).containsKey(null);
            assertThat(getTransitions(node3).get(null)).contains(node2);
            assertThat(getTransitions(node3).get(null)).contains(end);
        }

        // Group
        nfa = NFA.fromAST(groupA);
        assertThat(getNodes(nfa).size()).isEqualTo(2);
        assertThat(getTransitions(getStart(nfa)).get('a')).contains(getEnd(nfa));
    }

    @Test
    void testMatch() {
        NFA nfa = NFA.fromAST(AST.parse("a(b|c)*d"));
        assertThat(nfa.match("ad")).isEqualTo(true);
        assertThat(nfa.match("abd")).isEqualTo(true);
        assertThat(nfa.match("acd")).isEqualTo(true);
        assertThat(nfa.match("abcd")).isEqualTo(true);
        assertThat(nfa.match("abccd")).isEqualTo(true);
        assertThat(nfa.match("abccccccde")).isEqualTo(false);
        assertThat(nfa.match("aabcd")).isEqualTo(false);

        nfa = NFA.fromAST(AST.parse("(ab|c)d*"));
        assertThat(nfa.match("ad")).isEqualTo(false);
        assertThat(nfa.match("abd")).isEqualTo(true);
        assertThat(nfa.match("acd")).isEqualTo(false);
        assertThat(nfa.match("abcd")).isEqualTo(false);
        assertThat(nfa.match("cddddd")).isEqualTo(true);
        assertThat(nfa.match("abccccccde")).isEqualTo(false);
    }
}