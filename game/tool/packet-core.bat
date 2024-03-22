
cd /D %workspace%\core
:: mvn clean install
if defined clean (mvn clean install ) else ( mvn install)

