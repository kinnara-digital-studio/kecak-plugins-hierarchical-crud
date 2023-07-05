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

    public final static String ROW_KEY = "jsonrow";

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
            if(!submittedRow.containsKey(key)) {
                submittedRow.setProperty(String.valueOf(key), String.valueOf(dbValue));
            }
        }));

        FormRowSet store = super.store(form, submittedRows, formData);
        return store;
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        final HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        final String key = Optional.of(ROW_KEY)
                .map(request::getParameter)
                .filter(this::isNotEmpty)
                .map(Try.onFunction(JSONObject::new))
                .map(json -> JSONStream.of(json, Try.onBiFunction(JSONObject::get))
                        .collect(Collectors.toMap(JSONObjectEntry::getKey, JSONObjectEntry::getValue, (oldValue, newValue) -> newValue, FormRow::new)))
                .map(FormRow::getId)
                .orElse(primaryKey);

        final String formDefId = element.getPropertyString(FormUtil.PROPERTY_ID);
        final Form form = MapUtils.optForm(formDefId).orElse(null);
        FormRowSet load = super.load(form, key, formData);
        if(load.isEmpty()) {
            load.add(new FormRow());
        }
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
}
