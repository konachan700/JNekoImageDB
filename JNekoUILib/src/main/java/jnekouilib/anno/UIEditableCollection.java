package jnekouilib.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface UIEditableCollection {
    public String name();
    public UIFieldType type();
    public String title() default "";
    public String text() default "";
}
