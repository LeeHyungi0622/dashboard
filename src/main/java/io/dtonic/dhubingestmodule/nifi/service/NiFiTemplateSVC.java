package io.dtonic.dhubingestmodule.nifi.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.common.code.Constants;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClient;
import io.swagger.client.model.FlowEntity;
import io.swagger.client.model.InstantiateTemplateRequestEntity;
import io.swagger.client.model.ProcessGroupEntity;
import io.swagger.client.model.TemplateDTO;
import io.swagger.client.model.TemplateEntity;
import io.swagger.client.model.TemplatesEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NiFiTemplateSVC {

    @Autowired
    private NiFiClient niFiClient;

    /**
     * Upload NiFi Templates to register collector/filter/converter
     *
     */
    public void uploadTemplate() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath:template/*.xml");
            for (Resource template : resources) {
                TemplateEntity result = niFiClient.getProcessGroups().uploadTemplate("root", template.getFile(), true);
                if (result == null) {
                    log.info("Template {} is already Exist", template);
                } else {
                    log.info("Upload Template Name : [{}]", template);
                }
            }
        } catch (IOException e) {
            log.error("Not Found Template Files in src/main/resources/template", e);
        } catch (Exception e) {
            log.error("Fail to Upload Template", e);
        }
    }
    /**
     * Create Adaptor Dummy Template
     *
     * @param AdaptorName adpatorType Adaptor Type (collector, filter, convertor)
     * @param rootProcessorGroupId Parent Process ID
     * @param templateId create template ID
     * @return ProcessGroup ID created Adaptor
     */
    public String createDummyTemplate(AdaptorName templateName, String rootProcessorGroupId, String templateId) {
        try {
            InstantiateTemplateRequestEntity body = new InstantiateTemplateRequestEntity();
            body.setTemplateId(templateId);
            body.setDisconnectedNodeAcknowledged(false);
            if (templateName.equals(AdaptorName.ADAPTOR_NAME_COLLECTOR)) body.setOriginX(0.0D);
            else if (templateName.equals(AdaptorName.ADAPTOR_NAME_FILTER)) body.setOriginX(740.0D);
            else if (templateName.equals(AdaptorName.ADAPTOR_NAME_CONVERTER)) body.setOriginX(1480.0D);
            else body.setOriginX(0.0D);
            body.setOriginY(0.0D);
            FlowEntity resultFlowEntity = niFiClient.getProcessGroups().instantiateTemplate(templateId, body);
            List<ProcessGroupEntity> resultProcessGroup = resultFlowEntity
                .getFlow()
                .getProcessGroups();
            if (resultProcessGroup.size() == 0) {
                log.error("Empty Processor Group in Template : Template ID = {}", templateId);
                return null;
            } else if (resultProcessGroup.size() > 1) {
                log.error(
                    "Too Many (2 or more) Processor Group in Template : Template ID = {}",
                    templateId
                );
                return null;
            } else {
                for (ProcessGroupEntity e : resultProcessGroup) {
                    String adaptorId = e.getId();
                    return adaptorId;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Fail to Create Dummy Template.", e);
            return null;
        }
    }
    
    /**
     * Search Template ID by Template Name In Resources
     *
     * @param String TempleteName Search template name
     * @return String Template ID
     */
    public String searchTemplatebyName(String TempleteName) {
        try {
            TemplatesEntity result = niFiClient.getFlow().getTemplates();
            List<TemplateEntity> templateList = result.getTemplates();
            for (TemplateEntity template : templateList) {
                TemplateDTO templateinfo = template.getTemplate();
                String installTemplateName = templateinfo.getName();
                if (TempleteName.equals(installTemplateName)) {
                    return templateinfo.getId();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Not Found Template Name : [{}]", TempleteName);
            return null;
        }
    }
    /**
     * Delete Template ID by Template Name In Resources
     *
     * @param String TempleteName Search template name
     * @return String Template ID
     */
    public void deleteTemplate() {
        try {
            TemplatesEntity result = niFiClient.getFlow().getTemplates();
            List<TemplateEntity> templateList = result.getTemplates();
            if (templateList.size() == 0) {
                log.info("Empty Template List in NiFi");
            } else {
                for (TemplateEntity template : templateList) {
                    for (String uploadTemplateName : Constants.NIFI_TEMPLATE_NAMES) {
                        if (template.getTemplate().getName().equals(uploadTemplateName)) {
                            niFiClient.getTemplates().removeTemplate(template.getTemplate().getId(), null);
                            log.info("Delete Template Name : [{}]", uploadTemplateName);
                        }
                    }
                }
            }
        }  catch (Exception e) {
            log.error("Fail to Delete Template", e);
        }
    }
}
