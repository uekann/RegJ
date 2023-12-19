package lib;

import java.util.*;

sealed public class NFA permits DFA{
    private static class Node {
        public HashMap<Character, HashSet<Node>> transitions;
        Node(){
            transitions = new HashMap<>();
        }

        public void addTransition(Character c, Node n){
            if(transitions.containsKey(c)){
                transitions.get(c).add(n);
            } else {
                transitions.put(c, new HashSet<>((List.of(n))));
            }
        }

        public void deleteTransition(Character c, Node n){
            if(transitions.containsKey(c)){
                transitions.get(c).remove(n);
            }
        }
    }

    Node start;
    Node end;
    HashSet<Node> nodes;
    public NFA(){
        start = new Node();
        end = new Node();
        nodes = new HashSet<>();
        nodes.add(start);
        nodes.add(end);
    }

    public static NFA fromAST(AST ast){
        return switch (ast) {
            case AST.Char c -> {
                NFA nfa = new NFA();
                nfa.start.addTransition(c.c, nfa.end);
                yield nfa;
            }
            case AST.Union<?, ?> t -> {
                NFA nfa1 = fromAST(t.t1);
                NFA nfa2 = fromAST(t.t2);
                NFA nfa = new NFA();
                nfa.start.addTransition(null, nfa1.start);
                nfa.start.addTransition(null, nfa2.start);
                nfa1.end.addTransition(null, nfa.end);
                nfa2.end.addTransition(null, nfa.end);
                nfa.nodes.addAll(nfa1.nodes);
                nfa.nodes.addAll(nfa2.nodes);
                yield nfa;
            }
            case AST.Concat<?, ?> t -> {
                NFA nfa1 = fromAST(t.t1);
                NFA nfa2 = fromAST(t.t2);
                NFA nfa = new NFA();
                nfa.nodes.remove(nfa.start);
                nfa.start = nfa1.start;
                nfa1.end.addTransition(null, nfa2.start);
                nfa.nodes.remove(nfa.end);
                nfa.end = nfa2.end;
                nfa.nodes.addAll(nfa1.nodes);
                nfa.nodes.addAll(nfa2.nodes);
                yield nfa;
            }
            case AST.Star<?> t -> {
                NFA nfa1 = fromAST(t.t);
                NFA nfa = new NFA();
                nfa.start.addTransition(null, nfa1.start);
                nfa.start.addTransition(null, nfa.end);
                nfa1.end.addTransition(null, nfa1.start);
                nfa1.end.addTransition(null, nfa.end);
                nfa.nodes.addAll(nfa1.nodes);
                yield nfa;
            }
            case AST.Group<?> t -> fromAST(t.t);
        };
    }

    public boolean match(String s){
        HashSet<Node> current = new HashSet<>((List.of(start)));
        for(int i = 0; i < s.length(); i++){
            HashSet<Node> next = new HashSet<>();
            for(Node n : current){
                if(n.transitions.containsKey(s.charAt(i))){
                    next.addAll(n.transitions.get(s.charAt(i)));
                }
            }
            // イプシロン展開
            HashSet<Node> epsilon = new HashSet<>(next);
            for(int j = 0; j < this.nodes.size(); j++){
//                System.out.println(next.size());
                for (Node n : next) {
                    if (n.transitions.containsKey(null)) {
                        epsilon.addAll(n.transitions.get(null));
                    }
                }
                next.addAll(epsilon);
            }
            current = next;
        }
        return current.contains(end);
    }
}