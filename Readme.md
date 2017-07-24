# MNIST with Caffe on Spark

## compile
- it requires a CPU-only caffe of version 1.0, which is already available on dev003
    - or you can build your own one,
        - `git clone https://git.mineway.de/jwu/javacpp-presets`
        - `git checkout 1.3.3-Caffe-CPUONLY`
        - `mvn install --projects .,hdf5,opencv,openblas,caffe`
- compile by `sbt assembly`

## execution
+ `export MNIST_HOME=<your-path-to-this-repo>`
+ `/opt/spark-2.1.0/bin/spark-submit --master spark://dev003:27077 --driver-memory=4G --num-executors 6 --executor-cores 2 --class MnistApp target/scala-2.11/mnist-assembly-0.1-SNAPSHOT.jar 6`