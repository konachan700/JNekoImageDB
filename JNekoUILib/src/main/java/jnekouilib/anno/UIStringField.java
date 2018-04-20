package jnekouilib.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface UIStringField {
    public String name();
    public UIFieldType type();
    public int readOnly() default 0;
    public int maxChars() default 255;
    public String helpText() default "";
    public String labelText() default "";
}
