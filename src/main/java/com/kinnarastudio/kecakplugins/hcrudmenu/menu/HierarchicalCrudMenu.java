package com.kinnarastudio.kecakplugins.hcrudmenu.menu;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.kecak.apps.exception.ApiException;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Hierarchical CRUD Menu
 */
public class HierarchicalCrudMenu extends UserviewMenu {
    @Override
    public String getCategory() {
        return "Kecak";
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getRenderPage() {
        final ApplicationContext appContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        final Map<String, Object> dataModel = new HashMap<>();
        final String template = "/templates/HierarchicalCrudMenu.ftl";
        final String label = getLabel();
        dataModel.put("className", getClass().getName());
        dataModel.put("label", label);

        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        if (appDefinition != null) {
            dataModel.put("appId", appDefinition.getAppId());
            dataModel.put("appVersion", appDefinition.getVersion());

            final List<Map<String, Object>> tables = getTables();
            dataModel.put("tables", tables);
        }
        final String htmlContent = pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), template, "/messages/HierarchicalCrud");
        return htmlContent;
    }

    @Override
    public boolean isHomePageSupported() {
        return false;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        return resourceBundle.getString("buildNumber");
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "HCrud";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/HierarchicalCrudMenu.json", null, true, "/messages/HierarchicalCrudMenu");
    }

    protected List<Map<String, Object>> getTables() {

        final List<Map<String, Object>> result = new ArrayList<>();
        final Map<String, String>[] tables = getPropertyGrid("tables");

        final Map<String, String> child = new HashMap<>();
        for (int i = tables.length - 1; i >= 0; i--) {
            final Map<String, String> table = tables[i];
            final String dataListId = table.get("dataListId");

            final Optional<DataList> optDataList = optDataList(dataListId);
            if(!optDataList.isPresent()) {
                continue;
            }

            final DataList dataList = optDataList.get();

            final Map<String, Object> element = new HashMap<>();

            element.put("id", dataList.getId());
            element.put("label", dataList.getName());

            final List<Map<String, String>> columns = Arrays.stream(dataList.getColumns())
                    .map(c -> {
                        final Map<String, String> column = new HashMap<>();

                        final String columnName = c.getName();
                        final String columnLabel = c.getLabel();

                        column.put("name", columnName);
                        column.put("label", columnLabel);

                        return column;
                    }).collect(Collectors.toList());

            element.put("columns", columns);

            element.put("children", child.keySet().isEmpty() ? Collections.emptyList() : Collections.singletonList(new HashMap<>(child)));

            child.put("dataListId", dataList.getId());
            child.put("foreignKeyFilter", table.get("foreignKeyFilter"));

            result.add(0, element);
        }

        return result;
    }

    @Nonnull
    protected Optional<DataList> optDataList(@Nonnull String dataListId) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) applicationContext.getBean("datalistDefinitionDao");
        final DataListService dataListService = (DataListService) applicationContext.getBean("dataListService");

        // get dataList definition
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(dataListId, appDefinition);
        return Optional.ofNullable(datalistDefinition)
                .map(DatalistDefinition::getJson)
                .map(it -> AppUtil.processHashVariable(it, null, null, null))
                .map(dataListService::fromJson)
                .filter(this::isAuthorize); // check permission
    }

    public Map<String, String>[] getPropertyGrid(String property) {
        return Optional.ofNullable(getProperty(property))
                .map(o -> (Object[]) o)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(o -> (Map<String, String>) o)
                .toArray(Map[]::new);
    }

    protected boolean isAuthorize(@Nonnull DataList dataList) {
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final DataListService dataListService = (DataListService) applicationContext.getBean("dataListService");
        final boolean isPermissionSet = dataList.getPermission() != null;
        return !isPermissionSet && isDefaultUserToHavePermission() || isPermissionSet && dataListService.isAuthorize(dataList);
    }

    /**
     * Is default user to be able to access API when permission is not set
     *
     * @return
     */
    protected boolean isDefaultUserToHavePermission() {
        return !WorkflowUtil.isCurrentUserAnonymous();
    }
}
