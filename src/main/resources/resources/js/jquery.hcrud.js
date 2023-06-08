(function( $ ){
    var contextPath;

    var methods = {
        init: function() {
            return this.each(function(){
                let thisObj = $(this);
                let $selectboxElement = $(this).find('select');
                let $editButton = $(this).find('.edit-selection');

                $selectboxElement.change(function(){
                    let value = $selectboxElement.val();
                    if(value == 'addNewOption'){
                        methods.add.apply(thisObj);
                        $selectboxElement.html('');
                        return false;
                    }
                });

                $editButton.click(function(){
                    let value = $selectboxElement.val();
                    if(value && value != '') {
                        methods.edit.apply(thisObj);
                    }
                    return false;
                });
            });
        },

        getFrameId: function(id) {
            return "crudSelectBoxFrame_" + id;
        },

        initPopupDialog: function(args){
            contextPath = args.contextPath;
            let elementId = $(this).attr("id");
            let frameId = methods.getFrameId(elementId);

            let width = $(this).find('.width').val();
            let height = $(this).find('.height').val();

            JPopup.create(frameId, args.title, width, height);
        },

        add: function() {
            let thisObj = $(this);
            return this.each(function(){
                let id = thisObj.attr('id');
                let formUrl = thisObj.find('.formUrl').val()
                let title = "Add Entry";
                let formJson = thisObj.find('.json').val();
                let nonce = thisObj.find('.nonceForm').val();
                let height = thisObj.find('.height').val();
                let width = thisObj.find('.width').val();
                let callback = thisObj.attr('id')+"_add";
                methods.popupForm(id, formUrl, title, formJson, nonce, callback, "{}", {}, height, width);
            });
        },

        edit: function() {
            let thisObj = $(this);
            return this.each(function() {
                let $selectboxElement = $(thisObj).find('select');
                let value = $selectboxElement.val();
                methods.popUpEdit(thisObj, value);
            });
        },

        linkView: function(value) {
            methods.popUpEdit($(this), value);
        },

        popUpEdit:function(thisObj, value){
            let jsonForm = JSON.parse(thisObj.find('.json').val());
            let nonce = thisObj.find('.nonceForm').val();
            let fieldId = $(thisObj).find('input.fieldId').val();
            let url = thisObj.find('.serviceUrl').val();

            $.ajax({
                url : url,
                contentType: "application/json; charset=utf-8",
                dataType: 'json',
                type: "GET",
                data : {
                    formDefId : jsonForm.properties.id,
                    tableName :  jsonForm.properties.tableName,
                    fieldId : fieldId,
                    value : value,
                    nonce : nonce
                },
                success : function(data){
                    let row = data.results[0];
                    let elementId = thisObj.attr('id');
                    let formUrl = thisObj.find('.formUrl').val();
                    let height = thisObj.find('.height').val();
                    let width = thisObj.find('.width').val();
                    let allowEdit = thisObj.find('.allowEdit').val();
                    let title = (allowEdit ? "Edit" : "View") + " Entry";
                    let callback = thisObj.attr('id')+"_edit";
                    methods.popupForm(elementId, formUrl, title, JSON.stringify(jsonForm), nonce, callback, "{rowId:'" + row.id + "'}", row, height, width);
                }
            });
        },

        popupForm: function(id, url, title, json, nonce, callback, setting, jsonData, height, width){
            if (jsonData) {
                if (jsonData.id) {
                    if (url.indexOf("?") != -1) {
                        url += "&";
                    } else {
                        url += "?";
                    }
                    url += "id=" + jsonData.id;
                }
                url += UI.userviewThemeParams();
            }

            var params = {
                _json : json,
                _callback : callback,
                _setting : setting,
                _jsonFormData : JSON.stringify(jsonData),
                _nonce : nonce
            };

            var frameId = methods.getFrameId(id);
            JPopup.show(frameId, url, params, title, width, height);
        },

        updateRow: function(args){
            let data = JSON.parse(args.result);
            return $(this).each(function(){
                let elementId = $(this).attr('id');
                let frameId = methods.getFrameId(elementId);
                let $selectboxElement = $(this).find('select');
                let url = $(this).find('.serviceUrl').val();
                let formDefId = $(this).find('.formDefId').val();
                let fieldId = $(this).find('.fieldId').val();
                let grouping = $(this).find('.grouping').val();
                let primaryKey = data['id'];
                let nonce = $(this).find('.nonceService').val();

                $.ajax({
                    url :url,
                    contentType: "application/json; charset=utf-8",
                    dataType: 'json',
                    type: "GET",
                    data : {
                        formDefId : formDefId,
                        fieldId :  fieldId,
                        grouping : grouping,
                        value : primaryKey,
                        nonce : nonce,
                        page : 0
                    },
                    success:function(data){
                        if(data && data.results) {
                            data.results.forEach(function(row) {
                                let newOption = new Option(row.text, row.id, true, true);
                                $selectboxElement.append(newOption);
                            });

                            $selectboxElement.trigger('change');
                        }
                    }
                });

                JPopup.hide(frameId);

            });
        },


    };

    $.fn.hcrudtable = function( arguments ) {

        if ( methods[method] ) {
            return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.crudselectbox' );
        }

    };

})( jQuery );

