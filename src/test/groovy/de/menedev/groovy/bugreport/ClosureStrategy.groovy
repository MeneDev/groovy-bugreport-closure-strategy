package de.menedev.groovy.bugreport

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.stc.ClosureParams
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import spock.lang.Specification
import spock.lang.Unroll

class ClosureStrategy extends Specification {

    @Unroll
    def "Closure resolves to #expected property when reading using #strategy and #compileAnnotation for Groovy #groovyVersion"() {
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
                def res = closureExecuter(theDelegate) {
                    return x
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
        res == expected

        where:
        strategy                 | expected   | compileAnnotation
        "Closure.DELEGATE_ONLY"  | "delegate" | "@CompileDynamic"
        "Closure.DELEGATE_ONLY"  | "delegate" | "@TypeChecked"
        "Closure.DELEGATE_ONLY"  | "delegate" | "@CompileStatic"
        "Closure.DELEGATE_FIRST" | "delegate" | "@CompileDynamic"
        "Closure.DELEGATE_FIRST" | "delegate" | "@TypeChecked"
        "Closure.DELEGATE_FIRST" | "delegate" | "@CompileStatic"
        "Closure.OWNER_ONLY"     | "owner"    | "@CompileDynamic"
        "Closure.OWNER_ONLY"     | "owner"    | "@TypeChecked"
        "Closure.OWNER_ONLY"     | "owner"    | "@CompileStatic"
        "Closure.OWNER_FIRST"    | "owner"    | "@CompileDynamic"
        "Closure.OWNER_FIRST"    | "owner"    | "@TypeChecked"
        "Closure.OWNER_FIRST"    | "owner"    | "@CompileStatic"

        groovyVersion = GroovySystem.version
    }

    @Unroll
    def "Closure resolves to #desc property when writing using #strategy and #compileAnnotation for Groovy #groovyVersion"() {
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
        res == expected.toSet()

        where:
        strategy                 | desc       | expected                | compileAnnotation
        "Closure.DELEGATE_ONLY"  | "delegate" | ["owner", "changed"]    | "@CompileDynamic"
        "Closure.DELEGATE_ONLY"  | "delegate" | ["owner", "changed"]    | "@TypeChecked"
        "Closure.DELEGATE_ONLY"  | "delegate" | ["owner", "changed"]    | "@CompileStatic"
        "Closure.DELEGATE_FIRST" | "delegate" | ["owner", "changed"]    | "@CompileDynamic"
        "Closure.DELEGATE_FIRST" | "delegate" | ["owner", "changed"]    | "@TypeChecked"
        "Closure.DELEGATE_FIRST" | "delegate" | ["owner", "changed"]    | "@CompileStatic"
        "Closure.OWNER_ONLY"     | "owner"    | ["delegate", "changed"] | "@CompileDynamic"
        "Closure.OWNER_ONLY"     | "owner"    | ["delegate", "changed"] | "@TypeChecked"
        "Closure.OWNER_ONLY"     | "owner"    | ["delegate", "changed"] | "@CompileStatic"
        "Closure.OWNER_FIRST"    | "owner"    | ["delegate", "changed"] | "@CompileDynamic"
        "Closure.OWNER_FIRST"    | "owner"    | ["delegate", "changed"] | "@TypeChecked"
        "Closure.OWNER_FIRST"    | "owner"    | ["delegate", "changed"] | "@CompileStatic"

        groovyVersion = GroovySystem.version
    }

    @Unroll
    def "Closure resolves to #expected method when using #strategy and #compileAnnotation for Groovy #groovyVersion"() {
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
        res == expected

        where:
        strategy                 | expected   | compileAnnotation
        "Closure.DELEGATE_ONLY"  | "delegate" | "@CompileDynamic"
        "Closure.DELEGATE_ONLY"  | "delegate" | "@TypeChecked"
        "Closure.DELEGATE_ONLY"  | "delegate" | "@CompileStatic"
        "Closure.DELEGATE_FIRST" | "delegate" | "@CompileDynamic"
        "Closure.DELEGATE_FIRST" | "delegate" | "@TypeChecked"
        "Closure.DELEGATE_FIRST" | "delegate" | "@CompileStatic"
        "Closure.OWNER_ONLY"     | "owner"    | "@CompileDynamic"
        "Closure.OWNER_ONLY"     | "owner"    | "@TypeChecked"
        "Closure.OWNER_ONLY"     | "owner"    | "@CompileStatic"
        "Closure.OWNER_FIRST"    | "owner"    | "@CompileDynamic"
        "Closure.OWNER_FIRST"    | "owner"    | "@TypeChecked"
        "Closure.OWNER_FIRST"    | "owner"    | "@CompileStatic"

        groovyVersion = GroovySystem.version
    }

    private GroovyShell createShell() {
        def configuration = new CompilerConfiguration()
        def importCustomizer = new ImportCustomizer()
        importCustomizer.addImports(CompileDynamic.canonicalName)
        importCustomizer.addImports(TypeChecked.canonicalName)
        importCustomizer.addImports(CompileStatic.canonicalName)
        importCustomizer.addImports(Specification.canonicalName)
        importCustomizer.addImports(ClosureParams.canonicalName)
        importCustomizer.addImports(DelegatesTo.canonicalName)

        configuration.addCompilationCustomizers(importCustomizer)
        def shell = new GroovyShell(new Binding(), configuration)
        shell
    }
}
