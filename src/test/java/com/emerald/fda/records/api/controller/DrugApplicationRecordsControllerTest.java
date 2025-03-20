package com.emerald.fda.records.api.controller;

import com.emerald.fda.records.api.dto.fda.DrugApplicationResultDto;
import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import com.emerald.fda.records.api.dto.fda.MetaDto;
import com.emerald.fda.records.api.dto.fda.ResultsMetaDto;
import com.emerald.fda.records.api.dto.request.StoreDrugApplicationRecordDto;
import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import com.emerald.fda.records.api.service.DrugApplicationRecordsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DrugApplicationRecordsController.class)
class DrugApplicationRecordsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DrugApplicationRecordsService service;

    @Test
    void searchDrugApplicationRecord_ShouldReturnFdaResponse() throws Exception {
        // given
        var responseDto = new FdaResponseDto(
                new MetaDto(null, null, null, null, new ResultsMetaDto(0, 10, 1)),
                List.of(new DrugApplicationResultDto(null, "ANDA076805", "TARO", null, null))
        );

        // when
        when(service.searchDrugApplicationRecords(eq("TARO"), eq("LORATADINE"), anyInt(), anyInt()))
                .thenReturn(responseDto);

        // then
        mockMvc.perform(get("/v1/drug-application-records/search")
                        .param("manufacturerName", "TARO")
                        .param("brandName", "LORATADINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)))
                .andExpect(jsonPath("$.results[0].application_number", is("ANDA076805")))
                .andExpect(jsonPath("$.results[0].sponsor_name", is("TARO")));
    }

    @Test
    void getAllDrugApplications_ShouldReturnPageOfApplicationRecords() throws Exception {
        // given
        List<DrugApplicationRecord> applications = List.of(
                DrugApplicationRecord.builder()
                        .applicationNumber("ANDA076805")
                        .manufacturerName("TARO")
                        .substanceName("LORATADINE")
                        .productNumbers(Set.of("001"))
                        .build(),
                DrugApplicationRecord.builder()
                        .applicationNumber("ANDA076806")
                        .manufacturerName("OTHER")
                        .substanceName("SUBSTANCE")
                        .productNumbers(Set.of("002"))
                        .build()
        );

        Page<DrugApplicationRecord> page = new PageImpl<>(applications, PageRequest.of(0, 10), applications.size());

        //when
        when(service.getAllDrugApplicationRecords(any(PageRequest.class))).thenReturn(page);

        // then
        mockMvc.perform(get("/v1/drug-application-records")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].applicationNumber", is("ANDA076805")))
                .andExpect(jsonPath("$.content[1].applicationNumber", is("ANDA076806")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getDrugApplicationById_WithExistingId_ShouldReturnApplicationRecord() throws Exception {
        // given
        String applicationNumber = "ANDA076805";
        var application = DrugApplicationRecord.builder()
                .applicationNumber(applicationNumber)
                .manufacturerName("TARO")
                .substanceName("LORATADINE")
                .productNumbers(Set.of("001"))
                .build();

        // when
        when(service.getDrugApplicationById(applicationNumber)).thenReturn(Optional.of(application));

        // then
        mockMvc.perform(get("/v1/drug-application-records/{applicationNumber}", applicationNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationNumber", is(applicationNumber)))
                .andExpect(jsonPath("$.manufacturerName", is("TARO")))
                .andExpect(jsonPath("$.substanceName", is("LORATADINE")))
                .andExpect(jsonPath("$.productNumbers", contains("001")));
    }

    @Test
    void getDrugApplicationRecordById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // given
        String applicationNumber = "NONEXISTENT";

        // when
        when(service.getDrugApplicationById(applicationNumber)).thenReturn(Optional.empty());

        // then
        mockMvc.perform(get("/v1/drug-application-records/{applicationNumber}", applicationNumber))
                .andExpect(status().isNotFound());
    }

    @Test
    void storeSpecificDrugApplicationRecord_ShouldReturnCreated() throws Exception {
        // given
        var request = new StoreDrugApplicationRecordDto(
                "ANDA076805",
                "TARO",
                "LORATADINE",
                Set.of("001")
        );

        var drugApplicationRecord = DrugApplicationRecord.builder()
                .applicationNumber(request.applicationNumber())
                .manufacturerName(request.manufacturerName())
                .substanceName(request.substanceName())
                .productNumbers(request.productNumbers())
                .build();

        // when
        when(service.saveDrugApplicationRecord(request.applicationNumber(), request.manufacturerName(), request.substanceName(), request.productNumbers()))
                .thenReturn(drugApplicationRecord);

        // then
        mockMvc.perform(post("/v1/drug-application-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationNumber", is("ANDA076805")))
                .andExpect(jsonPath("$.manufacturerName", is("TARO")))
                .andExpect(jsonPath("$.substanceName", is("LORATADINE")))
                .andExpect(jsonPath("$.productNumbers", contains("001")));
    }

}