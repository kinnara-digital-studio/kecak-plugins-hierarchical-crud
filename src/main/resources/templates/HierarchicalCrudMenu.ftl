<#-- <script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery/dist/jquery.min.js"></script> -->

<#-- datatables -->
<link href="${request.contextPath}/plugin/${className}/node_modules/datatables.net-dt/css/jquery.dataTables.min.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/datatables.net/js/jquery.dataTables.js"></script>

<#-- jquery-ui -->
<link href="${request.contextPath}/plugin/${className}/node_modules/jquery-ui/themes/base/tabs.css" rel="stylesheet"/>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/jquery-ui/dist/jquery-ui.js"></script>

<#-- main datalist loop -->
<#list tables as table>

    <#assign dataListId=table.id>
    <#assign dataListLabel=table.label>

    <h1>${dataListLabel!}</h1>
    <table id="${table?index}_${dataListId!}" class="display" style="width:100%">
        <thead>
            <tr>
                <th>_id</th>
                <#list table.columns as column>
                    <th>${column.label!}</th>
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
</#list>

<script type="text/javascript">
    $(document).ready(function () {
        <#list tables as table>
            <#assign dataListId = table.id>
            <#assign initVariable = 'init_' + table.id>
            <#assign dataTableVariable = 'dataTable_' + table.id>

            var ${initVariable} = false;

            var ${dataTableVariable} = $('#${table?index}_${dataListId!}').DataTable({
                ajax: {
                    url: '${request.contextPath}/web/json/data/app/${appId!}/${appVersion}/datalist/${dataListId!}',
                    dataSrc: 'data'
                },
                columns: [
                    { data : '_id', visible: false, searchable: false },
                    <#list table.columns as column>
                        { data : '${column.name!}' } <#if column?has_next>,</#if>
                    </#list>
                ],
                initComplete: function() {
                    ${initVariable} = true;
                }
            });

            $('#${table?index}_${dataListId!} tbody').on('click', 'tr', function () {
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

        function loadChildDataTableRows(parentDataTable, childDataTable, parameter, parentRowId) {
            let url = childDataTable.ajax.url().replace(/\?.+/, '') + '?' + parameter + '=' + parentRowId;
            childDataTable.ajax.url(url);
            childDataTable.ajax.reload();
        }
    });
</script>