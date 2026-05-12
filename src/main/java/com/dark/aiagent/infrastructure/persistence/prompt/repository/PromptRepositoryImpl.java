package com.dark.aiagent.infrastructure.persistence.prompt.repository;

import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.entity.PromptTemplate;
import com.dark.aiagent.domain.prompt.entity.PromptVersion;
import com.dark.aiagent.domain.prompt.repository.PromptRepository;
import com.dark.aiagent.infrastructure.persistence.prompt.entity.PromptTemplateDO;
import com.dark.aiagent.infrastructure.persistence.prompt.entity.PromptVersionDO;
import com.dark.aiagent.infrastructure.persistence.prompt.mapper.PromptMapper;
import com.dark.aiagent.infrastructure.persistence.prompt.mapper.PromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prompt 仓储实现 (Infrastructure Layer)
 */
@Repository
@RequiredArgsConstructor
public class PromptRepositoryImpl implements PromptRepository {

    private final PromptMapper promptMapper;
    private final PromptTemplateMapper templateMapper;

    @Override
    public Optional<Prompt> findActiveBySlug(String slug) {
        PromptVersionDO versionDO = promptMapper.findActiveBySlug(slug);
        if (versionDO == null) {
            return Optional.empty();
        }

        return Optional.of(Prompt.builder()
                .slug(slug)
                .versionTag(versionDO.getVersionTag())
                .content(versionDO.getContent())
                .modelConfig(versionDO.getModelConfig())
                .variables(versionDO.getVariables())
                .build());
    }

    @Override
    public List<PromptTemplate> findAllTemplates() {
        return templateMapper.selectList(null).stream()
                .map(this::toTemplateEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PromptTemplate> findTemplateById(Integer id) {
        return Optional.ofNullable(templateMapper.selectById(id))
                .map(this::toTemplateEntity);
    }

    @Override
    public List<PromptVersion> findVersionsByTemplateId(Integer templateId) {
        return promptMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromptVersionDO>()
                        .eq(PromptVersionDO::getTemplateId, templateId)
                        .orderByDesc(PromptVersionDO::getCreateTime))
                .stream()
                .map(this::toVersionEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PromptVersion> findVersionById(Integer versionId) {
        return Optional.ofNullable(promptMapper.selectById(versionId))
                .map(this::toVersionEntity);
    }

    @Override
    public void saveTemplate(PromptTemplate template) {
        PromptTemplateDO templateDO = new PromptTemplateDO();
        templateDO.setId(template.getId());
        templateDO.setSlug(template.getSlug());
        templateDO.setType(template.getType());
        templateDO.setDescription(template.getDescription());
        
        if (templateDO.getId() == null) {
            templateMapper.insert(templateDO);
        } else {
            templateMapper.updateById(templateDO);
        }
    }

    @Override
    public void saveVersion(PromptVersion version) {
        PromptVersionDO versionDO = new PromptVersionDO();
        versionDO.setId(version.getId());
        versionDO.setTemplateId(version.getTemplateId());
        versionDO.setVersionTag(version.getVersionTag());
        versionDO.setContent(version.getContent());
        versionDO.setVariables(version.getVariables());
        versionDO.setModelConfig(version.getModelConfig());
        versionDO.setIsActive(version.getIsActive());

        if (versionDO.getId() == null) {
            promptMapper.insert(versionDO);
        } else {
            promptMapper.updateById(versionDO);
        }
    }

    @Override
    @Transactional
    public void updateVersionStatus(Integer templateId, Integer activeVersionId) {
        promptMapper.deactivateVersionsByTemplateId(templateId);
        promptMapper.activateVersion(activeVersionId);
    }

    private PromptTemplate toTemplateEntity(PromptTemplateDO templateDO) {
        return PromptTemplate.builder()
                .id(templateDO.getId())
                .slug(templateDO.getSlug())
                .type(templateDO.getType())
                .description(templateDO.getDescription())
                .createTime(templateDO.getCreateTime())
                .updateTime(templateDO.getUpdateTime())
                .build();
    }

    private PromptVersion toVersionEntity(PromptVersionDO versionDO) {
        return PromptVersion.builder()
                .id(versionDO.getId())
                .templateId(versionDO.getTemplateId())
                .versionTag(versionDO.getVersionTag())
                .content(versionDO.getContent())
                .variables(versionDO.getVariables())
                .modelConfig(versionDO.getModelConfig())
                .isActive(versionDO.getIsActive())
                .createTime(versionDO.getCreateTime())
                .build();
    }
}
