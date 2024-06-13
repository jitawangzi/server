set project=util

set "file=%workspace%\%project%\pom.xml"
:: mvn clean install
if defined clean (mvn -f %file% clean install ) else ( mvn -f %file% install)
