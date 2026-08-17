package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.domain.repository.MdmImportObjectStorage;
import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MdmExcelImportServiceTest {
    @Test void generatedTemplateCanBeUploadedAndPrechecked() {
        var mdm = mock(MdmApplicationService.class);
        var repository=mock(MdmRepository.class); var storage=mock(MdmImportObjectStorage.class); var taskId=UUID.randomUUID();
        var field = new MdmModels.Field(1,"materialType","物料类型","ENUM",true,false,false,true,true,true,32,List.of("RAW","FINISHED"),"物料分类",10);
        var model = new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of(field));
        when(mdm.schema(1,"material")).thenReturn(model);
        when(mdm.validateBatch(eq(1L),eq("material"),any())).thenAnswer(invocation -> {
            MdmDtos.BatchRecordRequest request=invocation.getArgument(2);
            return new MdmDtos.BatchValidationResult(true,request.records().size(),List.of());
        });
        when(repository.insertImportTask(eq(1L),eq(9L),eq(7L),eq("material"),eq("material.xlsx"),any(),any(),anyString(),anyString(),anyLong())).thenReturn(new MdmModels.ImportTask(taskId,7,"material","material.xlsx","READY",1,1,0,0,true,9,"测试用户",null,null));
        var service = new MdmExcelImportService(mdm,repository,storage);
        byte[] template = service.template(1,"material");
        byte[] populated;
        try (var workbook=WorkbookFactory.create(new ByteArrayInputStream(template)); var output=new ByteArrayOutputStream()) {
            var row=workbook.getSheet("导入数据").createRow(1); row.createCell(0).setCellValue("M-001"); row.createCell(1).setCellValue("测试物料"); row.createCell(2).setCellValue("RAW");
            workbook.write(output); populated=output.toByteArray();
        } catch (Exception failure) { throw new AssertionError(failure); }

        var preview = service.preview(1,9,"material",new MockMultipartFile("file","material.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",populated));

        assertThat(template).isNotEmpty();
        assertThat(preview.fileName()).isEqualTo("material.xlsx");
        assertThat(preview.taskId()).isEqualTo(taskId);
        assertThat(preview.records()).hasSize(1);
        assertThat(preview.records().getFirst().attributes()).containsEntry("materialType","RAW");
        verify(storage).put(startsWith("1/mdm-imports/"),any(),eq((long)populated.length),eq("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test void commitRevalidatesClaimsAndCompletesPersistedTask() {
        var mdm=mock(MdmApplicationService.class); var repository=mock(MdmRepository.class); var taskId=UUID.randomUUID();
        var model=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of());
        var ready=new MdmModels.ImportTask(taskId,7,"material","material.xlsx","READY",1,1,0,0,true,9,"测试用户",null,null);
        var committed=new MdmModels.ImportTask(taskId,7,"material","material.xlsx","COMMITTED",1,1,0,1,true,9,"测试用户",null,null);
        var record=new MdmDtos.SaveRecordRequest("M-1","物料","DRAFT","GROUP",List.of(),null,null,Map.of(),null,"导入");
        var validation=new MdmDtos.BatchValidationResult(true,1,List.of(new MdmDtos.BatchRowValidation(2,"M-1",true,List.of())));
        when(mdm.schema(1,"material")).thenReturn(model); when(repository.findImportTask(1,7,taskId)).thenReturn(java.util.Optional.of(ready),java.util.Optional.of(committed));
        when(repository.findImportTaskRecords(1,7,taskId)).thenReturn(List.of(record)); when(mdm.validateBatch(eq(1L),eq("material"),any())).thenReturn(validation);
        when(repository.claimImportTask(1,7,taskId,9)).thenReturn(true); when(mdm.createBatch(eq(1L),eq(9L),eq("material"),any())).thenReturn(List.of(mock(MdmModels.Record.class)));

        var result=new MdmExcelImportService(mdm,repository,mock(MdmImportObjectStorage.class)).commit(1,9,"material",taskId);

        assertThat(result.status()).isEqualTo("COMMITTED");
        verify(repository).claimImportTask(1,7,taskId,9); verify(repository).completeImportTask(1,7,taskId,9,1);
    }

    @Test void downloadsArchivedSourceAndGeneratesReadableResultWorkbook() throws Exception {
        var mdm=mock(MdmApplicationService.class);var repository=mock(MdmRepository.class);var storage=mock(MdmImportObjectStorage.class);var taskId=UUID.randomUUID();
        var model=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of());
        var task=new MdmModels.ImportTask(taskId,7,"material","material.xlsx","PRECHECK_FAILED",1,0,1,0,true,9,"测试用户",null,null);
        var validation=new MdmDtos.BatchValidationResult(false,1,List.of(new MdmDtos.BatchRowValidation(2,"M-1",false,List.of("materialType: invalid option"))));
        when(mdm.schema(1,"material")).thenReturn(model);when(repository.findImportTask(1,7,taskId)).thenReturn(java.util.Optional.of(task));
        when(repository.findImportArtifact(1,7,taskId)).thenReturn(java.util.Optional.of(new MdmModels.ImportArtifact("1/mdm-imports/source.xlsx","material.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",3)));
        when(storage.get("1/mdm-imports/source.xlsx")).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));when(repository.findImportTaskValidation(1,7,taskId)).thenReturn(validation);
        var service=new MdmExcelImportService(mdm,repository,storage);

        assertThat(service.sourceFile(1,"material",taskId).content()).containsExactly(1,2,3);
        var report=service.resultFile(1,"material",taskId);
        try(var workbook=WorkbookFactory.create(new ByteArrayInputStream(report.content()))){assertThat(workbook.getSheet("校验结果").getRow(1).getCell(3).getStringCellValue()).contains("invalid option");}
    }
}
