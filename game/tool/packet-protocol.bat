c:
cd %workspace%\protocol
:: mvn clean install
if defined clean (mvn clean install ) else ( mvn install)

