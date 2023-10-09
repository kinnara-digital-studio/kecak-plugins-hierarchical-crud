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

<style>
    .dataTables_length label {
        width: 180px
    }

    .dataTables_length label select {
        width: 60px
    }

    .hcrud-btn-hide {
        display: none
    }
</style>


<#assign fooValue = "${random!'putSomeRandomValue'}" >

<#list levels as tables>
    <div id="hcrud-tabs-${tables?index}" class="hcrud-tabs" data-hcrud-level="${tables?index}">
        <ul>
            <#list tables as table>
                <#assign elementId=table.id>
                <#assign dataListId=table.dataListId>
                <#assign dataListLabel=table.label>

                <li id="${elementId}_tab" class="hcrud-tab" data-hcrud-id="${elementId}" data-hcrud-level="${tables?index}" data-hcrud-parent="${table.parent!}"><a href="#${elementId}_tabcontent">${dataListLabel}</a></li>
            </#list>
        </ul>

        <#list tables as table>
            <#assign elementId=table.id>
            <#assign dataListId=table.dataListId>
            <#assign dataListLabel=table.label>

            <div id="${elementId}_tabcontent" class="hcrud-tabcontent" data-hcrud-id="${elementId}" data-hcrud-level="${tables?index}" data-hcrud-parent="${table.parent!}">
                <input type="hidden" disabled="disabled" id="formUrl" value="${request.contextPath}/web/app/${appId}/${appVersion}/form/embed?_submitButtonLabel=${table.submitButtonLabel!'Submit'}" />
                <input type="hidden" disabled="disabled" id="createFormJson" value="${table.jsonCreateForm}" />
                <input type="hidden" disabled="disabled" id="editFormJson" value="${table.jsonEditForm}" />
                <input type="hidden" disabled="disabled" id="nonce" value="${table.nonce}" />
                <input type="hidden" disabled="disabled" id="height" value="${table.height}" />
                <input type="hidden" disabled="disabled" id="width" value="${table.width}" />

                <#if table.foreignKey?? >
                    <input type='hidden' disabled id='foreignKey' name='foreignKey' value='${fooValue}'>
                </#if>

                <table id="${elementId}" class="display" style="width:100%" data-hcrud-id="${elementId}" data-hcrud-level="${tables?index}" data-hcrud-parent="${table.parent!}" data-hcrud-foreignKey="${table.foreignKey!}" data-hcrud-children="${table.children?map(child -> child.id)?join(' ')}">
                    <thead>
                        <tr>
                            <th>_id</th>
                            <th></th>

                            <#if table.deletable!false >
                                <th></th>
                            </#if>

                            <#list table.columns as column>
                                <th name='${column.name}' data-hcrud-filter='${column.filter!}' >${column.label!}</th>
                            </#list>
                        </tr>
                    </thead>
                    <tbody></tbody>
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
                    autoWidth: false,
                    dom: 'l<"clear">Bfrtip',
                    buttons: [
                        {
                            text: '<i class="fa fa-refresh "/>',
                            attr : { disabled: ${(tables?index != 0)?string} },
                            action: function ( e, dt, node, config ) {
                                dt.ajax.reload();
                            }
                        }

                        <#if table.createFormId?? && table.deletable!false >
                            ,{
                                text: '<i class="fa fa-file"/>',
                                attr : { disabled: ${(tables?index != 0)?string} },
                                action: function ( e, dt, node, config ) {
                                    // create new record

                                    let $table = $(dt.table().node());
                                    let $tabcontent = $table.parents('.hcrud-tabcontent');
                                    let formUrl = $tabcontent.find('input#formUrl').val();
                                    let jsonForm = JSON.parse($tabcontent.find('input#createFormJson').val());
                                    let nonce = $tabcontent.find('input#nonce').val();
                                    let callback = 'onFormSubmitted';
                                    let elementId = dt.table().node().id;
                                    let jsonSetting = { elementId : elementId };
                                    let jsonData = { };

                                    <#if table.parent ??>
                                        let foreignKeyField = $table.attr('data-hcrud-foreignKey');
                                        let foreignKeyValue = $tabcontent.find('input#foreignKey').val();
                                        let jsonFk = { field : foreignKeyField, value : foreignKeyValue};

                                        jsonSetting['fkfield'] = foreignKeyField;
                                        jsonSetting['fkvalue'] = foreignKeyValue;

                                        if(foreignKeyValue == '${fooValue}') {
                                            alert('Please choose parent data');
                                            return;
                                        }
                                    <#else>
                                        let jsonFk = {};
                                    </#if>

                                    let height = $tabcontent.find('input#height').val();
                                    let width = $tabcontent.find('input#width').val();

                                    popupForm(elementId, formUrl, jsonForm, nonce, callback, jsonSetting, jsonData, jsonFk, height, width);
                                }
                            }
                        </#if>
                    ],
                    ajax: {
                        url: '${request.contextPath}/web/json/data/app/${appId!}/${appVersion}/datalist/${dataListId!}',
                        error: (err) => { debugger; },
                        data: function(data, setting) {
                            data.rows = $('div#${elementId}_length select').val();
                            data.page = $('div#${elementId}_paginate a.current').attr('data-dt-idx');

                            if(data.order.length) {
                                let order = data.order[0];
                                let columnIdx = order.column;
                                if(columnIdx > 1) {
                                    let column = data.columns[columnIdx].data;
                                    data.sort = column;
                                    data.desc = order.dir == 'desc';
                                }
                            }

                            let cell = $('#${elementId} .filters th');
                            let input = $(cell).find('input');

                            <#if table.foreignKey?? >
                                let $foreignKeyFilter = $('#${elementId}_tabcontent input#foreignKey');
                                data.${table.foreignKey} = $foreignKeyFilter.val();
                            </#if>

                            $(cell).each(function() {
                                let name = $(this).attr('name');
                                let isForeignKey = name == "${table.foreignKey!''}";
                                if(!isForeignKey) {
                                    let $filter = $(this).find('input');
                                    let value = $filter.val();

                                    if(name && value) {
                                        data[name] = value;
                                    }
                                }
                            });
                        },
                        dataSrc: function(response) {
                            response.recordsTotal = response.recordsFiltered = response.total;
                            return response.data;
                        }
                    },
                    columns: [
                        { data : '_id', visible: false, searchable: false },

                        <#if table.editFormId?? >
                            {
                                data: null,
                                className: 'dt-center inlineaction inlineaction-edit',
                                defaultContent: '<i class="fa <#if table.editable!false>fa-pencil<#else>fa-eye</#if>" />',
                                width : '12',
                                orderable: false
                            },
                        </#if>

                        <#if table.createFormId?? && table.deletable!false >
                            {
                                data: null,
                                className: 'dt-center inlineaction inlineaction-delete',
                                defaultContent: '<i class="fa fa-trash"/>',
                                width : '12',
                                orderable: false
                            },
                        </#if>

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

                    let formUrl = $('#${elementId}_tabcontent input#formUrl').val();
                    let jsonForm = JSON.parse($('#${elementId}_tabcontent input#editFormJson').val());
                    let nonce = $('#${elementId}_tabcontent input#nonce').val();
                    let callback = 'onFormSubmitted';
                    let jsonSetting = { 'elementId' : '${elementId}' };
                    let primaryKey = ${dataTableVariable}.row(this).data()._id;
                    let jsonData = { id : primaryKey };
                    let jsonFk = { };
                    let height = $('#${elementId}_tabcontent input#height').val();
                    let width = $('#${elementId}_tabcontent input#width').val();

                    popupForm('${elementId}', formUrl, jsonForm, nonce, callback, jsonSetting, jsonData, jsonFk, height, width)
                });

                $('#${elementId}').on('click', 'td.inlineaction-delete', function (e) {
                    e.preventDefault();

                    let dt = ${dataTableVariable};
                    let elementId = dt.table().node().id;
                    let primaryKey = dt.row().data()._id;

                    deleteFormData('${table.createFormId!''}', primaryKey, dt);
                });

                $('#${elementId} tbody').on('click', 'tr', function () {
                    if ($(this).hasClass('selected')) {
                        $(this).removeClass('selected');
                        loadChildDataTableRows(${dataTableVariable},  '${fooValue}');
                    } else {
                        ${dataTableVariable}.$('tr.selected').removeClass('selected');
                        $(this).addClass('selected');

                        let rowId = ${dataTableVariable}.row(this).data()._id;
                        loadChildDataTableRows(${dataTableVariable},  rowId);
                    }
                });

            </#list>

        </#list>


        $('li.hcrud-tab').on('click', function(event) {
            let $tab = $(this);
            showChildren($tab, false);
        });

        let $tab = $('.hcrud-tabs li').filter(':eq(0)');
        showChildren($tab, false);
    });

    function showChildren($parentTab, collapse) {
        debugger;

        let parentId = $parentTab.data('hcrud-id');
        let level = parseInt($parentTab.data('hcrud-level'));

        let $childTabs = $('.hcrud-tabs').filter(function() {
            return $(this).data('hcrud-level') > level;
        });

        let $childrenTab = $childTabs.find('.hcrud-tab:data[data-hcrud-parent="' + parentId + '"]');

        // if parent has any child, show tabs
        if($childrenTab.length) {
            $childTabs.show();
            $childrenTab.show();
            $childTabs.tabs({active : false});
        } else {
            $childTabs.hide();
        }

        // let $childrenTabcontent = $childTabs.find('.hcrud-tabcontent:data[data-hcrud-parent="' + parentId + '"]');
        // $childrenTabcontent.tabs({active: true});

        let $nephewsTab = $childTabs.find('.hcrud-tab:data[data-hcrud-parent!="' + parentId + '"]');
        $nephewsTab.hide();

        // let $nephewsTabcontent= $childTabs.find('.hcrud-tabcontent:data[data-hcrud-parent!="' + parentId + '"]');
        // $nephewsTabcontent.tabs({active: false});

        let $grandchildTabs = $('.hcrud-tabs').filter(function() {
            return $(this).data('hcrud-level') > (level + 1);
        });
        $grandchildTabs.hide();

        // let $firstChild = $children.filter('li').filter(':eq(0)');
        // $firstChild.each((i, e) => showChildren($(e, false)));
    }

    function loadChildDataTableRows(parentDataTable, parentRowId) {
        debugger;
        let parentId = parentDataTable.table().node().id;

        $('table[data-hcrud-parent="' + parentId + '"]').each(function(i, table) {
            let $childTable = $(table);
            let $tabcontent = $childTable.parents('.hcrud-tabcontent');

            let disable = parentRowId == '${fooValue}';
            $tabcontent.find('.dt-buttons button').attr('disabled', disable);

            let childDataTable = $childTable.DataTable();
            let foreignKey = $childTable.attr('data-hcrud-foreignKey');

            let $inputForeignKey = $tabcontent.find('input#foreignKey');
            $inputForeignKey.val(parentRowId);

            childDataTable.ajax.reload();

            loadChildDataTableRows(childDataTable, '${fooValue}');
        });
    }

    function popupForm(elementId, formUrl, jsonForm, nonce, callback, jsonSetting, jsonData, jsonFk, height, width) {
        debugger;
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
            _jsonrow : JSON.stringify(jsonData),
            _nonce : nonce,
            _foreignkey : JSON.stringify(jsonFk)
        };

        var frameId = getFrameId();
        JPopup.show(frameId, formUrl, params, "", width, height);
    }
    function getFrameId() {
        return 'hcrudFrame';
    }

    function onFormSubmitted(args) {
        let result = JSON.parse(args.result);
        let elementId = args.elementId;
        let dt = $('#' + elementId).DataTable();
        dt.ajax.reload();
        let frameId = getFrameId();
        JPopup.hide(frameId);
    }

    function deleteFormData(formDefId, primaryKey, dt) {
        if(confirm( "Are you sure you want to delete the selected rows?" )) {
            $.ajax({
                url: '${request.contextPath}/web/json/data/app/${appId!}/${appVersion}/form/' + formDefId + '/' + primaryKey,
                type: 'DELETE',
                success: function(result) {
                    // Do something with the result
                    dt.ajax.reload();
                }
            });
        }
    }
</script>