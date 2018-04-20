package jnekouilib.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface UICollection {
    public String name();
    public UIFieldType type();
    public int multiSelect() default 0;
    public int yesNoBoxPresent() default 1;
    public String text() default "";
}
