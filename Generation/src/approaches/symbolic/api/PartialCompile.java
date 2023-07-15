package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.SymbolMapper;

import java.util.Scanner;

import static approaches.symbolic.DescriptionParser.*;

public class PartialCompile {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready");

        SymbolMapper symbolMapper = new CachedMapper();

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            input = input.replace("\\n", "\n");
            PartialCompilation partialCompilation = compilePartialDescription(standardize(input), symbolMapper);
            String compilingPortion = partialCompilation.consistentGames.peek().consistentGame.root().description();
            System.out.println(partialCompilation.exception == null? 1:0);
            System.out.println(compilingPortion.length() / 100.0);
            System.out.println(destandardize(input, compilingPortion).replace("\n", "\\n"));
        }
        sc.close();
    }
}
