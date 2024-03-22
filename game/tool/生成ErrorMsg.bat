
cd /D %metafolder%\dic_generate

java -cp .;lib/*;Gen.jar  x.dic.java.JavaMaker


xcopy /Y /Q %metafolder%\output\java\enum\ErrorMsgEnum.java %workspace%\protocol\src\main\java\cn\game\manual