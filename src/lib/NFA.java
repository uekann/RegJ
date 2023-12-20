package lib;

import java.util.*;

sealed public class NFA permits DFA{
    private static class Node {
        HashMap<Character, HashSet<Node>> transitionsTo;
        HashMap<Character, HashSet<Node>> transitionsFrom;
        int id;
        Node(){
            transitionsTo = new HashMap<>();
            transitionsFrom = new HashMap<>();
            id = 0;
        }

        public void addTransition(Character c, Node n){
            if(transitionsTo.containsKey(c)){
                transitionsTo.get(c).add(n);
            } else {
                transitionsTo.put(c, new HashSet<>((List.of(n))));
            }
            if(n.transitionsFrom.containsKey(c)){
                n.transitionsFrom.get(c).add(this);
            } else {
                n.transitionsFrom.put(c, new HashSet<>((List.of(this))));
            }
        }

        public void deleteTransition(Character c, Node n){
            if(transitionsTo.containsKey(c)){
                transitionsTo.get(c).remove(n);
            }
            if(n.transitionsFrom.containsKey(c)){
                n.transitionsFrom.get(c).remove(this);
            }
        }
    }

    Node start;
    Node end;
    HashMap<Integer ,Node> nodes;
    public NFA(){
        start = new Node();
        start.id = 0;
        end = new Node();
        end.id = 1;
        nodes = new HashMap<>();
        nodes.put(0, start);
        nodes.put(1, end);
    }

    private static HashMap<Integer, Node> _concatNodes(HashMap<Integer, Node>... nodesList){
        HashMap<Integer, Node> nodes = new HashMap<>();
        HashSet<Node> values = new HashSet<>();
        for(HashMap<Integer, Node> nodesMap : nodesList){
            values.addAll(nodesMap.values());
        }
        List<Node> valuesList = new ArrayList<>(values);
        for(int i = 0; i < values.size(); i++){
            nodes.put(i, valuesList.get(i));
            valuesList.get(i).id = i;
        }
        return nodes;
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
                nfa.nodes = _concatNodes(nfa.nodes, nfa1.nodes, nfa2.nodes);
                yield nfa;
            }
            case AST.Concat<?, ?> t -> {
                NFA nfa1 = fromAST(t.t1);
                NFA nfa2 = fromAST(t.t2);
                nfa1.end.addTransition(null, nfa2.start);
                NFA nfa = new NFA();
                nfa.start = nfa1.start;
                nfa.end = nfa2.end;
                nfa.nodes = _concatNodes(nfa1.nodes, nfa2.nodes);
                yield nfa;
            }
            case AST.Star<?> t -> {
                NFA nfa1 = fromAST(t.t);
                NFA nfa = new NFA();
                nfa.start.addTransition(null, nfa1.start);
                nfa.start.addTransition(null, nfa.end);
                nfa1.end.addTransition(null, nfa1.start);
                nfa1.end.addTransition(null, nfa.end);
                nfa.nodes = _concatNodes(nfa.nodes, nfa1.nodes);
                yield nfa;
            }
            case AST.Group<?> t -> fromAST(t.t);
        };
    }

    private HashSet<Node> _epsilonClosure(HashSet<Node> nodes){
        nodes = new HashSet<>(nodes);
        HashSet<Node> epsilon = new HashSet<>(nodes);
        for(int i = 0; i < this.nodes.size(); i++){
            for(Node n : nodes){
                if(n.transitionsTo.containsKey(null)){
                    epsilon.addAll(n.transitionsTo.get(null));
                }
            }
            nodes.addAll(epsilon);
        }
        return nodes;
    }

    public boolean match(String s){
        HashSet<Node> current = new HashSet<>((List.of(start)));
        for(int i = 0; i < s.length(); i++){
            current = _epsilonClosure(current);
            HashSet<Node> next = new HashSet<>();
            for(Node n : current){
                if(n.transitionsTo.containsKey(s.charAt(i))){
                    next.addAll(n.transitionsTo.get(s.charAt(i)));
                }
            }
            current = next;
        }
        current = _epsilonClosure(current);
        return current.contains(end);
    }

    public void removeEpsilon(){
        for(Node n: nodes.values()){
            if(!n.transitionsTo.containsKey(null)){
                continue;
            }
            HashSet<Node> epsilon = new HashSet<>(n.transitionsTo.get(null));
            epsilon = _epsilonClosure(epsilon);
            for(Node m : epsilon){
                if(m == n){
                    continue;
                }
                for(Character c : m.transitionsTo.keySet()){
                    if(c == null){
                        continue;
                    }
                    for(Node o : m.transitionsTo.get(c)){
                        n.addTransition(c, o);
                    }
                }
            }
        }
        for(Node n : nodes.values()){
            if(!(n == start || n == end)){
                n.transitionsTo.remove(null);
                n.transitionsFrom.remove(null);
            }
        }
    }
}