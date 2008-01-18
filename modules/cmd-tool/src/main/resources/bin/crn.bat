@echo off
rem Copyright 2007 Andrew Wills
rem 
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem 
rem     http://www.apache.org/licenses/LICENSE-2.0
rem 
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem Establish the classpath...
set CRN_CLASSPATH=%CRN_HOME%\build;%CRN_HOME%\config
for %%f in ("%CRN_HOME%\lib\*.jar") do call add-jar %%f

rem (Re)Marshall the command line args...
set CRN_ARGS=
:nextArg
if ""%1""=="""" goto invoke
set CRN_ARGS=%CRN_ARGS% %1
shift
goto nextArg

:invoke
@echo on
java -cp "%CRN_CLASSPATH%" org.danann.cernunnos.runtime.Main %CRN_ARGS%