FROM openjdk:11
VOLUME /tmp
EXPOSE 8781
ADD ./target/SistemaCitasEureka-1.jar backend-citas-eureka.jar
ENTRYPOINT ["java","-jar","/backend-citas-eureka.jar"]