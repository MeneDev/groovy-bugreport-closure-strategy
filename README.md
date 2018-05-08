# groovy-bugreport-closure-strategy

Small project demonstrating a Groovy irregularity. The bug report is in the [Apache Jira](https://issues.apache.org/jira/browse/GROOVY-8562).

Closures resolve to owner property although `DELEGATE_FIRST`
or `DELEGATE_ONLY` are set as `resolveStrategy`.
This only happens when `@CompileStatic` is used.
This does not apply to method calls.
For Groovy 2.3.11 this only affects read operations.

The versions 3.0.0-alpha-2, 2.6.0-alpha-3, 2.5.0-rc-1, 2.4.15, 2.3.11 have been tested.
I will add tests for newer versions, but this requires a different
test framework than Spock.

The below code illustrates to problem:

```groovy
class ADelegate {
    def x = "delegate"
}

@CompileStatic
class AClass {
    public <T> T closureExecuter(
            ADelegate d,
            @DelegatesTo(value = ADelegate, strategy = DELEGATE_ONLY) Closure<T> c) {
        c.resolveStrategy = DELEGATE_ONLY
        c.delegate = d
        return c()
    }

    def x = "owner"
    
    def test() {
        def theDelegate = new ADelegate()
        def res = closureExecuter(theDelegate) {
            return x
        }
        
        // is "owner" instead of "delegate"
        return res
    }
}
```