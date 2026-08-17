package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.repository.MdmRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="iaf.mdm.enabled",havingValue="true",matchIfMissing=true)
public class MdmImportTaskDispatcher {
    private final MdmRepository repository; private final MdmExcelImportService imports;
    public MdmImportTaskDispatcher(MdmRepository repository,MdmExcelImportService imports){this.repository=repository;this.imports=imports;}
    @Scheduled(fixedDelayString="${iaf.mdm.import.poll-delay-ms:3000}")
    public void dispatch(){repository.findQueuedImportTasks(2).forEach(imports::processQueued);}
}
