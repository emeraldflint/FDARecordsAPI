package com.emerald.fda.records.api.service;

import com.emerald.fda.records.api.dto.fda.DrugApplicationResultDto;
import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import com.emerald.fda.records.api.dto.fda.MetaDto;
import com.emerald.fda.records.api.dto.fda.ResultsMetaDto;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class FdaClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FdaClientService fdaClientService;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    private final String baseUrl = "https://api.fda.gov/drug/drugsfda.json";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fdaClientService, "baseUrl", baseUrl);
    }

    @Test
    void searchDrugApplicationRecords_ShouldCallFdaApiWithCorrectParams() {
        // given
        FdaResponseDto expectedResponse = new FdaResponseDto(
                new MetaDto(null, null, null, null, new ResultsMetaDto(0, 10, 1)),
                List.of(new DrugApplicationResultDto(null, "ANDA076805", "TARO", null, null))
        );

        when(restTemplate.getForObject(anyString(), eq(FdaResponseDto.class)))
                .thenReturn(expectedResponse);

        // when
        FdaResponseDto actualResponse = fdaClientService.searchDrugApplicationRecords("TARO", "LORATADINE", 0, 10);

        // than
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(FdaResponseDto.class));

        String capturedUrl = urlCaptor.getValue();
        String decodedUrl = URLDecoder.decode(capturedUrl, StandardCharsets.UTF_8);

        assertThat(decodedUrl).startsWith(baseUrl);
        assertThat(decodedUrl).contains("search=openfda.manufacturer_name:\"TARO\" AND openfda.brand_name:\"LORATADINE\"");
        assertThat(decodedUrl).contains("skip=0");
        assertThat(decodedUrl).contains("limit=10");
    }

    @Test
    void searchDrugApplicationRecords_WithoutBrandName_ShouldNotIncludeBrandNameInQuery() {
        // given
        when(restTemplate.getForObject(anyString(), eq(FdaResponseDto.class)))
                .thenReturn(new FdaResponseDto(null, null));

        // when
        fdaClientService.searchDrugApplicationRecords("TARO", null, 0, 10);

        // then
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(FdaResponseDto.class));

        String capturedUrl = urlCaptor.getValue();
        String decodedUrl = URLDecoder.decode(capturedUrl, StandardCharsets.UTF_8);

        assertThat(decodedUrl).contains("search=openfda.manufacturer_name:\"TARO\"");
        assertThat(decodedUrl).doesNotContain("AND openfda.brand_name");
    }
}