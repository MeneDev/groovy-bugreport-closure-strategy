package de.menedev.groovy.bugreport

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.stc.ClosureParams
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized.class)
class ClosureStrategyTest {

    @Parameters(name = "resolves to {1} when using {0} and {2} for Groovy {3}")
    public static Iterable<Object[]> data() {
        [
            ["Closure.DELEGATE_ONLY",   "delegate",  "@CompileDynamic", GroovySystem.version] as Object[],
            ["Closure.DELEGATE_ONLY"  , "delegate" , "@TypeChecked",    GroovySystem.version] as Object[],
            ["Closure.DELEGATE_ONLY"  , "delegate" , "@CompileStatic",  GroovySystem.version] as Object[],
            ["Closure.DELEGATE_FIRST" , "delegate" , "@CompileDynamic", GroovySystem.version] as Object[],
            ["Closure.DELEGATE_FIRST" , "delegate" , "@TypeChecked",    GroovySystem.version] as Object[],
            ["Closure.DELEGATE_FIRST" , "delegate" , "@CompileStatic",  GroovySystem.version] as Object[],
            ["Closure.OWNER_ONLY"     , "owner"    , "@CompileDynamic", GroovySystem.version] as Object[],
            ["Closure.OWNER_ONLY"     , "owner"    , "@TypeChecked",    GroovySystem.version] as Object[],
            ["Closure.OWNER_ONLY"     , "owner"    , "@CompileStatic",  GroovySystem.version] as Object[],
            ["Closure.OWNER_FIRST"    , "owner"    , "@CompileDynamic", GroovySystem.version] as Object[],
            ["Closure.OWNER_FIRST"    , "owner"    , "@TypeChecked",    GroovySystem.version] as Object[],
            ["Closure.OWNER_FIRST"    , "owner"    , "@CompileStatic",  GroovySystem.version] as Object[],
        ]
    }

    String strategy
    Object expected
    String compileAnnotation
    String groovyVersion

    ClosureStrategyTest(String strategy, String expected, String compileAnnotation, String groovyVersion) {
        this.strategy = strategy
        this.expected = expected
        this.compileAnnotation = compileAnnotation
        this.groovyVersion = groovyVersion
    }

    @Test
    void "read property"() {
        def script = """
        class ADelegate {
            def x = "delegate"
        }
        
        $compileAnnotation // @CompileStatic, @CompileDynamic, @TypeChecked
        class AClass {
            public <T> T closureExecuter(
                    ADelegate d,
                    @DelegatesTo(value = ADelegate, strategy = $strategy) Closure<T> c) {
                c.resolveStrategy = $strategy
                c.delegate = d
                return c()
            }
    
            def x = "owner"
            
            def test() {
                def theDelegate = new ADelegate()
                def res = closureExecuter(theDelegate) {
                    return x
                }
                
                return res
            }
        }

        new AClass().test()
        """
        GroovyShell shell = createShell()

        Script s = shell.parse(script)
        def res = s.run()

        assert res == expected
    }

    @Test
    void "write property"() {
        given:
        def script = """
        class ADelegate {
            def x = "delegate"
        }

        $compileAnnotation // @CompileStatic, @CompileDynamic, @TypeChecked
        class AClass {
            public <T> T closureExecuter(
                    ADelegate d,
                    @DelegatesTo(value = ADelegate, strategy = $strategy) Closure<T> c) {
                c.resolveStrategy = $strategy
                c.delegate = d
                return c()
            }

            def x = "owner"

            def test() {
                def theDelegate = new ADelegate()
                closureExecuter(theDelegate) {
                    x = "changed"
                }

                return [theDelegate.x, this.x].toSet()
            }
        }

        new AClass().test()
        """
        GroovyShell shell = createShell()

        when:
        Script s = shell.parse(script)
        def res = s.run()

        then:
        def expectedSet = (["owner", "delegate", "changed"] - [expected]).toSet()
        assert res == expectedSet
    }

    @Test
    void "method call"() {
        given:
        def script = """
        class ADelegate {
            def x() {
                "delegate"
            }
        }

        $compileAnnotation // @CompileStatic, @CompileDynamic, @TypeChecked
        class AClass {
            public <T> T closureExecuter(
                    ADelegate d,
                    @DelegatesTo(value = ADelegate, strategy = $strategy) Closure<T> c) {
                c.resolveStrategy = $strategy
                c.delegate = d
                return c()
            }

            def x() {
                "owner"
            }

            def test() {
                def theDelegate = new ADelegate()
                def res = closureExecuter(theDelegate) {
                    return x()
                }

                return res
            }
        }

        new AClass().test()
        """
        GroovyShell shell = createShell()

        when:
        Script s = shell.parse(script)
        def res = s.run()

        then:
        assert res == expected
    }

    private GroovyShell createShell() {
        def configuration = new CompilerConfiguration()
        def importCustomizer = new ImportCustomizer()
        importCustomizer.addImports(CompileDynamic.canonicalName)
        importCustomizer.addImports(TypeChecked.canonicalName)
        importCustomizer.addImports(CompileStatic.canonicalName)
        importCustomizer.addImports(ClosureParams.canonicalName)
        importCustomizer.addImports(DelegatesTo.canonicalName)

        configuration.addCompilationCustomizers(importCustomizer)
        def shell = new GroovyShell(new Binding(), configuration)
        shell
    }
}
