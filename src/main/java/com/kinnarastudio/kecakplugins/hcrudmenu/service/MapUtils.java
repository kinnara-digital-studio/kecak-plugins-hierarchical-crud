package com.kinnarastudio.kecakplugins.hcrudmenu.service;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.kecakplugins.hcrudmenu.formBinder.HierarchicalCrudFormBinder;
import com.kinnarastudio.kecakplugins.hcrudmenu.model.Table;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.form.model.Form;
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

        final Form form = table.getForm();
        if(form != null && table.isReadonly()) {
            FormUtil.setReadOnlyProperty(form);
        }

        map.put("id", dataList.getId());

        map.put("label", dataList.getName());

        map.put("level", table.getDepth());

        map.put("dataListId", dataList.getId());

        map.put("formId", Optional.ofNullable(form).map(f -> f.getPropertyString("id")).orElse(""));

        map.put("editable", !table.isReadonly());

        map.put("deletable", !table.isReadonly());

//        map.put("formUrl", "${request.contextPath}/web/app/${appId}/${appVersion}/form/embed?_submitButtonLabel=${buttonLabel!?html}");

        final String jsonForm = form == null ? "{}" : formService.generateElementJson(form);


        map.put("jsonForm", StringEscapeUtils.escapeHtml4(jsonForm));

        final String nonce = SecurityUtil.generateNonce(
                new String[]{"EmbedForm", appDefinition.getAppId(), appDefinition.getVersion().toString(), jsonForm },
                1);

        map.put("nonce", nonce);

        map.put("height", 500);
        map.put("width", 900);

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
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao)appContext.getBean("formDefinitionDao");
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();

        return Optional.of(formDefId)
                .map(s -> formDefinitionDao.loadById(s, appDef))
                .map(FormDefinition::getJson)
                .map(formService::createElementFromJson)
                .map(e -> (Form)e)
                .map(Try.toPeek(f -> {
                    f.setLoadBinder(new HierarchicalCrudFormBinder());
                    f.setStoreBinder(new HierarchicalCrudFormBinder());
                }));
    }
}
