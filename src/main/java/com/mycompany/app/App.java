package com.mycompany.app;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import java.io.IOException;
import org.graalvm.polyglot.Source;

/**
 * Simple benchmark for Graal.js via GraalVM Polyglot Context and ScriptEngine.
 */
public class App {

    public static final int WARMUP = 10;
    public static final int ITERATIONS = 10;
    public static final String BENCHFILE = "src/bench.js";

    public static final String SOURCE = ""
            + "var N = 2000;\n"
            + "var EXPECTED = 17393;\n"
            + "\n"
            + "class Natural {\n"
            + "  constructor() {\n"
            + "    this.x = 2;\n"
            + "  }\n"
            + "  next() { return this.x++; }\n"
            + "}\n"
            + "\n"
            + "class Filter {\n"
            + "  constructor (number, filter) {\n"
            + "    this.number = number;\n"
            + "    this.filter = filter;\n"
            + "  }\n"
            + "  accept(n) {\n"
            + "      var filter = this;\n"
            + "      for (;;) {\n"
            + "          if (n % filter.number === 0) {\n"
            + "              return false;\n"
            + "          }\n"
            + "          filter = filter.filter;\n"
            + "          if (filter === null) {\n"
            + "              break;\n"
            + "          }\n"
            + "      }\n"
            + "      return true;\n"
            + "  }\n"
            + "}\n"
            + "\n"
            + "class Primes {\n"
            + "  constructor (natural) {\n"
            + "    this.natural = natural;\n"
            + "    this.filter = null;\n"
            + "  }\n"
            + "  next() {\n"
            + "        for (;;) {\n"
            + "            var n = this.natural.next();\n"
            + "            if (this.filter === null || this.filter.accept(n)) {\n"
            + "                this.filter = new Filter(n, this.filter);\n"
            + "                return n;\n"
            + "            }\n"
            + "        }\n"
            + "  }\n"
            + "}\n"
            + "\n"
            + "function primesMain() {\n"
            + "    var primes = new Primes(new Natural());\n"
            + "    var primArray = [];\n"
            + "    for (var i=0;i<=N;i++) { primArray.push(primes.next()); }\n"
            + "    if (primArray[N] != EXPECTED) { throw new Error('wrong prime found: '+primArray[N]); }\n"
            + "}\n";

    public static void main(String[] args) throws Exception {
        benchGraalPolyglotContext();
        benchGraalScriptEngine();
        benchNashornScriptEngine();
    }

    static long benchGraalPolyglotContext() throws IOException {
        System.out.println("=== Graal.js via org.graalvm.polyglot.Context === ");
        long took = 0;
        try (Context context = Context.create()) {
            context.eval(Source.newBuilder("js", SOURCE, "src.js").build());
            Value primesMain = context.getBindings("js").getMember("primesMain");
            System.out.println("warming up ...");
            for (int i = 0; i < WARMUP; i++) {
                primesMain.execute();
            }
            System.out.println("warmup finished, now measuring");
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.currentTimeMillis();
                primesMain.execute();
                took = System.currentTimeMillis() - start;
                System.out.println("iteration: " + took);
            }
        } // context.close() is automatic
        return took;
    }

    static long benchNashornScriptEngine() throws IOException {
        System.out.println("=== Nashorn via javax.script.ScriptEngine ===");
        ScriptEngine nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
        if (nashornEngine == null) {
            System.out.println("*** Nashorn not found ***");
            return Long.MAX_VALUE;
        } else {
            if (SOURCE.contains("class ")) {
                System.out.println("*** Nashorn doesn't support ES6");
                return Long.MAX_VALUE;
            }
            return benchScriptEngineIntl(nashornEngine);
        }
    }

    static long benchGraalScriptEngine() throws IOException {
        System.out.println("=== Graal.js via javax.script.ScriptEngine ===");
        ScriptEngine graaljsEngine = new ScriptEngineManager().getEngineByName("graal.js");
        if (graaljsEngine == null) {
            System.out.println("*** Graal.js not found ***");
            return 0;
        } else {
            return benchScriptEngineIntl(graaljsEngine);
        }
    }

    private static long benchScriptEngineIntl(ScriptEngine eng) throws IOException {
        long took = 0L;
        try {
            eng.eval(SOURCE);
            Invocable inv = (Invocable) eng;
            System.out.println("warming up ...");
            for (int i = 0; i < WARMUP; i++) {
                inv.invokeFunction("primesMain");
            }
            System.out.println("warmup finished, now measuring");
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.currentTimeMillis();
                inv.invokeFunction("primesMain");
                took = System.currentTimeMillis() - start;
                System.out.println("iteration: " + (took));
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return took;
    }

}
