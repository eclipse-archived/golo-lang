
module golo.test.AnnotationMacros

&use("gololang.meta.Annotations")

&annotationWrapper(golo.test.annotations.OnMethod.class)
&annotationWrapper(golo.test.annotations.OnClass.class)
&annotationWrapper(golo.test.annotations.OnClassInherit.class)
&annotationWrapper(golo.test.annotations.OnBoth.class)
&annotationWrapper(golo.test.annotations.OnAll.class)
&annotationWrapper(golo.test.annotations.OnAll.class, "OnMultiple")
&annotationWrapper(golo.test.annotations.WithIntArg.class)
&annotationWrapper(golo.test.annotations.WithNamedArg.class)
&annotationWrapper(golo.test.annotations.WithArrayArg.class)
&annotationWrapper(golo.test.annotations.WithEnumArg.class)
&annotationWrapper(golo.test.annotations.Complex.class, "Complex", "Documentation of the complex macro")

