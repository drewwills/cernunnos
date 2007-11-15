#!/bin/sh

## Copyright 2007 Andrew Wills
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##
## Establish the classpath & script we expext to use...

CRN_CLASSPATH=$CRN_HOME/build:$CRN_HOME/config

for f in $(ls $CRN_HOME/lib/*.jar); do
    CRN_CLASSPATH=${CRN_CLASSPATH}:$f
done

##
CRN_ARGS=$*

java -cp "$CRN_CLASSPATH" org.danann.cernunnos.runtime.Main $CRN_ARGS