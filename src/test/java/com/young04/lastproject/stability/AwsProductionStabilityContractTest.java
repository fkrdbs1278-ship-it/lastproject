package com.young04.lastproject.stability;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AwsProductionStabilityContractTest {

    @Test
    void securityConfigDoesNotPermitEveryUnknownRequest() throws IOException {
        String source = read(
                "src/main/java/com/young04/lastproject/global/security/SecurityConfig.java"
        );

        assertThat(source)
                .doesNotContain(".anyRequest()\n                        .permitAll()")
                .contains(".anyRequest()\n                        .authenticated()")
                .contains("/actuator/health")
                .contains("/member/payments")
                .contains("/testcompany/**");
    }

    @Test
    void testControllersAreDisabledInProdProfile() throws IOException {
        String supplierController = read(
                "src/main/java/com/young04/lastproject/testcompany/controller/TestCompanyController.java"
        );

        String customerTestController = read(
                "src/main/java/com/young04/lastproject/treatmenthistory/controller/CustomerUserTestController.java"
        );

        assertThat(supplierController)
                .contains("@Profile(\"!prod\")");

        assertThat(customerTestController)
                .contains("@Profile(\"!prod\")");
    }

    @Test
    void productionConfigurationHasAwsSafeDefaults() throws IOException {
        String prodConfig = read(
                "src/main/resources/application-prod.yaml"
        );

        assertThat(prodConfig)
                .contains("forward-headers-strategy: framework")
                .contains("shutdown: graceful")
                .contains("secure: true")
                .contains("include: health")
                .contains("show-details: never");
    }

    @Test
    void siteAdminUploadPathIsExternalizable() throws IOException {
        String appConfig = read(
                "src/main/resources/application.yaml"
        );

        String webConfig = read(
                "src/main/java/com/young04/lastproject/global/config/SiteAdminWebConfig.java"
        );

        String service = read(
                "src/main/java/com/young04/lastproject/siteadmin/service/SiteSettingService.java"
        );

        assertThat(appConfig)
                .contains("siteadmin-upload-dir: ${SITEADMIN_UPLOAD_DIR:siteadmin-upload}");

        assertThat(webConfig)
                .contains("${file.siteadmin-upload-dir:siteadmin-upload}");

        assertThat(service)
                .contains("${file.siteadmin-upload-dir:siteadmin-upload}");
    }

    private String read(
            String relativePath
    ) throws IOException {
        return Files.readString(
                Path.of(relativePath)
        );
    }
}
