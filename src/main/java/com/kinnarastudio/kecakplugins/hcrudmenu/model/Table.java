package com.kinnarastudio.kecakplugins.hcrudmenu.model;

import org.joget.apps.datalist.model.DataList;
import org.joget.apps.form.model.Form;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public int getDepth() {
        return parent == null ? 0 : (parent.getDepth() + 1);
    }

    @Nullable
    public Table getParent() {
        return parent;
    }
}
