package com.ethanaa.crudstar;

import com.ethanaa.crudstar.model.api.ApiFilter;
import com.ethanaa.crudstar.model.api.FilterConstraint;
import com.ethanaa.crudstar.model.api.PatientModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrudstarApplication.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@TestPropertySource(
        locations = "classpath:application-integrationtest.yml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CrudstarIntegrationTests {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final LocalDateTime TEST_START_TIME = LocalDateTime.now();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String existingPatientLink;
    private PatientModel existingPatientModel;
    private String newPatientLink;
    private String newSnapshotPatientLink;
    private PatientModel newPatientModel;
    private PatientModel newSnapshotPatientModel;
    private String updatedPatientLink;
    private PatientModel updatedPatientModel;
    private PatientModel updatedSnapshotPatientModel;
    private PatientModel patchedPatientModel;
    private String nowSnapshotId;
    private String nowSnapshotAsOf;
    private String startSnapshotId;

    private static class TestStep {
        public static final int GET_PATIENTS = 1;
        public static final int GET_PATIENT = 2;
        public static final int CREATE_PATIENT = 3;
        public static final int UPDATE_PATIENT = 4;
        public static final int PATIENT_VERSION = 5;
        public static final int PATIENT_SEARCH = 6;
        public static final int CREATE_SNAPSHOT = 7;
        public static final int GET_SNAPSHOTS = 8;
        public static final int GET_SNAPSHOT_PATIENTS = 9;
        public static final int GET_SNAPSHOT_PATIENT = 10;
        public static final int CREATE_SNAPSHOT_PATIENT = 11;
        public static final int UPDATE_SNAPSHOT_PATIENT = 12;
        public static final int SNAPSHOT_PATIENT_VERSION = 13;
        public static final int GET_PATIENTS_AS_OF = 14;
        public static final int GET_PATIENT_AS_OF = 15;
        public static final int GET_SNAPSHOT_PATIENTS_AS_OF = 16;
        public static final int GET_SNAPSHOT_PATIENT_AS_OF = 17;
        public static final int GET_PATIENTS_AGAIN = 18;
        public static final int GET_PATIENT_AGAIN = 19;
        public static final int GET_SNAPSHOT_PATIENTS_AGAIN = 20;
        public static final int GET_SNAPSHOT_PATIENT_AGAIN = 21;
        public static final int DELETE_SNAPSHOT = 22;
        public static final int DELETE_PATIENT = 23;

    }

    @Order(TestStep.GET_PATIENTS)
    @Nested
    @DisplayName("Get Patients")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetPatients {

        private String nextPageLink;
        private String lastPageLink;
        private int totalElements;
        private int totalPages;

        @Test
        @Order(1)
        @DisplayName("Should return the first page of patients")
        public void getPatients() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");
            JsonNode pageInformation = root.get("page");
            JsonNode patientModels = root.get("_embedded").get("patientModelList");

            this.nextPageLink = linkInformation.get("next").get("href").textValue();
            this.lastPageLink = linkInformation.get("last").get("href").textValue();
            this.totalElements = pageInformation.get("totalElements").intValue();
            this.totalPages = pageInformation.get("totalPages").intValue();
            CrudstarIntegrationTests.this.existingPatientLink
                    = patientModels.get(0).get("_links").get("self").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return the next page of patients")
        public void getPatientsNextPage() throws Exception {

            MvcResult result = mvc.perform(get(nextPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return the last page of patients")
        public void getPatientsLastPage() throws Exception {

            int pageLength = totalElements % DEFAULT_PAGE_SIZE;
            if (pageLength == 0) {
                pageLength = DEFAULT_PAGE_SIZE;
            }

            MvcResult result = mvc.perform(get(lastPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(pageLength))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(pageLength)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(totalPages - 1))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_PATIENT)
    @Nested
    @DisplayName("Get Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetPatient {

        private String patchesLink;

        @Test
        @Order(1)
        @DisplayName("Should return a patient")
        public void getPatient() throws Exception {
            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.existingPatientLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._links.keys()", hasItems("self", "patches", "diff")))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            CrudstarIntegrationTests.this.existingPatientModel = objectMapper.convertValue(root, PatientModel.class);

            this.patchesLink = linkInformation.get("patches").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return a patient's patches")
        public void getPatientPatches() throws Exception {
            MvcResult result = mvc.perform(get(patchesLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patchModelList").isArray())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].patch").exists())
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.CREATE_PATIENT)
    @Nested
    @DisplayName("Create Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreatePatient {

        @Test
        @Order(1)
        @DisplayName("Should return a new patient")
        public void createPatient() throws Exception {
            PatientModel newPatientModel = new PatientModel();
            BeanUtils.copyProperties(CrudstarIntegrationTests.this.existingPatientModel, newPatientModel);

            String firstName = "Test-Create " + UUID.randomUUID();
            newPatientModel.setFirstName(firstName);

            MvcResult result = mvc.perform(post("/api/patient")
                            .content(objectMapper.writeValueAsString(newPatientModel))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("self", "patches", "diff")))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            CrudstarIntegrationTests.this.newPatientModel = objectMapper.convertValue(root, PatientModel.class);
            CrudstarIntegrationTests.this.newPatientLink = result.getResponse().getHeader("Location");
        }

    }

    @Order(TestStep.UPDATE_PATIENT)
    @Nested
    @DisplayName("Update Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdatePatient {

        @Test
        @Order(1)
        @DisplayName("Should update a patient")
        public void updatePatient() throws Exception {
            PatientModel updatedPatientModel = new PatientModel();
            BeanUtils.copyProperties(CrudstarIntegrationTests.this.newPatientModel, updatedPatientModel);

            String firstName = "Test-Update " + UUID.randomUUID();
            updatedPatientModel.setFirstName(firstName);

            MvcResult result = mvc.perform(put(newPatientLink)
                            .content(objectMapper.writeValueAsString(updatedPatientModel))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "diff", "previousVersion", "diffAgainstPreviousVersion")))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            CrudstarIntegrationTests.this.updatedPatientModel = objectMapper.convertValue(root, PatientModel.class);

            CrudstarIntegrationTests.this.updatedPatientLink = linkInformation.get("self").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should patch a patient")
        public void patchPatient() throws Exception {

            String firstName = "Test-Patch " + UUID.randomUUID();

            String patch = "[{\"op\": \"replace\", \"path\": \"/firstName\", \"value\": \"" + firstName + "\"}]";

            MvcResult result = mvc.perform(patch(CrudstarIntegrationTests.this.updatedPatientLink)
                            .content(patch)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "diff", "previousVersion", "diffAgainstPreviousVersion")))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            CrudstarIntegrationTests.this.patchedPatientModel = objectMapper.convertValue(root, PatientModel.class);
        }
    }

    // TODO test patient diff

    @Order(TestStep.PATIENT_VERSION)
    @Nested
    @DisplayName("Patient Version")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PatientVersion {

        private int latestVersion;
        private String diffAgainstPreviousVersionLink;
        private String diffAgainstNextVersionLink;

        @Test
        @Order(1)
        @DisplayName("Should return the latest version number of a patient")
        public void patientLatestVersion() throws Exception {
            MvcResult result = mvc.perform(get(
                    CrudstarIntegrationTests.this.newPatientLink + "/latestVersion")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("3"))
                    .andReturn();

            this.latestVersion = Integer.parseInt(result.getResponse().getContentAsString());
        }

        @Test
        @Order(2)
        @DisplayName("Should return a patient as of its previous version")
        public void patientVersion() throws Exception {
            MvcResult result = mvc.perform(get(
                    CrudstarIntegrationTests.this.newPatientLink + "/version/" + (this.latestVersion - 1))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.updatedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion",
                                    "nextVersion", "diffAgainstNextVersion", "diffAgainstLatestVersion", "latestVersion"
                            )))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            this.diffAgainstPreviousVersionLink = linkInformation.get("diffAgainstPreviousVersion").get("href").textValue();
            this.diffAgainstNextVersionLink = linkInformation.get("diffAgainstNextVersion").get("href").textValue();
        }

        @Test
        @Order(3)
        @DisplayName("Should return a patient as of every version")
        public void patientVersions() throws Exception {
            MvcResult result = mvc.perform(get(
                            CrudstarIntegrationTests.this.newPatientLink + "/version/")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(3))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(3)))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.newPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "nextVersion", "diffAgainstNextVersion",
                                    "diffAgainstLatestVersion", "latestVersion")))
                    .andExpect(jsonPath("$._embedded.patientModelList[1].firstName").value(
                            CrudstarIntegrationTests.this.updatedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[1]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion",
                                    "nextVersion", "diffAgainstNextVersion", "diffAgainstLatestVersion", "latestVersion")))
                    .andExpect(jsonPath("$._embedded.patientModelList[2].firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[2]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion", "diff")))
                    .andReturn();
        }

        @Test
        @Order(4)
        @DisplayName("Should return the diff between a patient version and its previous version")
        public void patientVersionDiffPrevious() throws Exception {
            MvcResult result = mvc.perform(get(this.diffAgainstPreviousVersionLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.[0].op").value("replace"))
                    .andExpect(jsonPath("$.[0].path").value("/firstName"))
                    .andExpect(jsonPath("$.[0].value").value(
                            CrudstarIntegrationTests.this.newPatientModel.getFirstName()))
                    .andReturn();
        }

        @Test
        @Order(5)
        @DisplayName("Should return the diff between a patient version and its next version")
        public void patientVersionDiffNext() throws Exception {
            MvcResult result = mvc.perform(get(this.diffAgainstNextVersionLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.[0].op").value("replace"))
                    .andExpect(jsonPath("$.[0].path").value("/firstName"))
                    .andExpect(jsonPath("$.[0].value").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andReturn();
        }
    }

    @Order(TestStep.PATIENT_SEARCH)
    @Nested
    @DisplayName("Search Patients")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PatientSearch {

        @Test
        @Order(1)
        @DisplayName("Should return a page of patients matching the query")
        public void searchForPatient() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient/search?query="
                            + CrudstarIntegrationTests.this.patchedPatientModel.getFirstName())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(1))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$.page.totalElements").value(1))
                    .andReturn();
        }

        @Test
        @Order(2)
        @DisplayName("Should return a page of patients matching the query and filters")
        public void searchForPatientWithFilters() throws Exception {

            List<ApiFilter> filters = List.of(
                    new ApiFilter("lastName", "or", List.of(new FilterConstraint(
                            "equals", CrudstarIntegrationTests.this.patchedPatientModel.getLastName()))),
                    new ApiFilter("dateOfBirth", "or", List.of(new FilterConstraint(
                            "dateIs",
                            CrudstarIntegrationTests.this.patchedPatientModel.getDateOfBirth().atStartOfDay()
                            .format(DateTimeFormatter.ISO_DATE_TIME))))
            );

            String filterParam = objectMapper.writeValueAsString(filters);

            MvcResult result = mvc.perform(get("/api/patient/search")
                            .param("query", CrudstarIntegrationTests.this.patchedPatientModel.getFirstName())
                            .param("filters", filterParam)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(1))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].lastName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getLastName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].dateOfBirth").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getDateOfBirth()
                                    .format(DateTimeFormatter.ISO_DATE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$.page.totalElements").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return an empty page of patients due to filters")
        public void searchForPatientWithFiltersNoResults() throws Exception {

            List<ApiFilter> filters = List.of(
                    new ApiFilter("lastName", "or", List.of(new FilterConstraint(
                            "equals",
                            CrudstarIntegrationTests.this.patchedPatientModel.getLastName() + UUID.randomUUID()))),
                    new ApiFilter("dateOfBirth", "or", List.of(new FilterConstraint(
                            "dateIs",
                            CrudstarIntegrationTests.this.patchedPatientModel.getDateOfBirth().atStartOfDay()
                                    .format(DateTimeFormatter.ISO_DATE_TIME))))
            );

            String filterParam = objectMapper.writeValueAsString(filters);

            MvcResult result = mvc.perform(get("/api/patient/search")
                            .param("query", CrudstarIntegrationTests.this.patchedPatientModel.getFirstName())
                            .param("filters", filterParam)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.page.totalElements").value(0))
                    .andExpect(jsonPath("$.page.totalPages").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.CREATE_SNAPSHOT)
    @Nested
    @DisplayName("Create Snapshot")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateSnapshot {

        @Test
        @Order(1)
        @DisplayName("Should return a new snapshot as of now")
        public void createSnapshot() throws Exception {

            MvcResult result = mvc.perform(post("/api/patient/snapshot")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            CrudstarIntegrationTests.this.nowSnapshotId = root.get("id").textValue();
            CrudstarIntegrationTests.this.nowSnapshotAsOf = root.get("asOf").textValue();
        }

        @Test
        @Order(2)
        @DisplayName("Should return a new snapshot as of the start of the tests")
        public void createSnapshotAsOf() throws Exception {

            MvcResult result = mvc.perform(post("/api/patient/snapshot")
                            .param("asOf", TEST_START_TIME.format(DateTimeFormatter.ISO_DATE_TIME))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.asOf").value(TEST_START_TIME.format(DateTimeFormatter.ISO_DATE_TIME)))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            CrudstarIntegrationTests.this.startSnapshotId = root.get("id").textValue();
        }
    }

    @Order(TestStep.GET_SNAPSHOTS)
    @Nested
    @DisplayName("Get Snapshots")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshots {

        @Test
        @Order(1)
        @DisplayName("Should return the first page of snapshots")
        public void getSnapshots() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient/snapshot")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientSnapshotModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientSnapshotModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientSnapshotModelList[1].asOf")
                            .value(startsWith(TEST_START_TIME.format(DateTimeFormatter.ISO_DATE_TIME)
                                    .replaceAll("\\..*$", ""))))
                    .andExpect(jsonPath("$._embedded.patientSnapshotModelList[*]._links.keys()",
                            everyItem(hasItems("self"))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_SNAPSHOT_PATIENTS)
    @Nested
    @DisplayName("Get Snapshot Patients")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshotPatients {

        private String nextPageLink;
        private String lastPageLink;
        private int totalElements;
        private int totalPages;

        @Test
        @Order(1)
        @DisplayName("Should return the first page of snapshot patients")
        public void getSnapshotPatients() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");
            JsonNode pageInformation = root.get("page");

            this.nextPageLink = linkInformation.get("next").get("href").textValue();
            this.lastPageLink = linkInformation.get("last").get("href").textValue();
            this.totalElements = pageInformation.get("totalElements").intValue();
            this.totalPages = pageInformation.get("totalPages").intValue();
        }

        @Test
        @Order(2)
        @DisplayName("Should return the next page of snapshot patients")
        public void getSnapshotPatientsNextPage() throws Exception {

            MvcResult result = mvc.perform(get(nextPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return the last page of snapshot patients")
        public void geSnapshotPatientsLastPage() throws Exception {

            int pageLength = totalElements % DEFAULT_PAGE_SIZE;
            if (pageLength == 0) {
                pageLength = DEFAULT_PAGE_SIZE;
            }

            MvcResult result = mvc.perform(get(lastPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(pageLength))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(pageLength)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(totalPages - 1))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_SNAPSHOT_PATIENT)
    @Nested
    @DisplayName("Get Snapshot Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshotPatient {

        private String patchesLink;

        @Test
        @Order(1)
        @DisplayName("Should return a snapshot patient")
        public void getSnapshotPatient() throws Exception {
            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.updatedPatientLink
                            + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._links.keys()", hasItems("self", "patches", "diff")))
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            this.patchesLink = linkInformation.get("patches").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return a snapshot patient's patches")
        public void getSnapshotPatientPatches() throws Exception {

            MvcResult result = mvc.perform(get(URLDecoder.decode(patchesLink, StandardCharsets.UTF_8))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patchModelList").isArray())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].patch").exists())
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].path")
                            .value("/firstName"))
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].value")
                            .value(CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.CREATE_SNAPSHOT_PATIENT)
    @Nested
    @DisplayName("Create Snapshot Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateSnapshotPatient {

        @Test
        @Order(1)
        @DisplayName("Should return a new snapshot patient")
        public void createSnapshotPatient() throws Exception {
            PatientModel newPatientModel = new PatientModel();
            BeanUtils.copyProperties(CrudstarIntegrationTests.this.existingPatientModel, newPatientModel);

            String firstName = "Test-Snapshot-Create " + UUID.randomUUID();
            newPatientModel.setFirstName(firstName);

            MvcResult result = mvc.perform(post(
                    "/api/patient/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .content(objectMapper.writeValueAsString(newPatientModel))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("self", "patches", "diff")))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            CrudstarIntegrationTests.this.newSnapshotPatientModel = objectMapper.convertValue(root, PatientModel.class);
            CrudstarIntegrationTests.this.newSnapshotPatientLink = result.getResponse().getHeader("Location");
        }
    }

    @Order(TestStep.UPDATE_SNAPSHOT_PATIENT)
    @Nested
    @DisplayName("Update Snapshot Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateSnapshotPatient {

        @Test
        @Order(1)
        @DisplayName("Should update a snapshot patient")
        public void updateSnapshotPatient() throws Exception {
            PatientModel updatedPatientModel = new PatientModel();
            BeanUtils.copyProperties(CrudstarIntegrationTests.this.patchedPatientModel, updatedPatientModel);

            String firstName = "Test-Snapshot-Update " + UUID.randomUUID();
            updatedPatientModel.setFirstName(firstName);

            MvcResult result = mvc.perform(put(CrudstarIntegrationTests.this.updatedPatientLink
                            + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .content(objectMapper.writeValueAsString(updatedPatientModel))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "diff", "previousVersion", "diffAgainstPreviousVersion")))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            CrudstarIntegrationTests.this.updatedSnapshotPatientModel = objectMapper.convertValue(root, PatientModel.class);
        }
    }

    @Order(TestStep.SNAPSHOT_PATIENT_VERSION)
    @Nested
    @DisplayName("Snapshot Patient Version")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SnapshotPatientVersion {

        private int latestVersion;

        @Test
        @Order(1)
        @DisplayName("Should return the latest version number of a snapshot patient")
        public void snapshotPatientLatestVersion() throws Exception {
            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.updatedPatientLink
                                    + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId + "/latestVersion")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("4"))
                    .andReturn();

            this.latestVersion = Integer.parseInt(result.getResponse().getContentAsString());
        }

        @Test
        @Order(2)
        @DisplayName("Should return a snapshot patient as of its previous version")
        public void snapshotPatientVersion() throws Exception {
            MvcResult result = mvc.perform(get(
                            CrudstarIntegrationTests.this.updatedPatientLink
                                    + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId
                                    + "/version/" + (this.latestVersion - 1))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion",
                                    "nextVersion", "diffAgainstNextVersion", "diffAgainstLatestVersion", "latestVersion"
                            )))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return a snapshot patient as of every version")
        public void snapshotPatientVersions() throws Exception {
            MvcResult result = mvc.perform(get(
                            CrudstarIntegrationTests.this.updatedPatientLink
                                    + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId
                                    + "/version/")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(4))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(4)))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.newPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "nextVersion", "diffAgainstNextVersion",
                                    "diffAgainstLatestVersion", "latestVersion")))
                    .andExpect(jsonPath("$._embedded.patientModelList[1].firstName").value(
                            CrudstarIntegrationTests.this.updatedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[1]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion",
                                    "nextVersion", "diffAgainstNextVersion", "diffAgainstLatestVersion", "latestVersion")))
                    .andExpect(jsonPath("$._embedded.patientModelList[2].firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[2]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion",
                                    "nextVersion", "diffAgainstNextVersion", "diffAgainstLatestVersion", "latestVersion")))
                    .andExpect(jsonPath("$._embedded.patientModelList[3].firstName").value(
                            CrudstarIntegrationTests.this.updatedSnapshotPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[3]._links.keys()",
                            containsInAnyOrder(
                                    "self", "patches", "previousVersion", "diffAgainstPreviousVersion", "diff")))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_PATIENTS_AS_OF)
    @Nested
    @DisplayName("Get Patients as of")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetPatientsAsOf {

        private String nextPageLink;
        private String lastPageLink;
        private int totalElements;
        private int totalPages;

        @Test
        @Order(1)
        @DisplayName("Should return the first page of patients as of some time")
        public void getPatientsAsOf() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient")
                            .param("asOf", TEST_START_TIME.format(DateTimeFormatter.ISO_DATE_TIME))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.existingPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.existingPatientLink)))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last")))
                    .andExpect(jsonPath("$._links[*].href", everyItem(containsString("asOf="))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");
            JsonNode pageInformation = root.get("page");

            this.nextPageLink = linkInformation.get("next").get("href").textValue();
            this.lastPageLink = linkInformation.get("last").get("href").textValue();
            this.totalElements = pageInformation.get("totalElements").intValue();
            this.totalPages = pageInformation.get("totalPages").intValue();
        }

        @Test
        @Order(2)
        @DisplayName("Should return the next page of patients as of some time")
        public void getPatientsAsOfNextPage() throws Exception {

            MvcResult result = mvc.perform(get(nextPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last", "prev")))
                    .andExpect(jsonPath("$._links[*].href", everyItem(containsString("asOf="))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return the last page of patients as of some time")
        public void getPatientsAsOfLastPage() throws Exception {

            int pageLength = totalElements % DEFAULT_PAGE_SIZE;
            if (pageLength == 0) {
                pageLength = DEFAULT_PAGE_SIZE;
            }

            MvcResult result = mvc.perform(get(lastPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(pageLength))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(pageLength)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "last", "prev")))
                    .andExpect(jsonPath("$._links[*].href", everyItem(containsString("asOf="))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(totalPages - 1))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_PATIENT_AS_OF)
    @Nested
    @DisplayName("Get Patient as of")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetPatientAsOf {

        private String patchesLink;

        @Test
        @Order(1)
        @DisplayName("Should return a patient as of some time")
        public void getPatientAsOf() throws Exception {
            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.updatedPatientLink)
                            .param("asOf", CrudstarIntegrationTests.this.nowSnapshotAsOf)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._links.keys()", hasItems("self", "patches", "diff")))
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            this.patchesLink = linkInformation.get("patches").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return a patient's patches as of some time")
        public void getPatientPatchesAsOf() throws Exception {
            MvcResult result = mvc.perform(get(URLDecoder.decode(patchesLink, StandardCharsets.UTF_8))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patchModelList").isArray())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].patch").exists())
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].path")
                            .value("/firstName"))
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].value")
                            .value(CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_SNAPSHOT_PATIENTS_AS_OF)
    @Nested
    @DisplayName("Get Snapshot Patients as of")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshotPatientsAsOf {

        private String nextPageLink;
        private String lastPageLink;
        private int totalElements;
        private int totalPages;

        @Test
        @Order(1)
        @DisplayName("Should return the first page of snapshot patients as of some time")
        public void getSnapshotPatientsAsOf() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .param("asOf", TEST_START_TIME.format(DateTimeFormatter.ISO_DATE_TIME))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.existingPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.existingPatientLink)))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last")))
                    .andExpect(jsonPath("$._links[*].href", everyItem(containsString("asOf="))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");
            JsonNode pageInformation = root.get("page");

            this.nextPageLink = linkInformation.get("next").get("href").textValue();
            this.lastPageLink = linkInformation.get("last").get("href").textValue();
            this.totalElements = pageInformation.get("totalElements").intValue();
            this.totalPages = pageInformation.get("totalPages").intValue();
        }

        @Test
        @Order(2)
        @DisplayName("Should return the next page of snapshot patients as of some time")
        public void getSnapshotPatientsAsOfNextPage() throws Exception {

            MvcResult result = mvc.perform(get(nextPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last", "prev")))
                    .andExpect(jsonPath("$._links[*].href", everyItem(containsString("asOf="))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return the last page of snapshot patients as of some time")
        public void geSnapshotPatientsAsOfLastPage() throws Exception {

            int pageLength = totalElements % DEFAULT_PAGE_SIZE;
            if (pageLength == 0) {
                pageLength = DEFAULT_PAGE_SIZE;
            }

            MvcResult result = mvc.perform(get(lastPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(pageLength))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(pageLength)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "last", "prev")))
                    .andExpect(jsonPath("$._links[*].href", everyItem(containsString("asOf="))))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(totalPages - 1))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_SNAPSHOT_PATIENT_AS_OF)
    @Nested
    @DisplayName("Get Snapshot Patient as of")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshotPatientAsOf {

        private String patchesLink;

        @Test
        @Order(1)
        @DisplayName("Should return a snapshot patient as of some time")
        public void getSnapshotPatientAsOf() throws Exception {

            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.updatedPatientLink
                            + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .param("asOf", CrudstarIntegrationTests.this.nowSnapshotAsOf)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._links.keys()", hasItems("self", "patches", "previousVersion",
                            "diffAgainstPreviousVersion", "nextVersion", "diffAgainstNextVersion",
                            "diffAgainstLatestVersion", "latestVersion")))
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            this.patchesLink = linkInformation.get("patches").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return a snapshot patient's patches as of some time")
        public void getSnapshotPatientPatchesAsOf() throws Exception {

            MvcResult result = mvc.perform(get(URLDecoder.decode(patchesLink, StandardCharsets.UTF_8))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patchModelList").isArray())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].patch").exists())
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].path")
                            .value("/firstName"))
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].value")
                            .value(CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_PATIENTS_AGAIN)
    @Nested
    @DisplayName("Get Patients Again")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetPatientsAgain {

        private String nextPageLink;
        private String lastPageLink;
        private int totalElements;
        private int totalPages;

        @Test
        @Order(1)
        @DisplayName("Should return the first page of patients")
        public void getPatients() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");
            JsonNode pageInformation = root.get("page");

            this.nextPageLink = linkInformation.get("next").get("href").textValue();
            this.lastPageLink = linkInformation.get("last").get("href").textValue();
            this.totalElements = pageInformation.get("totalElements").intValue();
            this.totalPages = pageInformation.get("totalPages").intValue();
        }

        @Test
        @Order(2)
        @DisplayName("Should return the next page of patients")
        public void getPatientsNextPage() throws Exception {

            MvcResult result = mvc.perform(get(nextPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return the last page of patients")
        public void getPatientsLastPage() throws Exception {

            int pageLength = totalElements % DEFAULT_PAGE_SIZE;
            if (pageLength == 0) {
                pageLength = DEFAULT_PAGE_SIZE;
            }

            MvcResult result = mvc.perform(get(lastPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(pageLength))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(pageLength)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(totalPages - 1))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_PATIENT_AGAIN)
    @Nested
    @DisplayName("Get Patient Again")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetPatientAgain {

        private String patchesLink;

        @Test
        @Order(1)
        @DisplayName("Should return a patient")
        public void getPatient() throws Exception {
            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.updatedPatientLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._links.keys()", hasItems("self", "patches", "diff")))
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            this.patchesLink = linkInformation.get("patches").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return a patient's patches")
        public void getPatientPatches() throws Exception {
            MvcResult result = mvc.perform(get(patchesLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patchModelList").isArray())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].patch").exists())
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].path")
                            .value("/firstName"))
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].value")
                            .value(CrudstarIntegrationTests.this.patchedPatientModel.getFirstName()))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_SNAPSHOT_PATIENTS_AGAIN)
    @Nested
    @DisplayName("Get Snapshot Patients Again")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshotPatientsAgain {

        private String nextPageLink;
        private String lastPageLink;
        private int totalElements;
        private int totalPages;

        @Test
        @Order(1)
        @DisplayName("Should return the first page of snapshot patients")
        public void getSnapshotPatients() throws Exception {

            MvcResult result = mvc.perform(get("/api/patient/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches"))))
                    .andExpect(jsonPath("$._embedded.patientModelList[0].firstName").value(
                            CrudstarIntegrationTests.this.newSnapshotPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._embedded.patientModelList[0]._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.newSnapshotPatientLink)))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");
            JsonNode pageInformation = root.get("page");

            this.nextPageLink = linkInformation.get("next").get("href").textValue();
            this.lastPageLink = linkInformation.get("last").get("href").textValue();
            this.totalElements = pageInformation.get("totalElements").intValue();
            this.totalPages = pageInformation.get("totalPages").intValue();
        }

        @Test
        @Order(2)
        @DisplayName("Should return the next page of snapshot patients")
        public void getSnapshotPatientsNextPage() throws Exception {

            MvcResult result = mvc.perform(get(nextPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(DEFAULT_PAGE_SIZE)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "next", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(1))
                    .andReturn();
        }

        @Test
        @Order(3)
        @DisplayName("Should return the last page of snapshot patients")
        public void geSnapshotPatientsLastPage() throws Exception {

            int pageLength = totalElements % DEFAULT_PAGE_SIZE;
            if (pageLength == 0) {
                pageLength = DEFAULT_PAGE_SIZE;
            }

            MvcResult result = mvc.perform(get(lastPageLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patientModelList").exists())
                    .andExpect(jsonPath("$._embedded.patientModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patientModelList.length()").value(pageLength))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.self.href", hasSize(pageLength)))
                    .andExpect(jsonPath("$._embedded.patientModelList[*]._links.keys()",
                            everyItem(hasItems("self", "patches", "diff"))))
                    .andExpect(jsonPath("$._links.keys()",
                            containsInAnyOrder("first", "self", "last", "prev")))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.totalElements").value(totalElements))
                    .andExpect(jsonPath("$.page.totalPages").value(totalPages))
                    .andExpect(jsonPath("$.page.number").value(totalPages - 1))
                    .andReturn();
        }
    }

    @Order(TestStep.GET_SNAPSHOT_PATIENT_AGAIN)
    @Nested
    @DisplayName("Get Snapshot Patient Again")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSnapshotPatientAgain {

        private String patchesLink;

        @Test
        @Order(1)
        @DisplayName("Should return a snapshot patient")
        public void getSnapshotPatient() throws Exception {
            MvcResult result = mvc.perform(get(CrudstarIntegrationTests.this.updatedPatientLink
                            + "/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._links.keys()", hasItems("self", "patches", "diff")))
                    .andExpect(jsonPath("$.firstName").value(
                            CrudstarIntegrationTests.this.updatedSnapshotPatientModel.getFirstName()))
                    .andExpect(jsonPath("$._links.self.href").value(
                            startsWith(CrudstarIntegrationTests.this.updatedPatientLink)))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(content);

            JsonNode linkInformation = root.get("_links");

            this.patchesLink = linkInformation.get("patches").get("href").textValue()
                    .replaceAll("\\{.*}", "");
        }

        @Test
        @Order(2)
        @DisplayName("Should return a snapshot patient's patches")
        public void getSnapshotPatientPatches() throws Exception {

            MvcResult result = mvc.perform(get(URLDecoder.decode(patchesLink, StandardCharsets.UTF_8))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$._embedded.patchModelList").isArray())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].id").doesNotHaveJsonPath())
                    .andExpect(jsonPath("$._embedded.patchModelList[*].patch").exists())
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].path")
                            .value("/firstName"))
                    .andExpect(jsonPath("$._embedded.patchModelList[0].patch[0].value")
                            .value(CrudstarIntegrationTests.this.updatedSnapshotPatientModel.getFirstName()))
                    .andExpect(jsonPath("$.page.size").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andReturn();
        }
    }

    @Order(TestStep.DELETE_SNAPSHOT)
    @Nested
    @DisplayName("Delete Snapshot")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteSnapshot {

        @Test
        @Order(1)
        @DisplayName("Should delete now snapshot")
        public void deleteNowSnapshot() throws Exception {

            MvcResult result = mvc.perform(
                    delete("/api/patient/snapshot/" + CrudstarIntegrationTests.this.nowSnapshotId))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        @Test
        @Order(1)
        @DisplayName("Should delete start snapshot")
        public void deleteStartSnapshot() throws Exception {

            MvcResult result = mvc.perform(
                            delete("/api/patient/snapshot/" + CrudstarIntegrationTests.this.startSnapshotId))
                    .andExpect(status().isOk())
                    .andReturn();
        }
    }

    @Order(TestStep.DELETE_PATIENT)
    @Nested
    @DisplayName("Delete Patient")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeletePatient {

        @Test
        @Order(1)
        @DisplayName("Should delete a patient")
        public void deletePatient() throws Exception {

            MvcResult result = mvc.perform(
                            delete(CrudstarIntegrationTests.this.updatedPatientLink))
                    .andExpect(status().isOk())
                    .andReturn();
        }
    }
}
