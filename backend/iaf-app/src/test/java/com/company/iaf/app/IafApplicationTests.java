package com.company.iaf.app;

import com.company.iaf.platform.auth.domain.repository.AuthUserRepository;
import com.company.iaf.platform.auth.domain.repository.PlatformUserRepository;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.platform.integration.domain.repository.OutboxEventRepository;
import com.company.iaf.platform.org.domain.repository.OrgRepository;
import com.company.iaf.platform.permission.domain.repository.MenuRepository;
import com.company.iaf.platform.permission.domain.repository.PermissionRepository;
import com.company.iaf.platform.permission.domain.repository.RoleRepository;
import com.company.iaf.platform.system.domain.repository.SystemConfigurationRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseJobRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseResultRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionFileRepository;
import com.company.iaf.qms.engineering.domain.repository.DrawingLegendRuleRepository;
import com.company.iaf.qms.engineering.domain.repository.QualityCharacteristicRepository;
import com.company.iaf.qms.engineering.infrastructure.persistence.InspectionStandardService;
import com.company.iaf.qms.engineering.domain.repository.PartRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.qms.engineering.domain.repository.QmsFileObjectRepository;
import com.company.iaf.qms.engineering.domain.repository.QmsObjectStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
class IafApplicationTests {

    @MockBean
    private AuthUserRepository authUserRepository;

    @MockBean
    private TenantRepository tenantRepository;

    @MockBean
    private PlatformUserRepository platformUserRepository;

    @MockBean
    private UserOrgRepository userOrgRepository;

    @MockBean
    private OrgRepository orgRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PermissionRepository permissionRepository;

    @MockBean
    private MenuRepository menuRepository;

    @MockBean
    private SystemConfigurationRepository systemConfigurationRepository;

    @MockBean
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private PartRepository partRepository;

    @MockBean
    private DrawingRepository drawingRepository;

    @MockBean
    private DrawingRevisionRepository drawingRevisionRepository;

    @MockBean
    private DrawingParseJobRepository drawingParseJobRepository;

    @MockBean
    private DrawingParseResultRepository drawingParseResultRepository;

    @MockBean
    private QmsAuditTrail qmsAuditTrail;

    @MockBean
    private QmsFileObjectRepository qmsFileObjectRepository;

    @MockBean
    private QmsObjectStorage qmsObjectStorage;

    @MockBean
    private DrawingRevisionFileRepository drawingRevisionFileRepository;

    @MockBean
    private DrawingLegendRuleRepository drawingLegendRuleRepository;

    @MockBean
    private QualityCharacteristicRepository qualityCharacteristicRepository;

    @MockBean
    private InspectionStandardService inspectionStandardService;

    @Test
    void contextLoads() {
    }
}
