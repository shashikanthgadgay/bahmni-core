package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.admin.auditLog.model.AuditLog;
import org.bahmni.module.admin.auditLog.service.AuditLogDaoService;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({Context.class})
@RunWith(PowerMockRunner.class)
public class AuditLogControllerTest {
    @InjectMocks
    AuditLogController auditLogController;

    @Mock
    UserContext userContext;

    @Mock
    AuditLogDaoService auditLogDaoService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getUserContext()).thenReturn(userContext);
    }

    @Test
    public void shouldExceptionIfUserIsNotLoggedIn() throws Exception {
        when(userContext.isAuthenticated()).thenReturn(false);

        thrown.expect(APIAuthenticationException.class);
        thrown.expectMessage("User is not logged in");
        auditLogController.getLogs("username", "patientId",
                "2017-03-22T18:30:00.000Z", 1, null);
    }

    @Test
    public void shouldExceptionIfUserIsNOtPrivileges() throws Exception {
        when(userContext.isAuthenticated()).thenReturn(true);
        when(userContext.hasPrivilege("admin")).thenReturn(false);

        thrown.expect(APIException.class);
        thrown.expectMessage("User is logged in but does not have sufficient privileges");
        auditLogController.getLogs("username", "patientId",
                "2017-03-22T18:30:00.000Z", 1, null);
    }

    @Test
    public void shouldGiveAuditLogs() throws Exception {
        Date startDateTime = BahmniDateUtil.convertToDate("2017-03-22T18:30:00.000Z",
                BahmniDateUtil.DateFormatType.UTC);
        when(userContext.isAuthenticated()).thenReturn(true);
        when(userContext.hasPrivilege("admin")).thenReturn(true);
        when(auditLogDaoService.getLogs("username", "patientId", startDateTime,
                1, null)).thenReturn(new ArrayList<>());

        List<AuditLog> logs = auditLogController.getLogs("username", "patientId",
                "2017-03-22T18:30:00.000Z", 1, null);
        assertEquals(0, logs.size());
        verify(auditLogDaoService, times(1))
                .getLogs("username", "patientId", startDateTime, 1, false);
    }
}