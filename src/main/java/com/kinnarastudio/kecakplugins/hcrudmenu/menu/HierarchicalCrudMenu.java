package com.kinnarastudio.kecakplugins.hcrudmenu.menu;

import com.kinnarastudio.kecakplugins.hcrudmenu.model.Table;
import com.kinnarastudio.kecakplugins.hcrudmenu.service.MapUtils;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.model.Form;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

//        dataModel.put("random", new Random().ints().findFirst().orElse(0));
        dataModel.put("random", 0);

        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        if (appDefinition != null) {
            dataModel.put("appId", appDefinition.getAppId());
            dataModel.put("appVersion", appDefinition.getVersion());

            final List<List<Map<String, Object>>> levels = getLevel().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getValue().stream().map(MapUtils::toMap).collect(Collectors.toList()))
                    .collect(Collectors.toList());

            dataModel.put("levels", levels);
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
        final boolean showRowCount = "true".equalsIgnoreCase(getPropertyString("rowCount"));

        final Map<String, String>[] propertyTables = getPropertyGrid("tables");

        if (showRowCount) {
            final String rowCount = Arrays.stream(propertyTables)
                    .filter(p -> p.getOrDefault("parentDataListId", "").isEmpty())
                    .map(p -> p.getOrDefault("dataListId", ""))
                    .map(this::optDataList)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(DataList::getTotal)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String label = getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }
            return "<a href=\"" + getUrl() + "\" class=\"menu-link default\"><span>" + label + "</span> <span class='rowCount'>(" + rowCount + ")</span></a>";
        }
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

    @Nullable
    protected Table getTable(Map<String, String> properties, final Map<String, Table> memo) {
        final String dataListId = properties.getOrDefault("dataListId", "");
        final String createFormId = properties.getOrDefault("createFormId", "");
        final String editFormId = properties.getOrDefault("editFormId", "");
        final String foreignKeyFilter = properties.getOrDefault("foreignKeyFilter", "");
        final String parentDataListId = properties.getOrDefault("parentDataListId", "");
        final String editFormButtonLabel = properties.getOrDefault("editFormButtonLabel", "");
        final boolean isReadonly = "true".equalsIgnoreCase(properties.get("readonly"));

        return getTable(dataListId, foreignKeyFilter, createFormId, editFormId, parentDataListId, editFormButtonLabel, isReadonly, memo);
    }

    @Nullable
    protected Table getTable(String dataListId, String foreignKeyFilter, String createFormDefId, String editFormDefId, String parentDataListId, String editFormButtonLabel, boolean isReadonly, final Map<String, Table> memo) {
        return Optional.of(dataListId)
                .map(memo::get)
                .orElseGet(() -> {
                    final Optional<DataList> optDataList = optDataList(dataListId);
                    final Optional<Form> optCreateForm = MapUtils.optForm(createFormDefId);
                    final Optional<Form> optEditForm = MapUtils.optForm(editFormDefId);

                    if (!optDataList.isPresent()) {
                        return null;
                    }

                    final DataList dataList = optDataList.get();

                    final Table parentTable = getRowProperties(parentDataListId)
                            .map(m -> getTable(m, memo))
                            .filter(parent -> !isCyclical(dataList.getId(), parent))
                            .orElse(null);

                    final String editButtonLabel = Optional.ofNullable(editFormButtonLabel)
                            .filter(s -> !s.isEmpty())
                            .orElse(isReadonly ? "Close" : "Submit");

                    final Table childTable = new Table(dataList, foreignKeyFilter, optCreateForm.orElse(null), optEditForm.orElse(null), editButtonLabel, parentTable, isReadonly);

                    if (parentTable != null) {
                        parentTable.addChild(childTable);
                    }

                    memo.put(dataListId, childTable);

                    return childTable;
                });
    }

    protected boolean isCyclical(String id, Table table) {
        if (id.equals(table.getDataList().getId())) {
            return true;
        }

        final Table parent = table.getParent();
        if (parent == null) {
            return false;
        } else {
            return isCyclical(id, table.getParent());
        }
    }

    protected Optional<Map<String, String>> getRowProperties(String dataListId) {
        return Arrays.stream(getPropertyGrid("tables"))
                .filter(m -> dataListId.equals(m.get("dataListId")))
                .findAny();
    }

    protected Map<Integer, Collection<Table>> getLevel() {
        final Map<Integer, Collection<Table>> levels = new HashMap<>();
        final Map<String, Table> memo = new HashMap<>();

        final Map<String, String>[] propertyTables = getPropertyGrid("tables");
        for (Map<String, String> propertyRow : propertyTables) {

            final Table table = getTable(propertyRow, memo);
            if (table == null) {
                continue;
            }

            final int level = table.getDepth();
            if (!levels.containsKey(level)) {
                levels.put(level, new ArrayList<>());
            }

            levels.get(level).add(table);
        }

        return levels;
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
