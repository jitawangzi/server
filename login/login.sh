
# Server Name
ServerName="LoginServer"

#java home directory
#JAVA_HOME="/server/java/jdk-21.0.2"

#jvm arguments
JVM_ARGS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=oom.dump
-Xmx8g
-Xms4g
-Xss256k
-XX:MaxDirectMemorySize=256m
-XX:+UseG1GC 
-XX:+UseCompressedOops 
-XX:+UseCompressedClassPointers
-verbose:gc
-XX:+PrintCommandLineFlags
-XX:+ExplicitGCInvokesConcurrent
-Djava.security.egd=file:/dev/./urandom
-Xlog:gc*,safepoint:logs/gc.log:time,uptime:filecount=100,filesize=50M
--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true
"

#DEBUG="-agentlib:jdwp=transport=dt_socket,address=*:8390,server=y,suspend=n"

APP_CLASSPATH="-cp .:./lib/*:../lib/*"
PROPERTY="-DreceiveMailAccount=shuyongqiang7704@dingtalk.com -Djava.library.path=/usr/local/lib -Djdk.attach.allowAttachSelf=true"
JPROFILER="-agentpath:/server/jprofiler14.0.3/bin/linux-x64/libjprofilerti.so=port=8390,nowait,address=0.0.0.0"
APOLLO_ARGS="-Dapp.id=login -Denv=PROJECTPARTY -Dapollo.cluster=default -Dapollo.meta=http://172.17.34.7:8080"
LOG_PATH="-DSEVER_PATH=."
#main class
MAIN_CLASS="cn.game.login.LoginServer"

#application arguments
#APP_ARGS="resources/spring/applicationContext-loginserver.xml"

start() {
        echo "begin start ${ServerName} server...."
		echo ${APP_ARGS}

nohup ${JAVA_HOME}/bin/java ${DEBUG} ${JVM_ARGS} ${PROPERTY} ${JPROFILER} ${LOG_PATH} ${APOLLO_ARGS} ${APP_CLASSPATH} ${MAIN_CLASS} ${APP_ARGS} >/dev/null 2>out.log &
#${JAVA_HOME}/bin/java ${DEBUG} ${JVM_ARGS} ${PROPERTY} ${LOG_PATH} ${APOLLO_ARGS} ${APP_CLASSPATH} ${MAIN_CLASS} ${APP_ARGS} 
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
