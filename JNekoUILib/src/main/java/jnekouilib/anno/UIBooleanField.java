package jnekouilib.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface UIBooleanField {
    public String name();
    public UIFieldType type();
    public int readOnly() default 0;
    public String labelText() default "";
}
