'use strict';

/* App Module */

angular.module('logviewer', ['logviewerServices']).//, 'logviwerFilters']).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/:folder/:file', {templateUrl: 'partials/log-detail.html', controller: LogDetailCtrl}).
            otherwise({redirectTo: '/start/start'});
    }]);
