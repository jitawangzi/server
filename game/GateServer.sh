# Server Name
ServerName="GateServer"

#java home directory
#JAVA_HOME="/www/bin/jdk1.6"

#jvm arguments

JVM_ARGS="-server -Xmx3g -Xms3g -Xmn1g -XX:PermSize=64m -XX:MaxPermSize=64m -XX:SurvivorRatio=6\
 -Xnoclassgc -XX:+DisableExplicitGC -XX:+PrintTenuringDistribution\
 -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+UseParallelOldGC\
 -XX:+PrintClassHistogram -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Xloggc:logs/gc.log "

#class path
CLASSPAHT="-cp .:../lib/*"

#main class
MAIN_CLASS="net.game.GateServer "

#application arguments
APP_ARGS="res/applicationContext-gatewayserver.xml"

start() {
        echo "begin start ${ServerName} server...."
		echo ${APP_ARGS}
        nohup java ${CLASSPAHT} ${MAIN_CLASS} ${APP_ARGS} /dev/null 2>&1 &
        echo $!>../${ServerName}.server
        return
        }

stop() {
   if [ -f ../${ServerName}.server ]; then
      pid=`cat ../${ServerName}.server`
      kill -15 $pid
      echo "kill ${ServerName} Server ${pid} successfull!"
    else
      echo "kill ${ServerName} Server fail, ${ServerName}.server not exist";
    fi
    rm -f ${ServerName}.server
    return
      }

restart() {
    echo "begin restart robot"
    stop
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
   if [ -f ../${ServerName}.server ] ; then
    pid=`cat ../${ServerName}.server`
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
