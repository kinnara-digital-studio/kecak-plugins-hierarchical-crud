package com.kinnarastudio.kecakplugins.hcrudmenu.datalist;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.plugin.base.PluginManager;

import java.util.ResourceBundle;

public class HierarchicalCrudForeignKeyFilter extends TextFieldDataListFilterType {
    public final static String LABEL = "Hcrud FK Filter";
    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        return "";
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String name) {
        return null;
    }

    @Override
    public String getName() {
        return LABEL;
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
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }
}
