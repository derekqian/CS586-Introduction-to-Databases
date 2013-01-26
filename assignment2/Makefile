all : 
	javac setdb.java
	javac hw2jdbc.java

setup :
	java -classpath postgresql-9.2-1002.jdbc4.jar: setdb

run :
	java -classpath postgresql-9.2-1002.jdbc4.jar: hw2jdbc

clean :
	rm *.class
