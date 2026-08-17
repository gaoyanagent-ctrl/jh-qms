package com.company.iaf.mdm;

import com.company.iaf.mdm.application.MdmApplicationService;
import com.company.iaf.mdm.application.MdmExcelImportService;
import com.company.iaf.mdm.infrastructure.persistence.JdbcMdmRepository;
import com.company.iaf.mdm.interfaces.controller.MdmController;
import com.company.iaf.mdm.domain.repository.MdmImportObjectStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MdmModuleRegistrationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withBean(MdmImportObjectStorage.class, () -> mock(MdmImportObjectStorage.class))
            .withUserConfiguration(JdbcMdmRepository.class, MdmApplicationService.class, MdmExcelImportService.class, MdmController.class);

    @Test void registersMdmHttpChainByDefault() {
        runner.run(context -> assertThat(context).hasSingleBean(MdmController.class));
    }

    @Test void canDisableMdmForDatabaseFreeContexts() {
        runner.withPropertyValues("iaf.mdm.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MdmController.class));
    }
}
