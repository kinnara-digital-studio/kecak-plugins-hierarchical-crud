<#-- <script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery/dist/jquery.min.js"></script> -->
<#-- <script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery/dist/jquery.min.js"></script> -->

<#-- datatables -->
<link href="${request.contextPath}/plugin/${className}/node_modules/datatables.net-dt/css/jquery.dataTables.min.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/datatables.net/js/jquery.dataTables.js"></script>

<#-- jquery-ui -->
<link href="${request.contextPath}/plugin/${className}/node_modules/jquery-ui/themes/base/tabs.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery-ui/dist/jquery-ui.js"></script>

<#-- typewatch -->
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery.typewatch/jquery.typewatch.js"></script>

<#list levels as tables>
    <div id="hcrud-tabs-${tables?index}">
        <ul>
            <#list tables as table>
                <#assign elementId=table?index + '_' + table.id>
                <#assign dataListId=table.id>
                <#assign dataListLabel=table.label>
                <li><a href="#${elementId}_wrapper">${dataListLabel}</a></li>
            </#list>
        </ul>

        <#list tables as table>
            <#assign elementId=table?index + '_' + table.id>
            <#assign dataListId=table.id>
            <#assign dataListLabel=table.label>

            <div id="${elementId}_wrapper">
                <h1>${dataListLabel!}</h1>
                <table id="${elementId}" class="display" style="width:100%">
                    <thead>
                        <tr>
                            <th>_id</th>
                            <#list table.columns as column>
                                <th name='${column.name}' data-kecak-filter='${column.filter!}' >${column.label!}</th>
                            </#list>
                        </tr>
                    </thead>
                    <tbody></tbody>
                    <tfoot>
                        <th>_id</th>
                        <tr>
                            <#list table.columns as column>
                                <th>${column.label!}</th>
                            </#list>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </#list>
    </div>
</#list>

<script type="text/javascript">
    $(document).ready(function () {
        <#list levels as tables>

            $("#hcrud-tabs-${tables?index}").tabs({
                collapsible: true,
                active: false
            });

            <#list tables as table>
                <#assign elementId = table?index + '_' + table.id>
                <#assign dataListId = table.id>
                <#assign initVariable = 'init_' + table.id>
                <#assign dataTableVariable = 'dataTable_' + table.id>

                let ${initVariable} = false;

                $('#${elementId} thead tr').clone(true).addClass('filters').appendTo('#${elementId} thead');

                let ${dataTableVariable} = $('#${elementId}').DataTable({
                    orderCellsTop: true,
                    processing: true,
                    serverSide: true,
                    ajax: {
                        url: '${request.contextPath}/web/json/data/app/${appId!}/${appVersion}/datalist/${dataListId!}',
                        data: function(data, setting) {
                            data.rows = $('div#${elementId}_length select').val();
                            data.page = $('div#${elementId}_paginate a.current').attr('data-dt-idx');
                            let cell = $('#${elementId} .filters th');
                            let input = $(cell).find('input');

                            $(cell).each(function() {
                                let name = $(this).attr('name');
                                data[name] = $(this).find('input').val();
                            });

                            debugger;
                        },
                        dataSrc: function(response) {
                            response.recordsTotal = response.recordsFiltered = response.total;
                            return response.data;
                        }
                    },
                    columns: [
                        { data : '_id', visible: false, searchable: false },
                        <#list table.columns as column>
                            { data : '${column.name!}' } <#if column?has_next>,</#if>
                        </#list>
                    ],
                    initComplete: function() {
                        ${initVariable} = true;
                        let api = this.api();
                        api.columns().eq(0).each(function(colIdx) {
                            // Set the header cell to contain the input element
                            let cell = $('#${elementId} .filters th').eq($(api.column(colIdx).header()).index());
                            let filter = $(cell).attr('data-kecak-filter');
                            let style = 'visibility : ' + (filter ? 'visible' : 'hidden');
                            let title = $(cell).text();
                            $(cell).html('<input name="" style="' + style + '" type="text" placeholder="' + title + '" />' );

                            // On every keypress in this input
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

                $('#${elementId} tbody').on('click', 'tr', function () {
                    if ($(this).hasClass('selected')) {
                        $(this).removeClass('selected');
                    } else {
                        ${dataTableVariable}.$('tr.selected').removeClass('selected');
                        $(this).addClass('selected');

                        let rowId = ${dataTableVariable}.row(this).data()._id;
                        <#list table.children as child>
                            <#assign dataTableChildVariable = 'dataTable_' + child.dataListId>
                            loadChildDataTableRows(${dataTableVariable}, ${dataTableChildVariable}, '${child.foreignKeyFilter}', rowId);
                        </#list>
                    }
                });

            </#list>

        </#list>

        function loadChildDataTableRows(parentDataTable, childDataTable, parameter, parentRowId) {
            let url = childDataTable.ajax.url().replace(/\?.+/, '') + '?' + parameter + '=' + parentRowId;
            childDataTable.ajax.url(url);
            childDataTable.ajax.reload();
        }
    });
</script>