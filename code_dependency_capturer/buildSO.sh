BASE_HOME=`pwd`
INCLUDES="-I$JAVA_HOME/include -I$JAVA_HOME/include/linux -I~/lianxi/demo"
gcc -c -fPIC $BASE_HOME/agent_util.c $INCLUDES
gcc -c -fPIC $BASE_HOME/sqlite3.c $INCLUDES
g++ -c -fPIC $BASE_HOME/code_dependency_capturer.cpp $INCLUDES

#gcc $BASE_HOME/agent.cpp $INCLUDES -Wall -Wno-deprecated -fPIC --share -o $BASE_HOME/agent.so
#g++ $BASE_HOME/code_dependency_capturer.cpp $BASE_HOME/agent_util.c $BASE_HOME/sqlite3.c $INCLUDES -Wall -Wno-deprecated -fPIC --share -o $BASE_HOME/code_dependency_capturer.so
g++ -shared -fPIC -o  code_dependency_capturer.so agent_util.o sqlite3.o -lstdc++ code_dependency_capturer.o 

