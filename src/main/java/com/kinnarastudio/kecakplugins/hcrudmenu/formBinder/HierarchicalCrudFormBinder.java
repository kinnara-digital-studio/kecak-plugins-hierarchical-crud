package com.kinnarastudio.kecakplugins.hcrudmenu.formBinder;

import com.kinnarastudio.commons.Declutter;
import com.kinnarastudio.commons.Try;
import com.kinnarastudio.commons.jsonstream.JSONStream;
import com.kinnarastudio.commons.jsonstream.model.JSONObjectEntry;
import com.kinnarastudio.kecakplugins.hcrudmenu.service.MapUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aristo
 */
public class HierarchicalCrudFormBinder extends WorkflowFormBinder
        implements FormLoadElementBinder, FormStoreElementBinder, Declutter {

    public final static String ROW_KEY = "_jsonrow";

    private final static String LABEL = "Hcrud Form Binder";

    /**
     * Binder will be executed when submitting the attachment form.
     * Rows will be converted to json
     *
     * @param element
     * @param submittedRows assume the FormRowSet is not multirow
     * @param formData
     * @return
     */
    @Override
    public FormRowSet store(Element element, final FormRowSet submittedRows, FormData formData) {
        final String formDefId = element.getPropertyString(FormUtil.PROPERTY_ID);
        final Form form = MapUtils.optForm(formDefId).orElse(null);

        final FormRow dbEntry = Optional.of(element)
                .map(formData::getLoadBinderData)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .orElseGet(FormRow::new);

        dbEntry.forEach((key, dbValue) -> submittedRows.forEach(submittedRow -> {
            if (!submittedRow.containsKey(key)) {
                submittedRow.setProperty(String.valueOf(key), String.valueOf(dbValue));
            }
        }));

        return super.store(form, submittedRows, formData);
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        final HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        final String key = Optional.of(ROW_KEY)
                .map(request::getParameter)
                .filter(this::isNotEmpty)
                .map(Try.onFunction(JSONObject::new))
                .map(json -> JSONStream.of(json, Try.onBiFunction(JSONObject::get))
                        .collect(Collectors.toMap(JSONObjectEntry::getKey, JSONObjectEntry::getValue, this::overrideValue, FormRow::new)))
                .map(FormRow::getId)
                .orElse(primaryKey);

        final String formDefId = element.getPropertyString(FormUtil.PROPERTY_ID);
        final Form form = MapUtils.optForm(formDefId).orElse(null);
        final FormRowSet load = Optional.ofNullable(super.load(form, key, formData))
                .orElseGet(() -> {
                    // new record
                    FormRowSet rowSet = new FormRowSet();

                    final FormRow row = new FormRow();
                    rowSet.add(row);

                    return rowSet;
                });

        Optional.of("_foreignkey")
                .map(formData::getRequestParameter)
                .map(Try.onFunction(JSONObject::new))
                .ifPresent(Try.onConsumer(json -> {
                    final String field = json.getString("field");
                    final String value = json.getString("value");

                    load.stream().findFirst().ifPresent(r -> r.setProperty(field, value));
                }));

        return load;
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

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    protected <T> T overrideValue(T oldVal, T newVal) {
        return newVal;
    }

}
