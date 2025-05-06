'use strict';

/**
 * Login controller.
 */
angular.module('docs').controller('Login', function (Restangular, $scope, $rootScope, $state, $stateParams, $dialog, User, $translate, $uibModal) {
  $scope.codeRequired = false;
  $scope.showRegisterForm = false;
  $scope.registerData = {};

  // Get the app configuration
  Restangular.one('app').get().then(function (data) {
    $rootScope.app = data;
  });

  // Login as guest
  $scope.loginAsGuest = function () {
    $scope.user = {
      username: 'guest',
      password: ''
    };
    $scope.login();
  };

  // Login
  $scope.login = function () {
    User.login($scope.user).then(function () {
      User.userInfo(true).then(function (data) {
        $rootScope.userInfo = data;
      });

      if ($stateParams.redirectState !== undefined && $stateParams.redirectParams !== undefined) {
        $state.go($stateParams.redirectState, JSON.parse($stateParams.redirectParams))
          .catch(function () {
            $state.go('document.default');
          });
      } else {
        $state.go('document.default');
      }
    }, function (data) {
      if (data.data.type === 'ValidationCodeRequired') {
        // A TOTP validation code is required to login
        $scope.codeRequired = true;
      } else {
        // Login truly failed
        var title = $translate.instant('login.login_failed_title');
        var msg = $translate.instant('login.login_failed_message') + (data.data.message ? ': ' + data.data.message : '');
        var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };

  // Password lost
  $scope.openPasswordLost = function () {
    $uibModal.open({
      templateUrl: 'partial/docs/passwordlost.html',
      controller: 'ModalPasswordLost'
    }).result.then(function (username) {
      if (username === null) {
        return;
      }

      // Send a password lost email
      Restangular.one('user').post('password_lost', {
        username: username
      }).then(function () {
        var title = $translate.instant('login.password_lost_sent_title');
        var msg = $translate.instant('login.password_lost_sent_message', { username: username });
        var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
        $dialog.messageBox(title, msg, btns);
      }, function () {
        var title = $translate.instant('login.password_lost_error_title');
        var msg = $translate.instant('login.password_lost_error_message');
        var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
        $dialog.messageBox(title, msg, btns);
      });
    });
  };

  // Toggle between Login and Register form
  $scope.showRegisterForm = false; // 默认显示登录表单
  $scope.toggleRegisterForm = function () {
    $scope.showRegisterForm = !$scope.showRegisterForm;
    console.log('Toggling register form, showRegisterForm:', !$scope.showRegisterForm);
    if ($scope.showRegisterForm) {
      $scope.registerData = {}; // Reset form data when showing register
    }
  };

  // Cancel Register and return to Login
  $scope.cancelRegister = function () {
    $scope.showRegisterForm = false;
    $scope.registerData = {};
  };

  // Submit Register Request
  $scope.submitRegisterRequest = function () {
    if ($scope.registerForm.$valid) {
      Restangular.all('guest/register').post({
        username: $scope.registerData.username,
        email: $scope.registerData.email,
        password: $scope.registerData.password
      }).then(function (response) {
        var title = $translate.instant('login.register_success_title');
        var msg = $translate.instant('login.register_success_message');
        var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
        $dialog.messageBox(title, msg, btns);
        $scope.showRegisterForm = false;
        $scope.registerData = {};
      }, function (response) {
        var title = $translate.instant('login.register_error_title');
        var msg = $translate.instant('login.register_error_message') + (response.data.message ? ': ' + response.data.message : '');
        var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
        $dialog.messageBox(title, msg, btns);
      });
    }
  };
});