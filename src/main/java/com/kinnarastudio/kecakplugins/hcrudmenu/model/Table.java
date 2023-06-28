package com.kinnarastudio.kecakplugins.hcrudmenu.model;

import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.form.model.Form;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table {
    private final DataList dataList;
    private final List<Table> children;

    private final Form form;

    private final String foreignKeyFilter;
    public Table(DataList dataList, String foreignKeyFilter, Form form) {
        this.dataList = dataList;
        this.foreignKeyFilter = foreignKeyFilter;
        this.form = form;
        this.children = new ArrayList<>();
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


    public Map<String, Object> toMap() {
        return toMap(0);
    }
    protected Map<String, Object> toMap(final int depth) {
        final Map<String, Object> map = new HashMap<>();

        map.put("id", dataList.getId());

        map.put("label", dataList.getName());

        map.put("level", depth);

        map.put("formId", Optional.ofNullable(form).map(f -> f.getPropertyString("id")).orElse(""));

        final List<Map<String, String>> columns = Optional.of(dataList)
                .map(DataList::getColumns)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(c -> {
                    final Map<String, String> m = new HashMap<>();
                    m.put("name", c.getName());
                    m.put("label", c.getLabel());
                    return m;
                })
                .collect(Collectors.toList());

        map.put("columns", columns);

        Optional.of(dataList)
                .map(DataList::getFilters)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(f -> foreignKeyFilter.equals(f.getName()))
                .findFirst()
                .ifPresent(f -> map.put("foreignKeyFilter", f.getName()));

        final List<Map<String, Object>> children = getChildren().stream()
                .map(t -> t.toMap(depth + 1))
                .collect(Collectors.toList());

        map.put("children", children);
        return map;
    }

    public Form getForm() {
        return form;
    }
}
