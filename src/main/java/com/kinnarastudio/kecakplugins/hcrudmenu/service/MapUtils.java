package com.kinnarastudio.kecakplugins.hcrudmenu.service;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.kecakplugins.hcrudmenu.model.Table;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormDataDeletableBinder;
import org.joget.apps.form.model.FormDeleteBinder;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtils {
    public static Map<String, Object> toMap(Table table) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        final FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");

        final Map<String, Object> map = new HashMap<>();

        final DataList dataList = table.getDataList();

        final Optional<Form> optCreateForm = Optional.ofNullable(table.getCreateForm());

        final Optional<Form> optEditForm = Optional.ofNullable(table.getEditForm());
        optEditForm.ifPresent(form -> FormUtil.setReadOnlyProperty(form, table.isReadonly(), null));

        map.put("id", dataList.getId());

        map.put("label", dataList.getName());

        map.put("level", table.getDepth());

        map.put("dataListId", dataList.getId());

        final List<Map<String, String>> actions = Optional.of(dataList).map(DataList::getActions)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(a -> {
                    final Map<String, String> m = new HashMap<>();
                    return m;
                }).collect(Collectors.toList());

        optCreateForm.map(f -> f.getPropertyString("id")).ifPresent(s -> map.put("createFormId", s));

        optEditForm.map(f -> f.getPropertyString("id")).ifPresent(s -> map.put("editFormId", s));

        map.put("editable", optEditForm.isPresent() && !table.isReadonly());

        final boolean isDeletableStoreBinder = optCreateForm
                .map(Form::getStoreBinder)
                .filter(sb -> sb instanceof FormDeleteBinder || sb instanceof FormDataDeletableBinder)
                .isPresent();

        map.put("deletable", isDeletableStoreBinder && !table.isReadonly());

        final String jsonCreateForm = optCreateForm.map(formService::generateElementJson).orElse("{}");
        map.put("jsonCreateForm", StringEscapeUtils.escapeHtml4(jsonCreateForm));

        final String jsonEditForm = optEditForm.map(formService::generateElementJson).orElse("{}");
        map.put("jsonEditForm", StringEscapeUtils.escapeHtml4(jsonEditForm));

        final String nonceEdit = SecurityUtil.generateNonce(
                new String[]{"EmbedForm", appDefinition.getAppId(), appDefinition.getVersion().toString(), jsonEditForm},
                1);
        map.put("nonceEdit", nonceEdit);

        final String nonceCreate = SecurityUtil.generateNonce(
                new String[]{"EmbedForm", appDefinition.getAppId(), appDefinition.getVersion().toString(), jsonCreateForm},
                1);
        map.put("nonceCreate", nonceCreate);

        map.put("height", 500);
        map.put("width", 900);
        map.put("submitButtonLabel", table.getEditButtonLabel());

        final List<Map<String, String>> columns = Optional.of(dataList)
                .map(DataList::getColumns)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(c -> {
                    final Map<String, String> m = new HashMap<>();

                    final String columnName = c.getName();
                    m.put("name", columnName);

                    final String columnLabel = c.getLabel();
                    m.put("label", columnLabel);

                    Optional.of(dataList)
                            .map(DataList::getFilters)
                            .map(Arrays::stream)
                            .orElseGet(Stream::empty)
                            .filter(f -> columnName.equals(f.getName()))
                            .findAny().ifPresent(f -> m.put("filter", f.getName()));

                    return m;
                })
                .collect(Collectors.toList());

        map.put("columns", columns);

        final String foreignKeyFilter = table.getForeignKeyFilter();

        Optional.of(dataList)
                .map(DataList::getFilters)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(f -> foreignKeyFilter.equals(f.getName()))
                .findFirst()
                .ifPresent(f -> map.put("foreignKey", f.getName()));

        // children
        final List<Map<String, Object>> children = table.getChildren().stream()
                .map(MapUtils::toMap)
                .collect(Collectors.toList());

        map.put("children", children);


        // parent
        final Table parent = table.getParent();
        Optional.ofNullable(parent)
                .map(Table::getDataList)
                .map(DataList::getId)
                .ifPresent(s -> map.put("parent", s));

        return map;
    }

    public static Optional<Form> optForm(String formDefId) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        FormService formService = (FormService) appContext.getBean("formService");
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) appContext.getBean("formDefinitionDao");
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();

        return Optional.of(formDefId)
                .map(s -> formDefinitionDao.loadById(s, appDef))
                .map(FormDefinition::getJson)
                .map(formService::createElementFromJson)
                .map(e -> (Form) e);
    }
}
