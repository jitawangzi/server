c:
cd %workspace%\core
:: mvn clean install
if defined clean (mvn clean install ) else ( mvn install)

