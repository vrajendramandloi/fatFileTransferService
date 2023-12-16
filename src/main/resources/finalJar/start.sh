export JAVA_HOME=""
export JDK_HOME=""
export JRE_HOME=""

cd /tmp/vrajendra
java -jar fatFileTransferService*.jar "test.log;temp.log" --server.port=7711 &
 