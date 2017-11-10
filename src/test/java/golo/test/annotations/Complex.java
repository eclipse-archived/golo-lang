package golo.test.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Complex {
  Values[] vals() default {Values.FIRST, Values.OTHER};

  Class<?>[] cls();

}


