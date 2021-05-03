import 'dart:async';

import 'package:flutter/services.dart';

class AuraVpn {
  static const MethodChannel _channel = const MethodChannel('aura_vpn');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get initHydraSdk async {
    final bool result = await _channel.invokeMethod('initHydraSdk') ?? false;
    return result;
  }

  static Future<bool> get onStart async {
    await _channel.invokeMethod('onStart');
    return true;
  }

  static Future<bool> get onStop async {
    await _channel.invokeMethod('onStop');
    return true;
  }

  static Future<bool> get logOutFromVpn async {
    await _channel.invokeMethod('logOutFromVpn');
    return true;
  }

  static Future<bool> get isConnected async {
    await _channel.invokeMethod('isConnected');
    return true;
  }

  static Future<bool> get disconnectFromVpn async {
    await _channel.invokeMethod('disconnectFromVpn');
    return true;
  }

  static Future get chooseServer async {
    var l = await _channel.invokeMethod('chooseServer');
    print(l);
    return l;
  }

  static Future get getTrafficUpdate async {
    var l = await _channel.invokeMethod('getTrafficUpdate');
    print(l);
    return l;
  }

  static Future<bool> get getCurrentServer async {
    await _channel.invokeMethod('getCurrentServer');
    return true;
  }

  static Future<bool> get checkRemainingTraffic async {
    await _channel.invokeMethod('checkRemainingTraffic');
    return true;
  }

  static Future<bool> get setLoginParams async {
    await _channel.invokeMethod('setLoginParams');
    return true;
  }

  static Future<bool> get loginUser async {
    bool result = await _channel.invokeMethod('loginUser') ?? false;
    return result;
  }

  static Future<bool> get getRegionList async {
    var result = await _channel.invokeMethod('getRegionList');
    return true;
  }

  static Future<bool> get onRegionSelected async {
    await _channel.invokeMethod('onRegionSelected');
    return true;
  }

  static Future<bool> connectServer(String country) async {
    Map<String, String> cou = Map();
    cou.putIfAbsent("country", () => country);
    var result = await _channel.invokeMethod('connectServer', cou);
    return result ?? false;
  }
}
