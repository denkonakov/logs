'use strict';

/* Controllers */
function LogDetailCtrl($scope, $timeout, $routeParams, Folders, LogLines) {
    $scope.log = LogLines.query({folder: $routeParams.folder, file: $routeParams.file, type: $scope.type, line: $scope.lines});
    $scope.folders = Folders.query();
    $scope.tick = 'none';
    $scope.type = 'tail';
    $scope.lines = 100;

    function tick() {
        if ($scope.tick != 'none') {
            $scope.log = LogLines.query({folder: $routeParams.folder, file: $routeParams.file, type: $scope.type, line: $scope.lines});
            $timeout(tick, $scope.tick);
        } else {
            $timeout.cancel();
        }
    }

    $scope.ticker = function () {
        $timeout(tick, $scope.tick);
    };

    $scope.fetch = function () {
       $scope.log = LogLines.query({folder: $routeParams.folder, file: $routeParams.file, type: $scope.type, line: $scope.lines});
    };

    $scope.navClass = function (page) {
        return page === $routeParams.name ? 'active' : '';
    };
}