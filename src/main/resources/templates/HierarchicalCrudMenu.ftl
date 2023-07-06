<#-- <script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery/dist/jquery.min.js"></script> -->
<#-- <script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery/dist/jquery.min.js"></script> -->

<#-- datatables -->
<link href="${request.contextPath}/plugin/${className}/node_modules/datatables.net-dt/css/jquery.dataTables.min.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/datatables.net/js/jquery.dataTables.js"></script>

<link href="${request.contextPath}/plugin/${className}/node_modules/datatables.net-buttons-dt/css/buttons.dataTables.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/datatables.net-buttons-dt/js/buttons.dataTables.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/datatables.net-buttons/js/dataTables.buttons.js"></script>

<#-- jquery-ui -->
<link href="${request.contextPath}/plugin/${className}/node_modules/jquery-ui/themes/base/tabs.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery-ui/dist/jquery-ui.js"></script>

<#-- typewatch -->
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery.typewatch/jquery.typewatch.js"></script>

<#list levels as tables>
    <div id="hcrud-tabs-${tables?index}" class="hcrud-tabs">
        <ul>
            <#list tables as table>
                <#assign elementId=table.id>
                <#assign dataListId=table.dataListId>
                <#assign dataListLabel=table.label>
                <li><a href="#${elementId}_wrapper">${dataListLabel}</a></li>
            </#list>
        </ul>

        <#list tables as table>
            <#assign elementId=table.id>
            <#assign dataListId=table.dataListId>
            <#assign dataListLabel=table.label>

            <div id="${elementId}_wrapper" class="hcrud-wrapper">
                <#-- <input type="hidden" disabled="disabled" id="formUrl" value="${table.formUrl}" /> -->
                <input type="hidden" disabled="disabled" id="formUrl" value="${request.contextPath}/web/app/${appId}/${appVersion}/form/embed?_submitButtonLabel=Submit" />
                <input type="hidden" disabled="disabled" id="formJson" value="${table.jsonForm}" />
                <input type="hidden" disabled="disabled" id="nonce" value="${table.nonce}" />
                <input type="hidden" disabled="disabled" id="height" value="${table.height}" />
                <input type="hidden" disabled="disabled" id="width" value="${table.width}" />

                <#if table.foreignKey?? >
                    <input type='hidden' disabled name='${table.foreignKey}'>
                </#if>
                <table id="${elementId}" class="display" style="width:100%" data-hcrud-parent="${table.parent!}" data-hcrud-foreignKey="${table.foreignKey!}" data-hcrud-children="${table.children?map(child -> child.id)?join(' ')}">
                    <thead>
                        <tr>
                            <th>_id</th>

                            <#if table.editable!false >
                                <th></th>
                            </#if>

                            <#if table.deletable!false >
                                <th></th>
                            </#if>

                            <#list table.columns as column>
                                <th name='${column.name}' data-hcrud-filter='${column.filter!}' >${column.label!}</th>
                            </#list>
                        </tr>
                    </thead>
                    <tbody></tbody>

                    <#--
                    <tfoot>
                        <th>_id</th>
                        <tr>
                            <#list table.columns as column>
                                <th>${column.label!}</th>
                            </#list>
                        </tr>
                    </tfoot>
                    -->
                </table>
            </div>
        </#list>
    </div>
</#list>

