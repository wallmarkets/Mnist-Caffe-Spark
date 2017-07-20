# MNIST with Caffe on Spark

## compile
- it requires a CPU-only caffe of version rc3, which is already available on dev003
    - or you can build your one from the repo here: https://git.mineway.de/jwu/javacpp-presets/tree/1.2-Caffe-CPUONLY
- compile by `sbt assembly`

## execution
`/opt/spark-2.1.0/bin/spark-submit --master spark://dev003:27077 --driver-memory=4G --num-executors 6 --executor-cores 2 --class MnistApp target/scala-2.11/mnist-assembly-0.1-SNAPSHOT.jar 6`