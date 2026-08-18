package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.repository.MdmRepository;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MdmModelDictionaryExcelServiceTest {
    private final MdmRepository repository=mock(MdmRepository.class);
    private final MdmModelDictionaryExcelService service=new MdmModelDictionaryExcelService(repository);

    @Test void generatesMultiSheetTemplate() throws Exception {
        try(var workbook=WorkbookFactory.create(new java.io.ByteArrayInputStream(service.template()))){
            assertThat(workbook.getSheet("模型定义")).isNotNull();
            assertThat(workbook.getSheet("字段定义")).isNotNull();
            assertThat(workbook.getSheet("枚举选项")).isNotNull();
            assertThat(workbook.getSheet("关联定义")).isNotNull();
        }
    }

    @Test void previewsNewModelWithoutWritingDatabase() throws Exception {
        when(repository.findModels(1L)).thenReturn(List.of());
        byte[] content;
        try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()){
            var models=workbook.createSheet("模型定义");row(models,0,"数据域编码","模型编码","模型名称","记录类型");row(models,1,"manufacturing","material","物料","MASTER");
            var fields=workbook.createSheet("字段定义");row(fields,0,"模型编码","字段编码","字段名称","数据类型","列表显示");row(fields,1,"material","materialType","物料类型","STRING","是");
            workbook.write(output);content=output.toByteArray();
        }
        var file=new MockMultipartFile("file","dictionary.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",content);
        var preview=service.preview(1L,file);
        assertThat(preview.valid()).isTrue();
        assertThat(preview.modelCreates()).isEqualTo(1);
        assertThat(preview.totalFields()).isEqualTo(1);
        assertThat(preview.changes().getFirst().fieldAdds()).isEqualTo(1);
    }

    private void row(org.apache.poi.ss.usermodel.Sheet sheet,int number,String...values){var row=sheet.createRow(number);for(int i=0;i<values.length;i++)row.createCell(i).setCellValue(values[i]);}
}
