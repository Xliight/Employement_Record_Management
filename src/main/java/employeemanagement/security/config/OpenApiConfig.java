package employeemanagement.security.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;


@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Abdelfettah Mostakir",
                        email = "abdelfattah.mostakir@gmailcom",
                        url = "https://abdelfettahmostakir.vercel.app"
                ),
                description = "Swagger documentation for Employee Management",
                title = "Employee Management - Mostakir",
                version = "1.0",
                termsOfService = "Terms of service"
        )

)
public class OpenApiConfig {

}