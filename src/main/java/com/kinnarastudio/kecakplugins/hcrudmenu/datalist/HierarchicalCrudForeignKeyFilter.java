package com.kinnarastudio.kecakplugins.hcrudmenu.datalist;

import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.userview.model.PwaOfflineResources;

import java.util.Set;

public class ForeignKey extends DataListFilterTypeDefault implements PwaOfflineResources {
    @Override
    public String getTemplate(DataList dataList, String s, String s1) {
        return null;
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String s) {
        return null;
    }

    @Override
    public Set<String> getOfflineStaticResources() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }
}
