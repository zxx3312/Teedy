'use strict';

/**
 * Settings user page controller.
 */
angular.module('docs').controller('SettingsUser', function ($scope, $state, Restangular, $translate, $dialog) {
  $translate.use('zh_CN')
  /**
   * Load users from server.
   */
  $scope.loadUsers = function () {
    Restangular.one('user/list').get({
      sort_column: 1,
      asc: true
    }).then(function (data) {
      $scope.users = data.users;
    });
  };

  $scope.loadUsers();

  /**
   * Edit a user.
   */
  $scope.editUser = function (user) {
    $state.go('settings.user.edit', { username: user.username });
  };

  // 初始化待审批用户列表
  $scope.pendingUsers = [];

  // 获取待审批用户
  Restangular.all('guest/pending_users').customGET().then(function (response) {
    console.log('Fetched pending users response:', response);
    $scope.pendingUsers = (response.pending_users || []).map(user => {
      user.request_date = new Date(Number(user.request_date));
      return user;
    });
    $timeout(function () {
      $scope.$apply(); // 确保视图更新
    });
  }, function (response) {
    var title = $translate.instant('admin.pending_user_error_title');
    var msg = $translate.instant('admin.pending_user_error_message') + (response.data.message ? ': ' + response.data.message : '');
    var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
    $dialog.messageBox(title, msg, btns);
  });

  // $http.get('../api/guest/pending_users').then(function (response) {
  //   console.log('Fetched pending users response:', response.data);
  //   $scope.pendingUsers = (response.data.pending_users || []).map(user => {
  //     user.request_date = new Date(Number(user.request_date));
  //     return user;
  //   });
  //   $timeout(function () {
  //     $scope.$apply();
  //   });
  // }, function (response) {
  //   console.log('Error fetching pending users:', response);
  //   var title = $translate.instant('admin.pending_user_error_title') || 'Error';
  //   var msg = ($translate.instant('admin.pending_user_error_message') || 'Failed to load pending users') +
  //     (response.data && response.data.message ? ': ' + response.data.message : '');
  //   var btns = [{ result: 'ok', label: $translate.instant('ok') || 'OK', cssClass: 'btn-primary' }];
  //   $dialog.messageBox(title, msg, btns);
  // });

  // 审批用户（通过）
  $scope.approveUser = function (user) {
    Restangular.one('guest/approve_user').post('', { id: user.id }).then(function () {
      $scope.pendingUsers = $scope.pendingUsers.filter(u => u.id !== user.id);
      var title = '成功创建用户';
      var msg = '成功';
      var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
      $dialog.messageBox(title, msg, btns);
    }, function (response) {
      var title = $translate.instant('admin.pending_user_error_title');
      var msg = $translate.instant('admin.pending_user_error_message');
      var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
      $dialog.messageBox(title, msg, btns);
    });
  };

  // 拒绝用户
  $scope.rejectUser = function (user) {
    Restangular.one('guest/reject_user').post('', { id: user.id }).then(function () {
      $scope.pendingUsers = $scope.pendingUsers.filter(u => u.id !== user.id);
      var title = $translate.instant('admin.pending_user_rejected_title');
      var msg = $translate.instant('admin.pending_user_rejected_message');
      var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
      $dialog.messageBox(title, msg, btns);
    }, function (response) {
      var title = $translate.instant('admin.pending_user_error_title');
      var msg = $translate.instant('admin.pending_user_error_message') + (response.data.message ? ': ' + response.data.message : '');
      var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
      $dialog.messageBox(title, msg, btns);
    });
  };
});
