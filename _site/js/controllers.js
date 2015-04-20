'use strict';

/* Controllers */
function LogDetailCtrl($scope, $timeout, $routeParams, Folders, Indexes, LogLines) {
    $scope.log = LogLines.query({folder: $routeParams.folder, file: $routeParams.file, type: $scope.type, lines: $scope.lines, index: $scope.index});
    $scope.folders = Folders.query();
    $scope.tick = 'none';
    $scope.type = 'tail';
    $scope.lines = 20;
    $scope.index = 'all';
    $scope.indexes = Indexes.query();

    function tick() {
        if ($scope.tick != 'none') {
            $scope.log = LogLines.query({folder: $routeParams.folder, file: $routeParams.file, type: $scope.type, lines: $scope.lines, index: $scope.index});
            $timeout(tick, $scope.tick);
        } else {
            $timeout.cancel();
        }
    }

    $scope.ticker = function () {
        $timeout(tick, $scope.tick);
    };

    $scope.fetch = function () {
       $scope.log = LogLines.query({folder: $routeParams.folder, file: $routeParams.file, type: $scope.type, lines: $scope.lines, index: $scope.index});
    };

    $scope.navClass = function (page) {
        return page === $routeParams.name ? 'active' : '';
    };
}