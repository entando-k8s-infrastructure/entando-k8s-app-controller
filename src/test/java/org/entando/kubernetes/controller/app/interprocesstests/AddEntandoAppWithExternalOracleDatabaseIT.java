/*
 *
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General License for more
 * details.
 *
 */

package org.entando.kubernetes.controller.app.interprocesstests;

import static org.entando.kubernetes.controller.spi.common.NameUtils.snakeCaseOf;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.entando.kubernetes.model.DbmsVendor;
import org.entando.kubernetes.model.JeeServer;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.test.e2etest.common.SampleWriter;
import org.entando.kubernetes.test.e2etest.helpers.EntandoAppE2ETestHelper;
import org.entando.kubernetes.test.e2etest.helpers.KeycloakE2ETestHelper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("still-waiting-for-fix-on-entando-core")
class AddEntandoAppWithExternalOracleDatabaseIT extends AddEntandoAppBaseIT {

    @Test
    void create() {
        //Given I have an external Oracle database
        helper.externalDatabases()
                .prepareExternalOracleDatabase(EntandoAppE2ETestHelper.TEST_NAMESPACE,
                        snakeCaseOf(EntandoAppE2ETestHelper.TEST_APP_NAME + "_dedb"),
                        snakeCaseOf(EntandoAppE2ETestHelper.TEST_APP_NAME + "_portdb"),
                        snakeCaseOf(EntandoAppE2ETestHelper.TEST_APP_NAME + "_servdb"));
        EntandoApp entandoApp = new EntandoAppBuilder().withNewSpec()
                .withStandardServerImage(JeeServer.WILDFLY)
                .withDbms(DbmsVendor.ORACLE)
                .withIngressHostName(EntandoAppE2ETestHelper.TEST_APP_NAME + "." + helper.getDomainSuffix())
                .withNewKeycloakToUse()
                .withRealm(KeycloakE2ETestHelper.KEYCLOAK_REALM)
                .endKeycloakToUse()
                .withReplicas(1)
                .withTlsSecretName(null)
                .endSpec()
                .build();

        entandoApp.setMetadata(new ObjectMeta());
        entandoApp.getMetadata().setNamespace(EntandoAppE2ETestHelper.TEST_NAMESPACE);
        SampleWriter.writeSample(entandoApp, "app-with-external-oracle-db");

        entandoApp.getMetadata().setName(EntandoAppE2ETestHelper.TEST_APP_NAME);
        createAndWaitForApp(entandoApp, 0, false);
        verifyAllExpectedResources(entandoApp);
    }

    @Override
    protected void verifyEntandoDbDeployment() {
        //Nothing to do here
    }
}

