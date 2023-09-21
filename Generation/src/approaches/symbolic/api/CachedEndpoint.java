package approaches.symbolic.api;

import approaches.symbolic.CachedMap;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMap;

import java.io.*;
import java.util.Scanner;

import static approaches.symbolic.FractionalCompiler.standardize;

public abstract class CachedEndpoint extends Endpoint {
    SymbolMap symbolMap = new CachedMap();
    FractionalCompiler.CompilationCheckpoint cachedCompilation;
    String standardInput;

    abstract String cachedResponse();

    @Override
    public String respond() {
        standardInput = standardize(rawInput);

        if (cachedCompilation == null || cachedCompilation.longest.isEmpty()) {
            cachedCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);
        } else {
            if (cachedCompilation.longest.size() > 64)
                System.out.println("Warning: " + cachedCompilation.longest.size() + " possible caches found for " + standardInput);

            String cachedDescription = cachedCompilation.longest.get(0).consistentGame.root().description();
            if (!standardInput.equals(cachedDescription)) {
                if (standardInput.startsWith(cachedDescription))
                    cachedCompilation = FractionalCompiler.compileFraction(standardInput, cachedCompilation, symbolMap);
                else
                    cachedCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);
            }
        }

        return cachedResponse();
    }
}
