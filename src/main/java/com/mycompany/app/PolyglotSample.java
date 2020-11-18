package  com.mycompany.app;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import java.net.URL;

public class PolyglotSample {
    public static void main(String[] args) throws java.io.IOException, java.lang.InterruptedException {
        Context ctx = Context.create();
        URL fibUrl = PolyglotSample.class.getResource("fib.js");
        Source fibSrc = Source.newBuilder("js", fibUrl).build();
        Value fib = ctx.eval(fibSrc);
        for (int i = 0; i < 10; i++) {
            Value res = fib.execute(i);
            System.out.println("Polyglot with Graal.JS - fib(" + i + ") = " + res.asInt());
            Thread.sleep(500);
        }
    }
}    
    
