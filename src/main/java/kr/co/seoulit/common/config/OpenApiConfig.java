package kr.co.seoulit.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "접수 백엔드 API 문서",
                description = "외래/응급/입원/예약 접수 API 명세",
                version = "v1"
        ),
        servers = @Server(url = "/", description = "현재 서버")
)
public class OpenApiConfig {
}
