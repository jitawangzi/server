
for /f "delims==, tokens=1,2" %%i in (env.txt) do (
	set %%i=%%j
	echo %%i=%%j
)