<script type="text/javascript">
    $(document).ready(function () {
        <#list levels as tables>

            $("#hcrud-tabs-${tables?index}").tabs({
                collapsible: true ${(tables?index == 0)?string('', ',active: false')}
            });

            <#list tables as table>
                <#assign elementId = table.id>
                <#assign dataListId = table.dataListId>
                <#assign initVariable = 'init_' + table.id>
                <#assign dataTableVariable = 'dataTable_' + table.id>

                let ${initVariable} = false;

                $('#${elementId} thead tr').clone(true).addClass('filters').appendTo('#${elementId} thead');

                let ${dataTableVariable} = $('table#${elementId}').DataTable({
                    orderCellsTop: true,
                    processing: true,
                    serverSide: true,
                    searching: false,
                    dom: 'B<"clear">lfrtip',
                    buttons: [{
                        text: 'Create',
                        action: function ( e, dt, node, config ) {
                            debugger;
                            let $table = $(dt.table().node());
                            let $wrapper = $table.parents('.hcrud-wrapper');
                            let formUrl = $wrapper.find('input#formUrl').val();
                            let jsonForm = JSON.parse($wrapper.find('input#formJson').val());
                            let nonce = $wrapper.find('input#nonce').val();
                            let callback = 'onFormSubmitted';
                            let elementId = dt.table().node().id;
                            let jsonSetting = { elementId : elementId };
                            let jsonData = { };

                            <#if table.parent ??>
                                let foreignKeyField = $table.attr('data-hcrud-foreignKey');
                                let foreignKeyValue = $wrapper.find('input[name="' + foreignKeyField + '"]').val();
                                let jsonFk = { field : foreignKeyField, value : foreignKeyValue};
                            <#else>
                                let jsonFk = {};
                            </#if>

                            let height = $wrapper.find('input#height').val();
                            let width = $wrapper.find('input#width').val();

                            popupForm(elementId, formUrl, jsonForm, nonce, callback, jsonSetting, jsonData, jsonFk, height, width);
                        }
                    }],
                    ajax: {
                        url: '${request.contextPath}/web/json/data/app/${appId!}/${appVersion}/datalist/${dataListId!}',
                        data: function(data, setting) {
                            data.rows = $('div#${elementId}_length select').val();
                            data.page = $('div#${elementId}_paginate a.current').attr('data-dt-idx');
                            let cell = $('#${elementId} .filters th');
                            let input = $(cell).find('input');

                            <#if table.foreignKey?? >
                                let $foreignKeyFilter = $('#${elementId}_wrapper input[name="${table.foreignKey}"]');

                                $foreignKeyFilter.each(function(i, e) {
                                    data['${table.foreignKey}'] = $(e).val();
                                });
                            </#if>

                            $(cell).each(function() {
                                let name = $(this).attr('name');
                                data[name] = $(this).find('input').val();
                            });

                            if(${(tables?index != 0)?string} && !${initVariable}) {
                                data.id = ' ';
                            }
                        },
                        dataSrc: function(response) {
                            response.recordsTotal = response.recordsFiltered = response.total;
                            return response.data;
                        }
                    },
                    columns: [
                        { data : '_id', visible: false, searchable: false },

                        <#if table.editable!false >
                            {
                                data: null,
                                className: 'dt-center inlineaction-edit',
                                defaultContent: '<i class="fa fa-pencil"/>',
                                orderable: false
                            },
                        </#if>

                        <#if table.deletable!false >
                            {
                                data: null,
                                className: 'dt-center inlineaction-edit',
                                defaultContent: '<i class="fa fa-trash"/>',
                                orderable: false
                            },
                        </#if>

                        <#-- { data : (r, t, s, m) => r.${table.foreignKey!'_id'}, visible: false, searchable: false }, -->
                        <#list table.columns as column>
                            { data : '${column.name!}' } <#if column?has_next>,</#if>
                        </#list>
                    ],
                    initComplete: function() {
                        ${initVariable} = true;
                        let api = this.api();
                        api.columns().eq(0).each(function(colIdx) {
                            let foreignKey = $('table#${elementId}').attr('data-hcrud-foreignKey');

                            // Set the header cell to contain the input element
                            let cell = $('#${elementId} .filters th').eq($(api.column(colIdx).header()).index());
                            let filter = $(cell).attr('data-hcrud-filter');
                            let name = $(cell).attr('name');
                            let hidden = !(filter && filter != foreignKey);
                            let title = $(cell).text();

                            if(hidden) {
                                $(cell).html('<input name="' + name + '" type="hidden" />' );
                            } else {
                                $(cell).html('<input name="' + name +'" type="text" placeholder="' + title + '" />' );
                            }

                            let input = $(cell).find('input');
                            $(input).typeWatch({
                                wait: 500,
                                highlight: true,
                                allowSubmit: true,
                                captureLength: 1,
                                callback : () => api.ajax.reload()
                            });
                        });
                    }
                });

                $('#${elementId}').on('click', 'td.inlineaction-edit', function (e) {
                    e.preventDefault();

                    let formUrl = $('#${elementId}_wrapper input#formUrl').val();
                    let jsonForm = JSON.parse($('#${elementId}_wrapper input#formJson').val());
                    let nonce = $('#${elementId}_wrapper input#nonce').val();
                    let callback = 'onFormSubmitted';
                    let jsonSetting = { 'elementId' : '${elementId}' };
                    let primaryKey = ${dataTableVariable}.row(this).data()._id;
                    let jsonData = { id : primaryKey };
                    let jsonFk = { };
                    let height = $('#${elementId}_wrapper input#height').val();
                    let width = $('#${elementId}_wrapper input#width').val();

                    popupForm('${elementId}', formUrl, jsonForm, nonce, callback, jsonSetting, jsonData, jsonFk, height, width)
                });

                $('#${elementId} tbody').on('click', 'tr', function () {
                    if ($(this).hasClass('selected')) {
                        $(this).removeClass('selected');
                    } else {
                        ${dataTableVariable}.$('tr.selected').removeClass('selected');
                        $(this).addClass('selected');

                        let rowId = ${dataTableVariable}.row(this).data()._id;
                        loadChildDataTableRows(${dataTableVariable},  rowId);
                    }
                });

            </#list>

        </#list>

        function loadChildDataTableRows(parentDataTable, parentRowId) {
            let parentId = parentDataTable.table().node().id;

            $('table[data-hcrud-parent="' + parentId + '"]').each(function(i, table) {
                let childDataTable = $(table).DataTable();
                let foreignKey = $(table).attr('data-hcrud-foreignKey');

                $(table).parents('.hcrud-wrapper').find('input[name="' + foreignKey + '"]').val(parentRowId);

                childDataTable.ajax.reload();

                loadChildDataTableRows(childDataTable, ' ');
            });
        }

        function popupForm(elementId, formUrl, jsonForm, nonce, callback, jsonSetting, jsonData, jsonFk, height, width) {
            if (jsonData.id) {
                if (formUrl.indexOf("?") != -1) {
                    formUrl += "&";
                } else {
                    formUrl += "?";
                }
                formUrl += "id=" + jsonData.id;
            }
            formUrl += UI.userviewThemeParams();

            var params = {
                _json : JSON.stringify(jsonForm),
                _callback : callback,
                _setting : JSON.stringify(jsonSetting).replace(/"/g, "'"),
                jsonrow : JSON.stringify(jsonData),
                _nonce : nonce,
                _foreignkey : JSON.stringify(jsonFk)
            };

            var frameId = getFrameId();
            debugger;
            JPopup.show(frameId, formUrl, params, "", width, height);
        }
    });

    function getFrameId() {
        return 'hcrudFrame';
    }

    function onFormSubmitted(args) {
        debugger;
        let result = JSON.parse(args.result);
        let elementId = args.elementId;
        let table = $('#' + elementId).DataTable();
        table.ajax.reload();
        let frameId = getFrameId();
        JPopup.hide(frameId);
    }
</script>