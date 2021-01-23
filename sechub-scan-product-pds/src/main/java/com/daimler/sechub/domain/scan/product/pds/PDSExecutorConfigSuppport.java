// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.daimler.sechub.adapter.DefaultExecutorConfigSupport;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.validation.Validation;

public class PDSExecutorConfigSuppport extends DefaultExecutorConfigSupport {

    /**
     * Creates the configuration support and VALIDATE. This will fail when
     * configuration data is not valid (e.g. mandatory keys missing)
     * 
     * @param config
     * @param systemEnvironment
     * @return support
     * @throws NotAcceptableException when configuration is not valid
     */
    public static PDSExecutorConfigSuppport createSupportAndAssertConfigValid(ProductExecutorConfig config, SystemEnvironment systemEnvironment) {
        return new PDSExecutorConfigSuppport(config, systemEnvironment, new PDSProductExecutorMinimumConfigValidation());
    }

    private PDSExecutorConfigSuppport(ProductExecutorConfig config, SystemEnvironment systemEnvironment, Validation<ProductExecutorConfig> validation) {
        super(config, systemEnvironment, validation);
    }

    public Map<String, String> createJobParametersToSendToPDS() {

        Map<String, String> parametersToSend = new TreeMap<>();
        List<PDSSecHubConfigDataKeyProvider<?>> providers = new ArrayList<>();
        providers.addAll(Arrays.asList(PDSProductExecutorKeys.values()));
        providers.addAll(Arrays.asList(PDSConfigDataKeys.values()));

        for (String originKey : configuredExecutorParameters.keySet()) {
            PDSSecHubConfigDataKeyProvider<?> foundProvider = null;
            for (PDSSecHubConfigDataKeyProvider<?> provider : providers) {
                String key = provider.getKey().getId();
                if (originKey.equalsIgnoreCase(key)) {
                    foundProvider = provider;
                    break;
                }
            }
            /* either not special (so always sent to PDS) or special but must be sent */
            if (foundProvider == null || foundProvider.getKey().isSentToPDS()) {
                parametersToSend.put(originKey, configuredExecutorParameters.get(originKey));
            }
        }
        return parametersToSend;
    }

    public String getPDSProductIdentifier() {
        return getParameter(PDSConfigDataKeys.PDS_PRODUCT_IDENTIFIER);
    }

    public int getScanResultCheckPeriodInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(PDSProductExecutorKeys.TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultScanResultCheckPeriodInMinutes();
    }

    public int getScanResultCheckTimeoutInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(PDSProductExecutorKeys.TIME_TO_WAIT_BEFORE_TIMEOUT);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultScanResultCheckPeriodInMinutes();
    }

    /**
     * @return <code>true</code> when PDS server with an untrusted certificate (e.g. self signed) is accepted, <code>false</code> when not (default)
     */
    public boolean isTrustAllCertificatesEnabled() {
        return getParameterBooleanValue(PDSProductExecutorKeys.TRUST_ALL_CERTIFICATES);
    }

    public boolean isTargetTypeForbidden(TargetType targetType) {
        boolean forbidden = false;
        for (PDSProductExecutorKeys k : PDSProductExecutorKeys.values()) {
            if (forbidden) {
                break;
            }
            PDSSecHubConfigDataKey<?> forbiddenKey = k.getKey();
            if (!(forbiddenKey instanceof PDSForbiddenTargetTypeInputKey)) {
                continue;
            }
            PDSForbiddenTargetTypeInputKey pdsForbiddenKey= (PDSForbiddenTargetTypeInputKey) forbiddenKey;
            if (! targetType.equals(pdsForbiddenKey.getForbiddenTargetType())){
                continue;
            }
            String val = getParameter(forbiddenKey);
            forbidden = Boolean.parseBoolean(val);
        }
        return forbidden;
    }

    private String getParameter(PDSSecHubConfigDataKeyProvider<?> keyProvider) {
        return getParameter(keyProvider.getKey());
    }

    private String getParameter(PDSSecHubConfigDataKey<?> configDataKey) {
        return getParameter(configDataKey.getId());
    }

    private int getParameterIntValue(PDSProductExecutorKeys k) {
        return getParameterIntValue(k.getKey().getId());
    }

    private boolean getParameterBooleanValue(PDSProductExecutorKeys k) {
        return getParameterBooleanValue(k.getKey().getId());
    }

}