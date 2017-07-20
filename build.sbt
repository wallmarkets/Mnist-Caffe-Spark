scalaVersion := "2.11.0"

externalResolvers := Seq(Resolver.mavenLocal, Resolver.defaultLocal, Resolver.jcenterRepo)

//javaCppPresetLibs ++= Seq("caffe" -> "master", "opencv" -> "3.2.0", "tensorflow" -> "1.0.0")
libraryDependencies ++= Seq(
    "org.bytedeco" % "javacpp" % "1.2.7",
    "org.bytedeco.javacpp-presets" % "caffe" % "rc3-1.2-CPUONLY",
    "org.bytedeco.javacpp-presets" % "caffe" % "rc3-1.2-CPUONLY" classifier "linux-x86_64",
    "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2",
    "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2" classifier "linux-x86_64",    
    "org.apache.spark" %% "spark-sql" % "2.1.0" % "provided",
    "org.apache.spark" %% "spark-core" % "2.1.0" % "provided"
)

test in assembly := {}
