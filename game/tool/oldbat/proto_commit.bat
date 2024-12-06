:: 协议修改后，提交到svn供客户端下载,这个工程不用了，不用svn

cd /D %metafolder%\protos

svn revert * -R

svn update

xcopy /Y /Q %workspace%\protocol\src\main\java\cn\game\protocol\protos %metafolder%\protos

xcopy /Y /Q d:\ProtosMessageID.ts %metafolder%\protos\output

xcopy /Y /Q d:\ProtosMessageName.ts %metafolder%\protos\output

xcopy /Y /Q d:\protos.d.ts %metafolder%\protos\output

xcopy /Y /Q d:\ProtosEnum.ts %metafolder%\protos\output


svn add . --no-ignore --force
svn add .\output --no-ignore --force

@echo off
set commitDesc="commit proto file"
set /p commitDesc=Write a log message, or press Enter:

svn ci -m %commitDesc%

pause
