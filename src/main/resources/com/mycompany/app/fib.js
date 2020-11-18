(function(n) {
    function fib(x) {
        if (x <= 2) {
            return 1;
        }
        let fibX1 = fib(x - 1);
        let fibX2 = fib(x - 2);
        return fibX1 + fibX2;
    }
 
    let fibN = fib(n);
    print(`JavaScript computed that fib(${n}) is ${fibN}`);
 
    return fibN;
})