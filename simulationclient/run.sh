JAVA_HOME=$(pwd)/jdk-21.0.2

JVM_ARGS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true"

APP_CLASSPATH="-cp .:./lib/*"
MAIN_CLASS=cn.game.simulation.client.ServerTestContext

${JAVA_HOME}/bin/java ${JVM_ARGS}  ${APP_CLASSPATH} ${MAIN_CLASS} "$@"

