
# Server Name
ServerName="GameServer"

#java home directory
#JAVA_HOME="/server/java/jdk-21.0.2"

#jvm arguments

JVM_ARGS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=oom.dump
-Xmx4g
-Xms256m
-Xss256k
-XX:MaxDirectMemorySize=256m
-XX:+UseG1GC 
-XX:+UseCompressedOops 
-XX:+UseCompressedClassPointers
-XX:+SegmentedCodeCache 
-verbose:gc
-XX:+PrintCommandLineFlags
-XX:+ExplicitGCInvokesConcurrent
-Djava.security.egd=file:/dev/./urandom
-Xlog:gc*,safepoint:logs/gc.log:time,uptime:filecount=100,filesize=50M
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true"

#DEBUG="-agentlib:jdwp=transport=dt_socket,address=*:8011,server=y,suspend=n"
#PROFILER="-agentpath:/game/jprofiler/bin/linux-x64/libjprofilerti.so=port=8849,nowait"

APP_CLASSPATH="-cp .:./resources:./lib/*:../lib/*"
PROPERTY="-DreceiveMailAccount=shuyongqiang7704@dingtalk.com -Djava.library.path=/usr/local/lib -Djdk.attach.allowAttachSelf=true"
JPROFILER="-agentpath:/server/jprofiler14.0.3/bin/linux-x64/libjprofilerti.so=port=8011,nowait,address=0.0.0.0"
APOLLO_ARGS="-Dapp.id=game -Denv=PROJECTPARTY -Dapollo.cluster=default -Dapollo.meta=http://172.17.34.7:8080"
#RUN_MODULE="-Dserver.run.mode=test"
#LOG_PATH="-DSEVER_PATH=./logs"
#main class
MAIN_CLASS=cn.game.games.net.game.GameServer

#application arguments
#APP_ARGS="resources/applicationContext-dataserver.xml resources/applicationContext-gameserver.xml"
APP_ARGS="hc_game_1"

start() {
        echo "begin start ${ServerName} server...."
		echo ${APP_ARGS}
 #       nohup ${JAVA_HOME}/bin/java ${DEBUG} ${PROFILER} ${JVM_ARGS} ${JPROFILER} ${RUN_MODULE} ${LOG_PATH} ${PROPERTY} ${APOLLO_ARGS} ${APP_CLASSPATH} ${MAIN_CLASS} ${APP_ARGS} > out.log &
        nohup ${JAVA_HOME}/bin/java ${DEBUG} ${PROFILER} ${JVM_ARGS} ${JPROFILER} ${RUN_MODULE} ${LOG_PATH} ${PROPERTY} ${APOLLO_ARGS} ${APP_CLASSPATH} ${MAIN_CLASS} ${APP_ARGS} >/dev/null 2>logs/out.log &
        echo $!>${ServerName}.server
        return
        }

stop() {
   if [ -f ${ServerName}.server ]; then
      pid=`cat ${ServerName}.server`
      kill -15 $pid
      echo "kill ${ServerName} Server ${pid} successfull!"
    else
      echo "kill ${ServerName} Server fail, ${ServerName}.server not exist";
    fi
    return
      }

restart() {
    echo "begin restart robot"
    stop
   sleep 35
    start
    return
   }
 
help() {
    echo "You must input a argument start,stop,restart,status or help"
    echo "start: run the server"
    echo "stop: stop the server"
    echo "restart: restart the server"
    echo "status: show the status of the server"
    echo "help : show the help infomation"
}

status() {
   if [ -f ${ServerName}.server ] ; then
    pid=`cat ${ServerName}.server`
    echo "current ${ServerName} is ${pid}"
    echo "${ServerName} Server is running..."
   else
    echo "${ServerName} Server is stop!"  
   fi
}

if [ $# -eq 0 ]; then
   help
else
  echo "you input command is $1"
  case $1 in

 "start" ) 
          start
          ;; 
 "stop" )
          stop 
          ;; 
 "restart" )
          restart
          ;; 
 "status" )
          status
          ;;
 esac
fi
