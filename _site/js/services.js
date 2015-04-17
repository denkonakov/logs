'use strict';

/* Services */
angular.module('logviewerServices', ['ngResource']).
    factory('Folders',function ($resource) {
        return $resource('/_logs/as_files', {}, {
            query: {method: 'GET', params: {}, isArray: true}
        });
    }).
    factory('LogLines',function ($resource) {
        return $resource('/_logs/:folder/:file', {}, {
            query: {method: 'GET', params: {folder: 'start', file: 'start', type: 'tail', lines: '100'}, isArray: true}
        });
    });
