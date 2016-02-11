package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.dao.impl.ObsDaoImpl;
import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ETObsToBahmniObsMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.OMRSObsToBahmniObsMapper;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class BahmniObsServiceImplTest {

    BahmniObsService bahmniObsService;

    private String personUUID = "12345";

    @Mock
    ObsDao obsDao;
    @Mock
    VisitDao visitDao;
    @Mock
    private ObservationTypeMatcher observationTypeMatcher;
    @Mock
    private ObservationMapper observationMapper;
    @Mock
    private VisitService visitService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private BahmniExtensions bahmniExtensions;

    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        when(observationTypeMatcher.getObservationType(any(Obs.class))).thenReturn(ObservationTypeMatcher.ObservationType.OBSERVATION);
        bahmniObsService = new BahmniObsServiceImpl(obsDao, new OMRSObsToBahmniObsMapper(new ETObsToBahmniObsMapper(null), observationTypeMatcher, observationMapper), visitService, conceptService, visitDao, bahmniExtensions, bahmniProgramWorkflowService);
    }

    @Test
    public void shouldGetPersonObs() throws Exception {
        bahmniObsService.getObsForPerson(personUUID);
        verify(obsDao).getNumericObsByPerson(personUUID);
    }

    @Test
    public void shouldGetNumericConcepts() throws Exception {
        bahmniObsService.getNumericConceptsForPerson(personUUID);
        verify(obsDao).getNumericConceptsForPerson(personUUID);
    }

    @Test
    public void shouldGetObsByPatientUuidConceptNameAndNumberOfVisits() throws Exception {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        Integer numberOfVisits = 3;
        bahmniObsService.observationsFor(personUUID, Arrays.asList(bloodPressureConcept), numberOfVisits, null, false, null, null, null);
        verify(obsDao).getObsByPatientAndVisit(personUUID, Arrays.asList("Blood Pressure"),
                visitDao.getVisitIdsFor(personUUID, numberOfVisits), -1, ObsDaoImpl.OrderBy.DESC, null, false, null, null, null);
        verify(bahmniExtensions, times(1)).getExtension("observationsAdder", "CurrentMonthOfTreatment.groovy");
    }

    @Test
    public void shouldGetInitialObservations() throws Exception {
        Concept weightConcept = new ConceptBuilder().withName("Weight").build();
        Integer limit = 1;
        VisitBuilder visitBuilder = new VisitBuilder();
        Visit visit = visitBuilder.withUUID("visitId").withEncounter(new Encounter(1)).withPerson(new Person()).build();
        List<String> obsIgnoreList = new ArrayList<>();
        bahmniObsService.getInitialObsByVisit(visit, Arrays.asList(weightConcept), obsIgnoreList, true);
        verify(obsDao).getObsByPatientAndVisit(visit.getPatient().getUuid(), Arrays.asList("Weight"),
                Arrays.asList(visit.getVisitId()), limit, ObsDaoImpl.OrderBy.ASC, obsIgnoreList, true, null, null, null);
    }

    @Test
    public void shouldGetAllObsForOrder() throws Exception {
        bahmniObsService.getObservationsForOrder("orderUuid");
        verify(obsDao, times(1)).getObsForOrder("orderUuid");
    }

    @Test
    public void shouldGetObsForPatientProgram() {
        Collection<Encounter> encounters = Arrays.asList(new Encounter(), new Encounter());
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(any(String.class))).thenReturn(encounters);
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        Integer numberOfVisits = 3;

        bahmniObsService.observationsFor(personUUID, bloodPressureConcept, bloodPressureConcept, numberOfVisits, null, null, "patientProgramUuid");
        verify(obsDao).getObsFor(personUUID, bloodPressureConcept, bloodPressureConcept, visitDao.getVisitIdsFor(personUUID, numberOfVisits), encounters, null, null);
        verify(bahmniProgramWorkflowService).getEncountersByPatientProgramUuid("patientProgramUuid");
    }

    @Test
    public void shouldMakeACallToGetObservationsForEncounterAndConcepts() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        String encounterUuid = "encounterUuid";

        bahmniObsService.getObservationsForEncounter(encounterUuid, conceptNames);

        verify(obsDao, times(1)).getObsForConceptsByEncounter(encounterUuid, conceptNames);
    }

    @Test
    public void shouldReturnEmptyObservationListIfProgramDoesNotHaveEncounters() {
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(any(String.class))).thenReturn(Collections.EMPTY_LIST);
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();

        Collection<BahmniObservation> observations = bahmniObsService.observationsFor(personUUID, bloodPressureConcept, bloodPressureConcept, 3, null, null, "patientProgramUuid");

        verify(obsDao, times(0)).getObsFor(anyString(), any(Concept.class), any(Concept.class), any(List.class), any(Collection.class), any(Date.class), any(Date.class));
        assertThat(observations.size(), is(equalTo(0)));
    }

    @Test
    public void shouldCallObsServiceWithEmptyListOfEncountersWhenProgramUuidIsNull() {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();

        int numberOfVisits = 3;
        bahmniObsService.observationsFor(personUUID, bloodPressureConcept, bloodPressureConcept, numberOfVisits, null, null, null);

        verify(obsDao).getObsFor(personUUID, bloodPressureConcept, bloodPressureConcept, visitDao.getVisitIdsFor(personUUID, numberOfVisits), new ArrayList<Encounter>(), null, null);
    }

    public void shouldGetObsbyPatientProgramUuid() throws Exception {
        String patientProgramUuid = "patientProgramUuid";
        ArrayList<String> conceptNames = new ArrayList<>();
        ArrayList<Encounter> encounters = new ArrayList<>();
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid)).thenReturn(encounters);

        bahmniObsService.getObservationsForPatientProgram(patientProgramUuid, conceptNames);

        verify(bahmniProgramWorkflowService, times(1)).getEncountersByPatientProgramUuid(patientProgramUuid);
    }
}
