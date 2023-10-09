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

    private final Form createForm;
    private final Form editForm;

    private final String foreignKeyFilter;

    private final boolean readonly;

    @Nullable
    private final Table parent;

    public Table(DataList dataList, String foreignKeyFilter, Form createForm, Form editForm, Table parent, boolean readonly) {
        this.dataList = dataList;
        this.foreignKeyFilter = foreignKeyFilter;
        this.createForm = createForm;
        this.editForm = editForm;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.readonly = readonly;
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

    public boolean isReadonly() {
        return readonly;
    }

    public Form getCreateForm() {
        return createForm;
    }

    public Form getEditForm() {
        return editForm;
    }
}
