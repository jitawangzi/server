::打包命令，一般不会单独使用 

set project=gm

set "file=%workspace%\..\%project%\pom.xml"
mvn -f %file% clean package