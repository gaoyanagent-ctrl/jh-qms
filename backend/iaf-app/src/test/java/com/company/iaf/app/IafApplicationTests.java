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

    @Test
    void contextLoads() {
    }
}
