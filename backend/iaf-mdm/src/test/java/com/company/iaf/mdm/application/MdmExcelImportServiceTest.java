package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MdmExcelImportServiceTest {
    @Test void generatedTemplateCanBeUploadedAndPrechecked() {
        var mdm = mock(MdmApplicationService.class);
        var field = new MdmModels.Field(1,"materialType","物料类型","ENUM",true,false,false,true,true,true,32,List.of("RAW","FINISHED"),"物料分类",10);
        var model = new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of(field));
        when(mdm.schema(1,"material")).thenReturn(model);
        when(mdm.validateBatch(eq(1L),eq("material"),any())).thenAnswer(invocation -> {
            MdmDtos.BatchRecordRequest request=invocation.getArgument(2);
            return new MdmDtos.BatchValidationResult(true,request.records().size(),List.of());
        });
        var service = new MdmExcelImportService(mdm);
        byte[] template = service.template(1,"material");
        byte[] populated;
        try (var workbook=WorkbookFactory.create(new ByteArrayInputStream(template)); var output=new ByteArrayOutputStream()) {
            var row=workbook.getSheet("导入数据").createRow(1); row.createCell(0).setCellValue("M-001"); row.createCell(1).setCellValue("测试物料"); row.createCell(2).setCellValue("RAW");
            workbook.write(output); populated=output.toByteArray();
        } catch (Exception failure) { throw new AssertionError(failure); }

        var preview = service.preview(1,"material",new MockMultipartFile("file","material.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",populated));

        assertThat(template).isNotEmpty();
        assertThat(preview.fileName()).isEqualTo("material.xlsx");
        assertThat(preview.records()).hasSize(1);
        assertThat(preview.records().getFirst().attributes()).containsEntry("materialType","RAW");
    }
}
