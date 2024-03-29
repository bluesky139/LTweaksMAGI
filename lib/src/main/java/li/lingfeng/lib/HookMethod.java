package li.lingfeng.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HookMethod {
    boolean isStatic() default false;
    String cls();
    String method();
    Class returnType() default void.class;
}
