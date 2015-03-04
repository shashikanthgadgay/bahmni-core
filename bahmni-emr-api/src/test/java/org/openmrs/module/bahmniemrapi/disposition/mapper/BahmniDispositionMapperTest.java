package org.openmrs.module.bahmniemrapi.disposition.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.*;

public class BahmniDispositionMapperTest {

    private BahmniDispositionMapper bahmniDispositionMapper;

    @Before
    public void setup(){
        bahmniDispositionMapper = new BahmniDispositionMapper();
    }

    @Test
    public void ensureBahmniDispositionIsPopulated(){
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setName("Sample Provider");
        provider.setUuid("1234Uuid");

        Set<EncounterTransaction.Provider> providers = new HashSet();
        providers.add(provider);

        Date dispositionDate = new Date();

        EncounterTransaction.Disposition disposition= new EncounterTransaction.Disposition();
        disposition.setCode("1234")
                .setExistingObs("a26a8c32-6fc1-4f5e-8a96-f5f5b05b87d")
                .setVoided(false)
                .setVoidReason(null)
                .setDispositionDateTime(dispositionDate);

        disposition.setConceptName("Absconding");
        disposition.setAdditionalObs(new ArrayList<EncounterTransaction.Observation>());

        BahmniDisposition bahmniDisposition = bahmniDispositionMapper.map(disposition, providers);

        Assert.assertEquals("1234",bahmniDisposition.getCode());
        Assert.assertEquals("a26a8c32-6fc1-4f5e-8a96-f5f5b05b87d",bahmniDisposition.getExistingObs());
        Assert.assertFalse(bahmniDisposition.isVoided());
        Assert.assertNull(bahmniDisposition.getVoidReason());
        Assert.assertEquals(dispositionDate,bahmniDisposition.getDispositionDateTime());
        Assert.assertEquals("Absconding", bahmniDisposition.getConceptName());
        Assert.assertEquals(0, bahmniDisposition.getAdditionalObs().size());

        EncounterTransaction.Provider actualProvider = bahmniDisposition.getProviders().iterator().next();
        Assert.assertEquals("Sample Provider", actualProvider.getName());
        Assert.assertEquals("1234Uuid", actualProvider.getUuid());

    }

}