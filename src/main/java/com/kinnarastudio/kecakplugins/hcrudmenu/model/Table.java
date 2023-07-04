package com.kinnarastudio.kecakplugins.hcrudmenu.model;

import org.joget.apps.datalist.model.DataList;
import org.joget.apps.form.model.Form;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {
    private final DataList dataList;
    private final List<Table> children;

    private final Form form;

    private final String foreignKeyFilter;

    @Nullable
    private final Table parent;

    public Table(DataList dataList, String foreignKeyFilter, Form form, Table parent) {
        this.dataList = dataList;
        this.foreignKeyFilter = foreignKeyFilter;
        this.form = form;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public DataList getDataList() {
        return dataList;
    }

    /**
     * Returns immutable list of children
     *
     * @return
     */
    public List<Table> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(Table child) {
        children.add(child);
    }

    public Form getForm() {
        return form;
    }

    public int getDepth() {
        return parent == null ? 0 : (parent.getDepth() + 1);
    }

    @Nullable
    public Table getParent() {
        return parent;
    }

    public String getForeignKeyFilter() {
        return foreignKeyFilter;
    }
}
