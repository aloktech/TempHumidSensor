cls
mvn clean install -DskipTests=true && mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=3b43c7c555cb71624c71958064d9f578ebe57173
