FROM openjdk:17-alpine
WORKDIR /app
COPY target/Employee_Management.jar /app/Employee_Management.jar
EXPOSE 8080
ENTRYPOINT  ["java", "-jar", "Employee_Management.jar"]