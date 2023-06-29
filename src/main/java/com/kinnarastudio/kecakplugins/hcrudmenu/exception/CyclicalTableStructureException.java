package com.kinnarastudio.kecakplugins.hcrudmenu.exception;

import com.kinnarastudio.kecakplugins.hcrudmenu.model.Table;

public class CyclicalTableStructureException extends Exception {
    final Table table;

    public CyclicalTableStructureException(Table table) {
        super("Cyclical table [" + table.getDataList().getId() + "]");
        this.table = table;
    }
}
