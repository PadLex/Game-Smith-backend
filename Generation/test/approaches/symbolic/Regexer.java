package approaches.symbolic;

import grammar.Grammar;
import main.StringRoutines;
import main.grammar.Symbol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Regexer {
    public static void main(String[] args) {
        List<Symbol> symbols = Grammar.grammar().symbols();
        Set<String> classes = new HashSet<>();
        Set<String> enums = new HashSet<>();
        for (Symbol s : symbols) {
            if (s.usedInGrammar()) {
                if (s.cls().isEnum()) {
                    if (!s.cls().getTypeName().equals(s.path()))
                        enums.add(s.token());
                } else {
                    classes.add(s.token());
                    if (s.hasAlias())
                        classes.add(StringRoutines.toDromedaryCase(s.name()));
                }
            }
        }

        System.out.println(enums.stream().reduce((a, b) -> a + "|" + b).orElse(""));
        System.out.println(classes.stream().reduce((a, b) -> a + "|" + b).orElse(""));
    }
}
