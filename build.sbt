scalaVersion := "2.11.0"

externalResolvers := Seq(Resolver.mavenLocal, Resolver.defaultLocal, Resolver.jcenterRepo)

libraryDependencies ++= Seq(
    "org.bytedeco" % "javacpp" % "1.3.3-SNAPSHOT",
    "org.bytedeco.javacpp-presets" % "caffe" % "1.0-CPUONLY-1.3.3-SNAPSHOT",
    "org.bytedeco.javacpp-presets" % "caffe" % "1.0-CPUONLY-1.3.3-SNAPSHOT" classifier "linux-x86_64",
    "org.bytedeco.javacpp-presets" % "opencv" % "3.2.0-1.3.3-SNAPSHOT",
    "org.bytedeco.javacpp-presets" % "opencv" % "3.2.0-1.3.3-SNAPSHOT" classifier "linux-x86_64",    
    "org.bytedeco.javacpp-presets" % "openblas" % "0.2.19-1.3.3-SNAPSHOT",
    "org.bytedeco.javacpp-presets" % "openblas" % "0.2.19-1.3.3-SNAPSHOT" classifier "linux-x86_64",    
    "org.bytedeco.javacpp-presets" % "hdf5" % "1.10.1-1.3.3-SNAPSHOT",
    "org.bytedeco.javacpp-presets" % "hdf5" % "1.10.1-1.3.3-SNAPSHOT" classifier "linux-x86_64",    
    "org.nd4j" % "nd4j-native-platform" % "0.8.0",
    "org.nd4j" %% "nd4s" % "0.8.0",
    "org.apache.spark" %% "spark-sql" % "2.1.0" % "provided",
    "org.apache.spark" %% "spark-core" % "2.1.0" % "provided"
)

test in assembly := {}
